package org.db.hrsp.api.dto;

import lombok.*;
import org.db.hrsp.service.repository.model.StaffingProcess;

import java.util.List;

@Getter
@Setter
public class ClientDTO extends AbstractEntityDto {

    private String clientName;
    private List<StaffingProcess> staffingProcesses;
    private String clientEmail;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;
}
