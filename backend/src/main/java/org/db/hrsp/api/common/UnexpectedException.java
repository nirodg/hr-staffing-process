package org.db.hrsp.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
public class UnexpectedException extends ResponseStatusException {
    public UnexpectedException(String msg) { super(HttpStatus.INTERNAL_SERVER_ERROR, msg); }
}