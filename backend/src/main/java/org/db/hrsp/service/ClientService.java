package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.common.UnexpectedException;
import org.db.hrsp.api.common.UpstreamFailureException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.api.dto.mapper.ClientMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.ClientRepository;
import org.db.hrsp.service.repository.model.Client;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@LogMethodExecution
@RequiredArgsConstructor
public class ClientService extends AbstractService<Client, ClientDTO, ClientRepository, ClientMapper> {

    private final ClientRepository clientRepository;

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    @Override
    public ClientDTO create(ClientDTO dto) {

        // optimistic Java-side uniqueness check
        if (clientRepository.existsByClientNameIgnoreCase(dto.getClientName())) {
            throw new ConflictException("Client '%s' already exists".formatted(dto.getClientName()));
        }
        ClientDTO responseDto = super.create(dto);

        publishEvent(responseDto.getId(), KafkaPayload.Action.CREATE);
        log.info("Client created: {}", responseDto.getId());

        return responseDto;
    }

    /* ───────────────────────────  HELPERS  ───────────────────────── */

    private void publishEvent(Long id, KafkaPayload.Action action) {
        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(action)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.CLIENTS)
                            .entityId(id)
                            .build());
        } catch (RuntimeException ex) {
            throw new UpstreamFailureException("Failed to publish Kafka message");
        }
    }
}
