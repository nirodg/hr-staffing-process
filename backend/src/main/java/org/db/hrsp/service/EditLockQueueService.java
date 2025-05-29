package org.db.hrsp.service;


import lombok.RequiredArgsConstructor;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.EditLockProducer;
import org.db.hrsp.service.repository.EditLockQueueRepository;
import org.db.hrsp.service.repository.model.EditLockQueue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EditLockQueueService {

    private final EditLockQueueRepository repo;
    private final EditLockProducer producer;

    @Transactional
    public boolean tryAcquireLock(String entity, Long entityId, String username, Long userId) {

        repo.insertIfAbsent(entity, entityId, username, Instant.now());

        List<EditLockQueue> queue =
                repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId);

        boolean acquired = !queue.isEmpty() && username.equals(queue.get(0).getUsername());

        if (acquired) {
            producer.publishLock(KafkaPayload.builder()
                    .entity(entity)
                    .entityId(entityId)
                    .userId(String.valueOf(userId))
                    .username(username)
                    .action(KafkaPayload.Action.LOCK)
                    .topic(KafkaPayload.Topic.EDIT_LOCKS)
                    .build());
        }
        return acquired;
    }

    @Transactional
    public void releaseLock(String entity, Long entityId, String username, Long userId) {

        repo.deleteByEntityAndEntityIdAndUsername(entity, entityId, username);

        /* UNLOCK event for the one leaving */
        producer.publishLock(KafkaPayload.builder()
                .entity(entity)
                .entityId(entityId)
                .userId(String.valueOf(userId))
                .username(username)
                .action(KafkaPayload.Action.UNLOCK)
                .topic(KafkaPayload.Topic.EDIT_LOCKS)
                .build());

        /* promote next (oldest) — if any */
        repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId)
                .stream()
                .findFirst()
                .ifPresent(next -> producer.publishLock(KafkaPayload.builder()
                        .entity(entity)
                        .entityId(entityId)
                        .username(next.getUsername())
                        .action(KafkaPayload.Action.LOCK)
                        .topic(KafkaPayload.Topic.EDIT_LOCKS)
                        .build()));
    }

    public Optional<String> getCurrentEditor(String entity, Long entityId) {
        return repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId)
                .stream()
                .findFirst()
                .map(EditLockQueue::getUsername);
    }
}