package org.db.hrsp.graphql;


import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.mapper.CommentMapper;
import org.db.hrsp.service.CommentService;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentGraphQLController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @QueryMapping
    public List<CommentDTO> getComments() {
        return commentService.getAllComments();
    }

    @QueryMapping
    public CommentDTO getComment(@Argument Long id) {
        return commentService.getComment(id);
    }

    @MutationMapping
    public CommentDTO addComment(
            @Argument String title,
            @Argument String comment,
            @Argument Long staffingProcessId,
            @Argument Long authorId
    ) {
        return commentService.addComment(title, comment, staffingProcessId);
    }
}