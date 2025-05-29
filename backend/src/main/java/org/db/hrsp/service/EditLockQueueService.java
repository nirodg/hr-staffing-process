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

    private final EditLockQueueRepository editLockQueueRepository;
    private final EditLockProducer editLockProducer;

    @Transactional
    public boolean tryAcquireLock(String entity, Long entityId, String username, Long userId) {

        // upsert guarantees one row per user-entity pair and avoids duplicate key errors
        editLockQueueRepository.upsert(entity, entityId, username, Instant.now());

        List<EditLockQueue> queue =
                editLockQueueRepository.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId);

        boolean acquired = !queue.isEmpty() && username.equals(queue.get(0).getUsername());
        if (acquired) {
            KafkaPayload payload = KafkaPayload.builder()
                    .entity(entity)
                    .entityId(entityId)
                    .userId(String.valueOf(userId))
                    .username(username)
                    .action(KafkaPayload.Action.LOCK)
                    .topic(KafkaPayload.Topic.EDIT_LOCKS)
                    .build();
            editLockProducer.publishLock(payload);
        }
        return acquired;
    }

    @Transactional
    public void releaseLock(String entity, Long entityId, String username, Long userId) {
        // Delete current holder
        editLockQueueRepository.deleteByEntityAndEntityIdAndUsername(entity, entityId, username);

        // Promote next in line (if any) *inside the same TX*
        editLockQueueRepository.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId)
                .stream()
                .findFirst()
                .ifPresent(next -> {
                    KafkaPayload payload = KafkaPayload.builder()
                            .entity(entity)
                            .entityId(entityId)
                            .username(next.getUsername())
                            .action(KafkaPayload.Action.LOCK)   // promote!
                            .topic(KafkaPayload.Topic.EDIT_LOCKS)
                            .build();
                    editLockProducer.publishLock(payload);
                });
        // Notify UNLOCK for the one that just left
        KafkaPayload unlocked = KafkaPayload.builder()
                .entity(entity)
                .entityId(entityId)
                .userId(String.valueOf(userId))
                .username(username)
                .action(KafkaPayload.Action.UNLOCK)
                .topic(KafkaPayload.Topic.EDIT_LOCKS)
                .build();

        // Send the UNLOCK event
        editLockProducer.publishLock(unlocked);
    }

    public Optional<String> getCurrentEditor(String entity, Long entityId) {
        List<EditLockQueue> queue = editLockQueueRepository.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId);
        return queue.isEmpty() ? Optional.empty() : Optional.of(queue.get(0).getUsername());
    }
}