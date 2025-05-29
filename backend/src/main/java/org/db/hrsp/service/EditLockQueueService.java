package org.db.hrsp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.EditLockProducer;
import org.db.hrsp.service.repository.EditLockQueueRepository;
import org.db.hrsp.service.repository.model.EditLockQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditLockQueueService {

    private static final Duration TIMEOUT = Duration.ofMinutes(1);     // no heartbeat for 1 min ⇒ stale

    private final EditLockQueueRepository repo;
    private final EditLockProducer producer;

    /**
     * queue the user and (maybe) acquire the lock
     */
    @Transactional
    public boolean tryAcquire(String entity, Long entityId, String username, Long userId) {

        // 1) insert row once, keep original requestedAt, refresh lastSeen
        repo.insertIfAbsent(entity, entityId, username, Instant.now());

        // 2) oldest row owns the lock
        List<EditLockQueue> queue =
                repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId);

        boolean acquired = !queue.isEmpty() && username.equals(queue.get(0).getUsername());

        if (acquired) {
            producer.publishLock(payload(entity, entityId, username, userId, KafkaPayload.Action.LOCK));
        }
        return acquired;
    }

    /**
     * heartbeat from UI every N seconds
     */
    @Transactional
    public void touch(String entity, Long entityId, String username) {
        repo.touch(entity, entityId, username, Instant.now());
    }

    /**
     * user closes dialog or navigates away
     */
    @Transactional
    public void release(String entity, Long entityId, String username, Long userId) {

        repo.deleteRow(entity, entityId, username);

        producer.publishLock(payload(entity, entityId, username, userId, KafkaPayload.Action.UNLOCK));

        promoteNext(entity, entityId, userId);   // give lock to the next in queue, if any
    }

    /**
     * who owns the lock right now? (REST 409 response)
     */
    @Transactional(readOnly = true)
    public Optional<String> getCurrentEditor(String entity, Long entityId) {
        return repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId)
                .stream()
                .findFirst()
                .map(EditLockQueue::getUsername);
    }

    /* ========= HOUSEKEEPING ========= */

    /**
     * purge rows whose owners stopped heart-beating
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void purgeStaleLocks() {

        Instant cutoff = Instant.now().minus(TIMEOUT);
        int purged = repo.deleteStale(cutoff);
        if (purged == 0) return;

        log.debug("Purged {} stale edit-lock rows", purged);

        // check each remaining entity/entityId and promote if gap occurred
        repo.findAllDistinctEntityAndId()
                .forEach(key -> promoteNext(key.getEntity(), key.getEntityId(), key.userId()));
    }

    /* ========= INTERNAL HELPERS ========= */

    private void promoteNext(String entity, Long entityId, Long userId) {
        repo.findByEntityAndEntityIdOrderByRequestedAtAsc(entity, entityId)
                .stream()
                .findFirst()
                .ifPresent(next -> producer.publishLock(
                        payload(entity,
                                entityId,
                                next.getUsername(),
                                userId,
                                KafkaPayload.Action.LOCK)
                ));
    }

    private KafkaPayload payload(String entity,
                                 Long entityId,
                                 String username,
                                 Long userId,
                                 KafkaPayload.Action action) {
        return KafkaPayload.builder()
                .entity(entity)
                .entityId(entityId)
                .username(username)
                .userId(String.valueOf(userId))
                .action(action)
                .topic(KafkaPayload.Topic.EDIT_LOCKS)
                .build();
    }
}
