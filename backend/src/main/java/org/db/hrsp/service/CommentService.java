package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.config.ApiException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.mapper.CommentMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.model.Comment;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;
import org.db.hrsp.service.repository.CommentRepository;
import org.db.hrsp.service.repository.StaffingProcessRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@LogMethodExecution
public class CommentService {

    private final CommentRepository commentRepository;
    private final StaffingProcessRepository staffingProcessRepository;
    private final CommentMapper commentMapper;
    private final JwtInterceptor jwtInterceptor;
    private final PersistEventProducer eventProducer;

    @Transactional
    public CommentDTO addComment(Long staffingId, CommentDTO dto) throws ChangeSetPersister.NotFoundException {
        Comment comment = new Comment();

        comment.setTitle(dto.getTitle());
        comment.setComment(dto.getComment());
        comment.setCommentParent(dto.getCommentParent());

        // set staffing process
        StaffingProcess process = staffingProcessRepository.findById(staffingId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        comment.setStaffingProcess(process);

        // set current employee as author
        User current = jwtInterceptor.getCurrentUser(); // adjust if needed
        comment.setAuthor(current);

        Comment saved = null;
        try {
            saved = commentRepository.save(comment);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, null, "Error saving comment");
        }

        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(KafkaPayload.Action.CREATE)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.COMMENTS)
                            .entityId(saved.getStaffingProcess().getId()) // We're using the staffing process ID as the entity ID for now
                            .build());
        } catch (RuntimeException re) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null, "Failed to send message to Kafka");
        }

        return commentMapper.toDto(saved);
    }

    private void updateStaffingProcessWithNewComment(StaffingProcess staffingProcess, Comment comment) {
        List<Comment> existingComments = staffingProcess.getComments();
        existingComments.add(comment);
        staffingProcess.setComments(existingComments);
    }


    public CommentDTO getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        log.info("Comment found with ID: {}", comment.getId());
        return commentMapper.toDto(comment);
    }

    public List<CommentDTO> getAllComments() {
        log.info("Get all comments");
        return commentMapper.toDtos(commentRepository.findAll());
    }

    public CommentDTO updateComment(CommentDTO comment) {
        Comment commentToUpdate = commentRepository.findById(comment.getId()).orElseThrow();
        commentToUpdate.setComment(comment.getComment());
        commentToUpdate = commentRepository.save(commentToUpdate);

        return commentMapper.toDto(commentToUpdate);
    }

    public void deleteComment(Long commentId) {
        log.info("Delete comment with ID: {}", commentId);
        commentRepository.deleteById(commentId);
    }

    public List<CommentDTO> findByStaffingProcessId(Long staffingId) {
        return commentMapper.toDtos(commentRepository.findByStaffingProcessId(staffingId));
    }
}
