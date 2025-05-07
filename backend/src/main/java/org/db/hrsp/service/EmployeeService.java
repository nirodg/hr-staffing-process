package org.db.hrsp.service;


import lombok.AllArgsConstructor;
import org.db.hrsp.api.config.ApiException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.mapper.EmployeeMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.EmployeeRepository;
import org.db.hrsp.service.repository.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@LogMethodExecution
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    public EmployeeDTO createEmployee(String name) {
        Employee employee = new Employee(name, true);
        employee = repository.save(employee);

        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(KafkaPayload.Action.CREATE)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.COMMENTS)
                            .entityId(employee.getId())
                            .build());
        } catch (RuntimeException re) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null, "Failed to send message to Kafka");
        }

        return employeeMapper.toDto(employee);
    }

    public EmployeeDTO getEmployee(Long employeeId) {
        Employee employee = repository.findById(employeeId).orElseThrow();
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.toDtos(repository.findAll());
    }

}
