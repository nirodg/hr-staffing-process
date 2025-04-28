package org.db.hrsp.service;

import org.db.hrsp.config.JwtInterceptor;
import org.db.hrsp.dto.ClientDTO;
import org.db.hrsp.kafka.KafkaPayload;
import org.db.hrsp.kafka.KafkaPersistEventProducer;
import org.db.hrsp.model.Client;
import org.db.hrsp.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final KafkaPersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    public ClientService(ClientRepository repository, ClientRepository repository1, KafkaPersistEventProducer eventProducer, JwtInterceptor jwtInterceptor) {
        this.repository = repository1;
        this.eventProducer = eventProducer;
        this.jwtInterceptor = jwtInterceptor;
    }

    public ClientDTO createClient(ClientDTO name) {
        Client client = new Client();
        client.setClientName(name.getClientName());
        client.setClientEmail(name.getClientEmail());
        client.setContactPersonEmail(name.getContactPersonEmail());
        client.setContactPersonName(name.getContactPersonName());
        client.setContactPersonPhone(name.getContactPersonPhone());
        repository.save(client);
        logger.info("Client created successfully with ID: {}", client.getId());

        eventProducer.publishEvent(
                KafkaPayload.builder()
                        .action(KafkaPayload.Action.CREATE)
                        .userId(jwtInterceptor.getCurrentUser().getUsername())
                        .topic(KafkaPayload.Topic.CLIENTS)
                        .build());

        return convertClientToDTO(client);
    }

    public ClientDTO getClient(Long clientId) {
        Client client = repository.findById(clientId).orElseThrow();
        logger.info("Client found with ID: {}", client.getId());
        return convertClientToDTO(client);
    }

    public List<ClientDTO> getAllClients() {
        logger.info("Getting all clients");
        return Streamable.of(repository.findAll()).toList().stream().map(this::convertClientToDTO).toList();
    }

    public void deleteClientById(Long clientId) {
        logger.info("Deleting client with ID: {}", clientId);
        repository.deleteById(clientId);
    }

    public ClientDTO updateClient(ClientDTO client) {
        Client clientToUpdate = repository.findById(client.getId()).orElseThrow();
        clientToUpdate.setClientName(client.getClientName());
        clientToUpdate = repository.save(clientToUpdate);

        return convertClientToDTO(clientToUpdate);
    }

    private ClientDTO convertClientToDTO(Client client) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(client.getId());
        clientDTO.setClientName(client.getClientName());
        clientDTO.setClientEmail(client.getClientEmail());
        clientDTO.setContactPersonPhone(client.getContactPersonPhone());
        clientDTO.setContactPersonEmail(client.getContactPersonEmail());
        clientDTO.setContactPersonName(client.getContactPersonName());
        clientDTO.setStaffingProcesses(client.getStaffingProcesses());
        return clientDTO;
    }

}
