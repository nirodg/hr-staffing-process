package org.db.hrsp.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UpstreamFailureException extends ResponseStatusException {
    public UpstreamFailureException(String msg) { super(HttpStatus.BAD_GATEWAY, msg); }
}