package org.db.hrsp.service.repository;

import jakarta.transaction.Transactional;
import org.db.hrsp.service.repository.model.EditLockQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EditLockQueueRepository extends JpaRepository<EditLockQueue, Long> {
    List<EditLockQueue> findByEntityAndEntityIdOrderByRequestedAtAsc(String entity, Long entityId);

    Optional<EditLockQueue> findByEntityAndEntityIdAndUsername(String entity, Long entityId, String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM EditLockQueue e WHERE e.entity = :entity AND e.entityId = :entityId AND e.username = :username")
    void deleteByEntityAndEntityIdAndUsername(@Param("entity") String entity,
                                              @Param("entityId") Long entityId,
                                              @Param("username") String username);

    @Modifying
    @Query(value = """
            INSERT INTO edit_lock_queue (entity, entity_id, username, requested_at)
            VALUES (:entity, :entityId, :username, :requestedAt)
            ON DUPLICATE KEY UPDATE requested_at = :requestedAt
            """,
            nativeQuery = true)
    void upsert(String entity, Long entityId, String username, Instant requestedAt);
}