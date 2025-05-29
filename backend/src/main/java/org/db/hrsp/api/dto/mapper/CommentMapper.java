package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.service.repository.model.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CommentMapper extends AbstractMapper<Comment, CommentDTO> {
    public abstract List<CommentDTO> toDtos(Iterable<Comment> comments);
}
