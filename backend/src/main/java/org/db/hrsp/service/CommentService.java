package org.db.hrsp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.common.UnexpectedException;
import org.db.hrsp.api.common.UpstreamFailureException;
import org.db.hrsp.api.config.ApiException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.mapper.CommentMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.CommentRepository;
import org.db.hrsp.service.repository.StaffingProcessRepository;
import org.db.hrsp.service.repository.model.Comment;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.User;
import org.springframework.dao.DataIntegrityViolationException;
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
    public CommentDTO addComment(Long staffingId, CommentDTO dto) throws UnexpectedException {

        StaffingProcess process = staffingProcessRepository.findById(staffingId)
                .orElseThrow(() ->
                        new NotFoundException("Staffing process %d not found".formatted(staffingId)));
        User user = jwtInterceptor.getCurrentUser();

        Comment comment = Comment.builder()
                .title(dto.getTitle())
                .comment(dto.getComment())
                .commentParent(dto.getCommentParent())
                .staffingProcess(process)
                .author(user)
                .build();


        try {
            comment = commentRepository.save(comment);
        } catch (DataIntegrityViolationException dup) {   // unlikely but safe
            throw new ConflictException("Duplicate comment");
        } catch (RuntimeException ex) {
            throw new UnexpectedException("Error saving comment");     // 500
        }

        publishEvent(comment.getStaffingProcess().getId(), KafkaPayload.Action.CREATE);
        log.info("Comment created: {}", comment.getId());

        return commentMapper.toDto(comment);
    }

    private void updateStaffingProcessWithNewComment(StaffingProcess staffingProcess, Comment comment) {
        List<Comment> existingComments = staffingProcess.getComments();
        existingComments.add(comment);
        staffingProcess.setComments(existingComments);
    }


    public CommentDTO getComment(Long id) {
        Comment comment = commentRepository.findById(id).
                orElseThrow(() -> new NotFoundException("Comment %d not found".formatted(id)));
        log.info("Comment found with ID: {}", comment.getId());
        return commentMapper.toDto(comment);
    }

    public List<CommentDTO> getAllComments() {
        log.info("Get all comments");
        return commentMapper.toDtos(commentRepository.findAll());
    }

    public CommentDTO updateComment(CommentDTO dto) throws UnexpectedException {
        Comment commentToUpdate = commentRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Comment %d not found".formatted(dto.getId())));
        commentToUpdate.setComment(dto.getComment());
        try {
            commentToUpdate = commentRepository.save(commentToUpdate);
        } catch (RuntimeException ex) {
            throw new UnexpectedException("Error updating comment");
        }

        publishEvent(commentToUpdate.getStaffingProcess().getId(), KafkaPayload.Action.UPDATE);
        return commentMapper.toDto(commentToUpdate);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new NotFoundException("Comment %d not found".formatted(id));
        }
        log.info("Delete comment with ID: {}", id);
        commentRepository.deleteById(id);
        publishEvent(id, KafkaPayload.Action.DELETE);
    }

    public List<CommentDTO> findByStaffingProcessId(Long staffingId) {
        return commentMapper.toDtos(commentRepository.findByStaffingProcessId(staffingId));
    }

    /* ───────────────────────────  HELPERS  ──────────────────────────── */
    private void publishEvent(Long entityId, KafkaPayload.Action action) {
        try {
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(action)
                            .userId(jwtInterceptor.getCurrentUser().getUsername())
                            .topic(KafkaPayload.Topic.COMMENTS)
                            .entityId(entityId)
                            .build());
        } catch (RuntimeException ex) {
            throw new UpstreamFailureException("Failed to publish Kafka message");
        }
    }

    public CommentDTO addComment(String title, String comment, Long staffingProcessId) {
        User user = jwtInterceptor.getCurrentUser();
        CommentDTO dto = CommentDTO.builder()
                .title(title)
                .comment(comment)
                .commentParent(null)
                .author(user)
                .build();

        return this.addComment(staffingProcessId, dto);
    }
}
