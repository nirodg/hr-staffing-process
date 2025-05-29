package org.db.hrsp.service.repository.model;

import jakarta.persistence.*;
import lombok.*;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.time.Instant;

@Entity
@Table(name = "edit_lock_queue",
        indexes = {@Index(columnList = "entity, entityId")},
        uniqueConstraints = @UniqueConstraint(name = "uq_edit_lock",
                columnNames = {"entity", "entity_id", "username"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditLockQueue extends AbstractEntity {

    private String entity;
    @Column(name = "entity_id")
    private Long entityId;
    private String username;
    @Column(name = "requested_at")
    private Instant requestedAt;  // first time user asked for the lock (queue order)
    @Column(name = "last_seen")
    private Instant lastSeen;      // heartbeat timestamp

}
