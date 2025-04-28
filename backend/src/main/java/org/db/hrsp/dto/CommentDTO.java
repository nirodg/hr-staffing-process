package org.db.hrsp.dto;

import org.db.hrsp.model.StaffingProcess;
import org.db.hrsp.model.User;
import org.db.hrsp.util.AbstractEntity;
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
