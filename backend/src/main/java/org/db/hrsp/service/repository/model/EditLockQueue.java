package org.db.hrsp.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "edit_lock_queue", indexes = {
        @Index(columnList = "entity, entityId")
})
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
    private Instant requestedAt;

}
