package org.db.hrsp.api.dto;

import lombok.*;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO extends AbstractEntityDto {

    private String title;
    private String comment;
    private StaffingProcess staffingProcess;
    public User author;
    private Long commentParent;

}
