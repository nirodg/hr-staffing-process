package org.db.hrsp.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConflictException extends ResponseStatusException {
    public ConflictException(String msg) {
        super(HttpStatus.CONFLICT, msg);
    }
}