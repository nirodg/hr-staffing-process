package org.db.hrsp.service.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.util.List;

@Entity
@Getter
@Setter
public class StaffingProcess extends AbstractEntity {

    private String title;
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    private User employee;

    @OneToMany(mappedBy = "staffingProcess")
    private List<Comment> comments;

}
