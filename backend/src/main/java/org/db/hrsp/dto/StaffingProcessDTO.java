package org.db.hrsp.dto;

import org.db.hrsp.model.Client;
import org.db.hrsp.model.Comment;
import org.db.hrsp.model.User;
import org.db.hrsp.util.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaffingProcessDTO extends AbstractEntity {

    private String title;
    private Client client;
    private User employee;
    private List<Comment> comments;
    private boolean isActive;

}
