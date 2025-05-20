package org.db.hrsp.graphql;


import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.api.dto.mapper.StaffingProcessMapper;
import org.db.hrsp.service.StaffingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StaffingProcessGraphQLController {

    private final StaffingService staffingProcessService;
    private final StaffingProcessMapper staffingProcessMapper;

    @QueryMapping
    public List<StaffingProcessDTO> getStaffingProcesses() {
        return staffingProcessService.getAllStaffingProcesses();
    }

    @QueryMapping
    public StaffingProcessDTO getStaffingProcess(@Argument Long id) {
        return staffingProcessService.getStaffingProcess(id);
    }

    @MutationMapping
    public StaffingProcessDTO addStaffingProcess(
            @Argument String title,
            @Argument Long clientId,
            @Argument Long employeeId
    ) {
        return staffingProcessService.createStaffingProcess(clientId, employeeId, title);
    }
}