package org.db.hrsp.service.repository;

import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.model.Client;
import org.springframework.data.repository.CrudRepository;

@LogMethodExecution
public interface ClientRepository extends CrudRepository<Client, Long> {
}
