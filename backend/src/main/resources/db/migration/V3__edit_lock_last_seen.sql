ALTER TABLE edit_lock_queue
    ADD COLUMN last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
           AFTER requested_at;
CREATE INDEX idx_edit_lock_last_seen ON edit_lock_queue (last_seen);