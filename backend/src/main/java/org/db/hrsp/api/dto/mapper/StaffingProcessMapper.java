package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffingProcessMapper {
    StaffingProcess toEntity(StaffingProcessDTO dto);
    StaffingProcessDTO toDto(StaffingProcess entity);

    List<StaffingProcessDTO> toDtos(Iterable<StaffingProcess> all);
}
