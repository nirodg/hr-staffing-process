package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.service.repository.model.Employee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeDTO dto);
    EmployeeDTO toDto(Employee entity);

    List<EmployeeDTO> toDtos(Iterable<Employee> all);
}
