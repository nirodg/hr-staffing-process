package org.db.hrsp.service.repository;

import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.model.Employee;
import org.springframework.data.repository.CrudRepository;


public interface EmployeeRepository extends CrudRepository<Employee, Long> {
}
