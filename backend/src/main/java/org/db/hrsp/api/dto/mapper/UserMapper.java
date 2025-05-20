package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.service.repository.model.Employee;
import org.db.hrsp.service.repository.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserDTO dto);
    UserDTO toDto(User entity);

}
