package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.service.repository.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class UserMapper extends AbstractMapper<User, UserDTO> {

    @Override
    @Mapping(target = "username", ignore = true)
    public abstract User toEntity(UserDTO userDTO);

    @Override
    public abstract UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "username", ignore = true)
    public abstract void update(@MappingTarget User user, UserDTO userDTO);
}
