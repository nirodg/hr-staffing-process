package org.db.hrsp.api.dto;

import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;
import org.db.hrsp.service.repository.model.util.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO extends AbstractEntity {

    private String title;
    private String comment;
    private StaffingProcess staffingProcess;
    public User author;
    private Long commentParent;

}
