package org.db.hrsp.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.Client;
import org.db.hrsp.service.repository.model.Comment;
import org.db.hrsp.service.repository.model.User;

import java.util.List;

@Getter
@Setter
public class StaffingProcessDTO extends AbstractEntityDto {

    private String title;
    private Client client;
    private User employee;
    private List<Comment> comments;
    private boolean isActive;

}
