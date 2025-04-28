package org.db.hrsp.controller;

import org.db.hrsp.dto.CommentDTO;
import org.db.hrsp.service.CommentService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable("commentId") Long commentId) {
        CommentDTO comment = service.getComment(commentId);

        return comment.getId() != null
                ? ResponseEntity.ok(comment)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping()
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        List<CommentDTO> comments = service.getAllComments();
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comments);
    }

    @Transactional
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@RequestBody CommentDTO comment) {
        return ResponseEntity.ok(service.updateComment(comment));
    }

    @Transactional
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("clientId") Long commentId) {
        service.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }


}
