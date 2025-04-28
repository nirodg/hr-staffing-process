package org.db.hrsp.service;

import org.db.hrsp.config.JwtInterceptor;
import org.db.hrsp.dto.StaffingProcessDTO;
import org.db.hrsp.kafka.KafkaPayload;
import org.db.hrsp.kafka.KafkaPersistEventProducer;
import org.db.hrsp.model.Client;
import org.db.hrsp.model.StaffingProcess;
import org.db.hrsp.model.User;
import org.db.hrsp.repository.ClientRepository;
import org.db.hrsp.repository.StaffingProcessRepository;
import org.db.hrsp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffingService {

    private final StaffingProcessRepository repository;
    private final ClientRepository clientRepository;
    private static final Logger logger = LoggerFactory.getLogger(StaffingService.class);
    private final UserRepository userRepository;

    private final KafkaPersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    public StaffingService(StaffingProcessRepository repository, ClientRepository clientRepository, UserRepository userRepository, KafkaPersistEventProducer eventProducer, JwtInterceptor jwtInterceptor) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.eventProducer = eventProducer;
        this.jwtInterceptor = jwtInterceptor;
    }

    public StaffingProcessDTO createStaffingProcess(Long clientID, Long employeeId, String title) {
        StaffingProcess staffingProcess = new StaffingProcess();
        Client client = clientRepository.findById(clientID).orElseThrow();
        User employee = userRepository.findById(employeeId).orElseThrow();

        staffingProcess.setClient(client);
        staffingProcess.setEmployee(employee);
        staffingProcess.setTitle(title);
        staffingProcess.setActive(true);
        repository.save(staffingProcess);
        logger.info("Staffing Process created successfully with ID: {}", staffingProcess.getId());

        updateEmployeeAndClientStaffingProcesses(employee, client, staffingProcess);

        eventProducer.publishEvent(
                KafkaPayload.builder()
                        .action(KafkaPayload.Action.CREATE)
                        .userId(jwtInterceptor.getCurrentUser().getUsername())
                        .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                        .build()
        );

        return convertStaffingProcessToDTO(staffingProcess);
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
        StaffingProcess process = repository.findById(staffingProcessId).orElseThrow();
        logger.info("Staffing Process retrieved successfully with ID: {}", staffingProcessId);
        return convertStaffingProcessToDTO(process);
    }

    public List<StaffingProcessDTO> getAllStaffingProcesses() {
        logger.info("Get all Staffing Processes ");
        return Streamable.of(repository.findAll()).toList().stream().map(this::convertStaffingProcessToDTO).toList();
    }

    public StaffingProcessDTO updateStaffingProcess(StaffingProcessDTO staffingProcess) {
        StaffingProcess staffingProcessToUpdate = repository.findById(staffingProcess.getId()).orElseThrow();
        staffingProcessToUpdate.setActive(staffingProcess.isActive());
        staffingProcessToUpdate.setTitle(staffingProcess.getTitle());
        staffingProcessToUpdate = repository.save(staffingProcessToUpdate);

        return convertStaffingProcessToDTO(staffingProcessToUpdate);
    }

    public void deleteStaffingProcessById(Long staffingProcessId) {
        logger.info("Delete Staffing Process by ID: {}", staffingProcessId);
        repository.deleteById(staffingProcessId);
    }

    private StaffingProcessDTO convertStaffingProcessToDTO(StaffingProcess staffingProcess) {
        StaffingProcessDTO dto = new StaffingProcessDTO();
        dto.setId(staffingProcess.getId());
        dto.setTitle(staffingProcess.getTitle());
        dto.setClient(staffingProcess.getClient());
        dto.setEmployee(staffingProcess.getEmployee());
        dto.setComments(staffingProcess.getComments());
        dto.setActive(staffingProcess.isActive());

        return dto;
    }

    @Transactional
    public void setInactive(Long id) throws ChangeSetPersister.NotFoundException {
        StaffingProcess process = repository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        process.setActive(false);
        repository.save(process);

        eventProducer.publishEvent(
                KafkaPayload.builder()
                        .action(KafkaPayload.Action.UPDATE)
                        .userId(jwtInterceptor.getCurrentUser().getUsername())
                        .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                        .build());
    }

}
