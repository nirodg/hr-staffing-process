package org.db.hrsp.service.repository;

import org.db.hrsp.service.repository.model.EditLockQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository that behaves like a FIFO queue for every (entity, entityId).
 */
public interface EditLockQueueRepository extends JpaRepository<EditLockQueue, Long> {

    /* ---------- queue inspection ---------- */

    List<EditLockQueue> findByEntityAndEntityIdOrderByRequestedAtAsc(String entity, Long entityId);

    Optional<EditLockQueue> findByEntityAndEntityIdAndUsername(
            String entity, Long entityId, String username);

    /* ---------- queue maintenance ---------- */

    /**
     * Insert row once (if absent) but always refresh {@code last_seen}.
     * <p>► MySQL-specific <em>upsert</em> guarantees “one row per user”.</p>
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO edit_lock_queue (entity, entity_id, username, requested_at, last_seen)
            VALUES (:entity, :entityId, :username, :now, :now)
            ON DUPLICATE KEY UPDATE last_seen = :now
            """, nativeQuery = true)
    void insertIfAbsent(String entity, Long entityId, String username, Instant now);

    /**
     * Heart-beat: refresh {@code last_seen}.
     */
    @Modifying
    @Transactional
    @Query("""
            update EditLockQueue e
               set e.lastSeen = :now
             where e.entity   = :entity
               and e.entityId = :entityId
               and e.username = :username
            """)
    void touch(String entity, Long entityId, String username, Instant now);

    /**
     * Remove a single row (normal dialog close).
     */
    @Modifying
    @Transactional
    @Query("""
            delete from EditLockQueue e
             where e.entity   = :entity
               and e.entityId = :entityId
               and e.username = :username
            """)
    void deleteRow(String entity, Long entityId, String username);

    /**
     * Purge stale rows (no heart-beat for N minutes).
     */
    @Modifying
    @Transactional
    @Query("""
            delete from EditLockQueue e
             where e.lastSeen < :cutoff
            """)
    int deleteStale(Instant cutoff);

    /* ---------- helper projection ---------- */

    interface LockKey {
        String getEntity();

        Long getEntityId();

        Long userId();  // optional, if available
    }

    /**
     * Distinct locked entities (used after purge to promote next owner).
     */
    @Query("""
            select distinct e.entity as entity, e.entityId as entityId
              from EditLockQueue e
            """)
    List<LockKey> findAllDistinctEntityAndId();
}
