package org.db.hrsp.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.common.UpstreamFailureException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.api.dto.mapper.StaffingProcessMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.ClientRepository;
import org.db.hrsp.service.repository.StaffingProcessRepository;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.Client;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@LogMethodExecution
public class StaffingService {

    private final StaffingProcessRepository staffingProcessRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final StaffingProcessMapper staffingProcessMapper;

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    @Transactional
    public StaffingProcessDTO createStaffingProcess(Long clientId, Long employeeId, String title) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client %d not found".formatted(clientId)));
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(employeeId)));

        StaffingProcess staffingProcess = StaffingProcess.builder()
                .client(client)
                .employee(employee)
                .title(title)
                .isActive(true)
                .build();

        staffingProcessRepository.save(staffingProcess);
        log.info("Staffing Process created successfully with ID: {}", staffingProcess.getId());

        updateBackrefs(employee, client, staffingProcess);
        publish(KafkaPayload.Action.CREATE);

        return staffingProcessMapper.toDto(staffingProcess);
    }

    public StaffingProcessDTO getStaffingProcess(Long id) {
        return staffingProcessRepository.findById(id)
                .map(staffingProcessMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Staffing process %d not found".formatted(id)));
    }

    public List<StaffingProcessDTO> getAllStaffingProcesses() {
        return staffingProcessMapper.toDtos(staffingProcessRepository.findAll());
    }

    @Transactional
    public StaffingProcessDTO updateStaffingProcess(StaffingProcessDTO dto) {
        StaffingProcess staffingProcessToUpdate = staffingProcessRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Staffing process %d not found".formatted(dto.getId())));

        staffingProcessToUpdate.setActive(dto.isActive());
        staffingProcessToUpdate.setTitle(dto.getTitle());
        staffingProcessToUpdate = staffingProcessRepository.save(staffingProcessToUpdate);

        return staffingProcessMapper.toDto(staffingProcessToUpdate);
    }

    @Transactional
    public void deleteStaffingProcessById(Long id) {
        if (!staffingProcessRepository.existsById(id)) {
            throw new NotFoundException("Staffing process %d not found".formatted(id));
        }
        log.info("Delete Staffing Process by ID: {}", id);
        staffingProcessRepository.deleteById(id);
    }

    @Transactional
    public void setInactive(Long id) {
        StaffingProcess process = staffingProcessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staffing process %d not found".formatted(id)));
        process.setActive(false);
        staffingProcessRepository.save(process);

        publish(KafkaPayload.Action.UPDATE);
    }

    // Helpers
    private void updateBackrefs(User employee, Client client, StaffingProcess staffingProcess) {
        List<StaffingProcess> existingEmployeeStaffingProcesses = employee.getStaffingProcesses();
        existingEmployeeStaffingProcesses.add(staffingProcess);
        employee.setStaffingProcesses(existingEmployeeStaffingProcesses);

        List<StaffingProcess> existingClientStaffingProcesses = client.getStaffingProcesses();
        existingClientStaffingProcesses.add(staffingProcess);
        client.setStaffingProcesses(existingClientStaffingProcesses);

        userRepository.save(employee);
        clientRepository.save(client);
    }

    private void publish(KafkaPayload.Action action) {
        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(action)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                            .build());
        } catch (RuntimeException ex) {
            throw new UpstreamFailureException("Failed to publish Kafka event");
        }
    }

    public List<StaffingProcessDTO> findByEmployeeId(String username, Pageable pageable) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(value -> staffingProcessRepository.findByEmployeeId(value.getId(), pageable).stream().map(staffingProcessMapper::toDto).toList()).orElse(null);
    }

    public List<StaffingProcessDTO> findByClientId(Long clientId, Pageable pageable) {
        List<StaffingProcess> list = staffingProcessRepository.findByClientId(clientId, pageable);
        return list.stream().map(staffingProcessMapper::toDto).toList();
    }

    @Transactional
    public StaffingProcessDTO updateTitle(Long processId, String newTitle) {
        StaffingProcess process = staffingProcessRepository.findById(processId)
                .orElseThrow(() -> new NotFoundException("Process not found"));
        process.setTitle(newTitle);
        return staffingProcessMapper.toDto(staffingProcessRepository.save(process));
    }

}
