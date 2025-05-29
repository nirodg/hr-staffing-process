CREATE TABLE edit_lock_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity VARCHAR(255) NOT NULL,
    entity_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_edit_lock UNIQUE (entity, entity_id, username)
);

CREATE INDEX idx_edit_lock_entity_entityid ON edit_lock_queue(entity, entity_id);