package org.db.hrsp.service.repository;

import org.db.hrsp.service.repository.model.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {
}
