package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.mapper.CommentMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.CommentService;
import org.db.hrsp.service.repository.CommentRepository;
import org.db.hrsp.service.repository.model.Comment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
@LogMethodExecution
public class CommentController {

    private final CommentService service;

    @GetMapping("/{id}")
    public CommentDTO getComment(@PathVariable("id") Long commentId) {
        return service.getComment(commentId);
    }

    @GetMapping()
    public List<CommentDTO> getAllComments() {
        return service.getAllComments();
    }

}
