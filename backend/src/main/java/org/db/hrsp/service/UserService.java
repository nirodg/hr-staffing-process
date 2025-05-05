package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@LogMethodExecution
public class UserService {

    private final UserRepository repository;

}
