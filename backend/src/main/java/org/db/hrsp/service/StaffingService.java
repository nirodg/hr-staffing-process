package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.config.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public StaffingProcessDTO createStaffingProcess(Long clientID, Long employeeId, String title) {
        StaffingProcess staffingProcess = new StaffingProcess();
        Client client = clientRepository.findById(clientID).orElseThrow();
        User employee = userRepository.findById(employeeId).orElseThrow();

        staffingProcess.setClient(client);
        staffingProcess.setEmployee(employee);
        staffingProcess.setTitle(title);
        staffingProcess.setActive(true);
        staffingProcessRepository.save(staffingProcess);
        log.info("Staffing Process created successfully with ID: {}", staffingProcess.getId());

        updateEmployeeAndClientStaffingProcesses(employee, client, staffingProcess);

        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(KafkaPayload.Action.CREATE)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                            .build()
            );
        } catch (RuntimeException re) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null, "Failed to send message to Kafka");
        }

        return staffingProcessMapper.toDto(staffingProcess);
    }

    private void updateEmployeeAndClientStaffingProcesses(User employee, Client client, StaffingProcess staffingProcess) {
        List<StaffingProcess> existingEmployeeStaffingProcesses = employee.getStaffingProcesses();
        existingEmployeeStaffingProcesses.add(staffingProcess);
        employee.setStaffingProcesses(existingEmployeeStaffingProcesses);

        List<StaffingProcess> existingClientStaffingProcesses = client.getStaffingProcesses();
        existingClientStaffingProcesses.add(staffingProcess);
        client.setStaffingProcesses(existingClientStaffingProcesses);

        userRepository.save(employee);
        clientRepository.save(client);
    }

    public StaffingProcessDTO getStaffingProcess(Long staffingProcessId) {
        StaffingProcess process = staffingProcessRepository.findById(staffingProcessId).orElseThrow();
        log.info("Staffing Process retrieved successfully with ID: {}", staffingProcessId);
        return staffingProcessMapper.toDto(process);
    }

    public List<StaffingProcessDTO> getAllStaffingProcesses() {
        return staffingProcessMapper.toDtos(staffingProcessRepository.findAll());
    }

    public StaffingProcessDTO updateStaffingProcess(StaffingProcessDTO staffingProcess) {
        StaffingProcess staffingProcessToUpdate = staffingProcessRepository.findById(staffingProcess.getId()).orElseThrow();
        staffingProcessToUpdate.setActive(staffingProcess.isActive());
        staffingProcessToUpdate.setTitle(staffingProcess.getTitle());
        staffingProcessToUpdate = staffingProcessRepository.save(staffingProcessToUpdate);

        return staffingProcessMapper.toDto(staffingProcessToUpdate);
    }

    public void deleteStaffingProcessById(Long staffingProcessId) {
        log.info("Delete Staffing Process by ID: {}", staffingProcessId);
        staffingProcessRepository.deleteById(staffingProcessId);
    }

    @Transactional
    public void setInactive(Long id) throws ChangeSetPersister.NotFoundException {
        StaffingProcess process = staffingProcessRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        process.setActive(false);
        staffingProcessRepository.save(process);

        eventProducer.publishEvent(
                KafkaPayload.builder()
                        .action(KafkaPayload.Action.UPDATE)
                        .userId(jwtInterceptor.getCurrentUser().getUsername())
                        .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                        .build());
    }

}
