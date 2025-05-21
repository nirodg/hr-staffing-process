package org.db.hrsp.service.repository;

import org.db.hrsp.service.repository.model.StaffingProcess;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StaffingProcessRepository extends CrudRepository<StaffingProcess, Long> {
    List<StaffingProcess> findByEmployeeId(Long employeeId, Pageable pageable);
}
