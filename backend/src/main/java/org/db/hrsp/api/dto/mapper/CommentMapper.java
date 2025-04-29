package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.service.repository.model.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toEntity(CommentDTO dto);
    CommentDTO toDto(Comment entity);

    List<CommentDTO> toDtos(Iterable<Comment> comments);
}
