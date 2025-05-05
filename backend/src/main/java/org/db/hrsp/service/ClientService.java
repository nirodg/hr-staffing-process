package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.config.ApiException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.api.dto.mapper.ClientMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.ClientRepository;
import org.db.hrsp.service.repository.model.Client;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@LogMethodExecution
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    @Transactional
    public ClientDTO createClient(ClientDTO client) {

        Client entity = clientRepository.save(clientMapper.toEntity(client));

        try {
            eventProducer.publishEvent(KafkaPayload.builder()
                    .entityId(client.getId())
                    .topic(KafkaPayload.Topic.CLIENTS)
                    .action(KafkaPayload.Action.CREATE)
                    .userId(jwtInterceptor.getCurrentUser().getUsername()).build());
        } catch (RuntimeException re) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null, "Failed to send message to Kafka");
        }

        return clientMapper.toDto(entity);
    }

    public ResponseEntity<ClientDTO> findById(Long clientId) {
        Optional<Client> entity = clientRepository.findById(clientId);
        return entity.map(value -> ResponseEntity.ok(clientMapper.toDto(value))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<List<ClientDTO>> findAll() {
        Iterable<Client> list = clientRepository.findAll();
        if (!list.iterator().hasNext()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientMapper.toDtos(list));
    }
}
