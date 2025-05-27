package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.AbstractEntityDto;
import org.db.hrsp.service.repository.model.util.AbstractEntity;
import org.mapstruct.*;

@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public abstract class AbstractMapper<AbstractEntity, AbstractEntityDto> {
    public abstract AbstractEntity toEntity(AbstractEntityDto dto);

    public abstract AbstractEntityDto toDto(AbstractEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void update(@MappingTarget AbstractEntity entity, AbstractEntityDto dto);
}
