package org.db.hrsp.dto;

import org.db.hrsp.model.StaffingProcess;

import java.util.List;

import org.db.hrsp.util.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDTO extends AbstractEntity {

    private String clientName;
    private List<StaffingProcess> staffingProcesses;
    private String clientEmail;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;
}
