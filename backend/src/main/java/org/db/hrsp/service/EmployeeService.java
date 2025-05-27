package org.db.hrsp.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.common.UpstreamFailureException;
import org.db.hrsp.api.config.ApiException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.mapper.EmployeeMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.EmployeeRepository;
import org.db.hrsp.service.repository.model.Employee;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.keycloak.util.JsonSerialization.mapper;

@Slf4j
@Service
@AllArgsConstructor
@LogMethodExecution
public class EmployeeService extends AbstractService<Employee, EmployeeDTO, EmployeeRepository, EmployeeMapper> {

    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    @Transactional
    public EmployeeDTO createEmployee(String name) {

        Employee employee;
        try {
            employee = repository.save(new Employee(name, true));
        } catch (DataIntegrityViolationException dup) {         // DB unique key
            throw new ConflictException("Employee '%s' already exists".formatted(name));
        }

        publishEvent(employee.getId(), KafkaPayload.Action.CREATE);
        log.info("Employee created: {}", employee.getId());
        return employeeMapper.toDto(employee);
    }

    public EmployeeDTO getEmployee(Long id) {
        Employee employee = repository.findById(id).orElseThrow(() -> new NotFoundException("Employee %d not found".formatted(id)));
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.toDtos(repository.findAll());
    }

    /* ─────────────────────────────── HELPERS ───────────────────────────── */
    private void publishEvent(Long entityId, KafkaPayload.Action action) {
        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(action)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.COMMENTS)
                            .entityId(entityId)
                            .build());
        } catch (RuntimeException ex) {
            throw new UpstreamFailureException("Failed to publish Kafka message");
        }
    }
}
