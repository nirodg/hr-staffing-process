package org.db.hrsp.dto.mapper;

import org.db.hrsp.dto.CommentDTO;
import org.db.hrsp.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDTO toDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTitle(comment.getTitle());
        dto.setComment(comment.getComment());
        dto.setCommentParent(comment.getCommentParent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setStaffingProcess(comment.getStaffingProcess());

        if (comment.getAuthor() != null) {
            dto.setAuthor(comment.getAuthor());
        }
        return dto;
    }
}
