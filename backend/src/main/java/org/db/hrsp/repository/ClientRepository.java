package org.db.hrsp.repository;

import org.db.hrsp.model.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {
}
