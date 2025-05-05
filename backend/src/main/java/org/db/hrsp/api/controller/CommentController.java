package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.mapper.CommentMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.model.Comment;
import org.db.hrsp.service.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/comments")
@AllArgsConstructor
@LogMethodExecution
public class CommentController {

    private final CommentRepository service;
    private final CommentMapper commentMapper;


    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable("commentId") Long commentId) {
        Optional<Comment> comment = service.findById(commentId);

        return comment.get().getId() != null
                ? ResponseEntity.ok(commentMapper.toDto(comment.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping()
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        Iterable<Comment> comments = service.findAll();
        if (!comments.iterator().hasNext()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(commentMapper.toDtos(comments));
    }

}
