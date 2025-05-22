package org.db.hrsp.api.dto.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public abstract class AbstractMapper<AbstractEntity, AbstractEntityDto> {
    public abstract AbstractEntity toEntity(AbstractEntityDto dto);

    public abstract AbstractEntityDto toDto(AbstractEntity entity);

    public abstract void update(@MappingTarget AbstractEntity entity, AbstractEntityDto dto);
}
