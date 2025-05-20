package org.db.hrsp.api.dto;

import lombok.*;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO extends AbstractEntity {

    private String title;
    private String comment;
    private StaffingProcess staffingProcess;
    public User author;
    private Long commentParent;

}
