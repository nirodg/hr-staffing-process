package org.db.hrsp.api.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.net.URI;

@AllArgsConstructor
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final URI type;
    private final String title;
}
