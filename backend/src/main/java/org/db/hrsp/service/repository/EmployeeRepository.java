package org.db.hrsp.service.repository;

import org.db.hrsp.service.repository.model.Employee;
import org.springframework.data.repository.CrudRepository;


public interface EmployeeRepository extends CrudRepository<Employee, Long> {
}
