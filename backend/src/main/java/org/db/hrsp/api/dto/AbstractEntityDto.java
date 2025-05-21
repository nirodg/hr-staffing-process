package org.db.hrsp.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public  class AbstractEntityDto {
    private Long id;
    private Long version;
    private Date createdAt;
    private Date updatedAt;
}
