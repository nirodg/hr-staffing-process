package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.service.repository.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserMapper extends AbstractMapper<User, UserDTO> {
}
