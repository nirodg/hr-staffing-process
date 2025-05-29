package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class StaffingProcessMapper extends AbstractMapper<StaffingProcess, StaffingProcessDTO> {
    public abstract List<StaffingProcessDTO> toDtos(Iterable<StaffingProcess> all);
}
