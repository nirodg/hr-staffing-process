package org.db.hrsp.service;

import org.db.hrsp.config.JwtInterceptor;
import org.db.hrsp.dto.CommentDTO;
import org.db.hrsp.dto.mapper.CommentMapper;
import org.db.hrsp.kafka.KafkaPayload;
import org.db.hrsp.kafka.KafkaPersistEventProducer;
import org.db.hrsp.model.Comment;
import org.db.hrsp.model.StaffingProcess;
import org.db.hrsp.model.User;
import org.db.hrsp.repository.CommentRepository;
import org.db.hrsp.repository.StaffingProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository repository;
    private final StaffingProcessRepository staffingProcessRepository;
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentMapper commentMapper;
    private JwtInterceptor jwtInterceptor;
    private final KafkaPersistEventProducer eventProducer;

    public CommentService(CommentRepository repository, StaffingProcessRepository staffingProcessRepository, CommentMapper commentMapper, JwtInterceptor jwtInterceptor, KafkaPersistEventProducer eventProducer) {
        this.repository = repository;
        this.staffingProcessRepository = staffingProcessRepository;
        this.commentMapper = commentMapper;
        this.jwtInterceptor = jwtInterceptor;
        this.eventProducer = eventProducer;
    }


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

        Comment saved = repository.save(comment);


        eventProducer.publishEvent(
                KafkaPayload.builder()
                        .action(KafkaPayload.Action.CREATE)
                        .userId(jwtInterceptor.getCurrentUser().getUsername())
                        .topic(KafkaPayload.Topic.COMMENTS)
                        .build());

        return commentMapper.toDto(saved);
    }

    private void updateStaffingProcessWithNewComment(StaffingProcess staffingProcess, Comment comment) {
        List<Comment> existingComments = staffingProcess.getComments();
        existingComments.add(comment);
        staffingProcess.setComments(existingComments);
    }


    public CommentDTO getComment(Long commentId) {
        Comment comment = repository.findById(commentId).orElseThrow();
        logger.info("Comment found with ID: {}", comment.getId());
        return convertCommentToDTO(comment);
    }

    public List<CommentDTO> getAllComments() {
        logger.info("Get all comments");
        return Streamable.of(repository.findAll()).toList().stream().map(this::convertCommentToDTO).toList();
    }

    public CommentDTO updateComment(CommentDTO comment) {
        Comment commentToUpdate = repository.findById(comment.getId()).orElseThrow();
        commentToUpdate.setComment(comment.getComment());
        commentToUpdate = repository.save(commentToUpdate);

        return convertCommentToDTO(commentToUpdate);
    }

    public void deleteComment(Long commentId) {
        logger.info("Delete comment with ID: {}", commentId);
        repository.deleteById(commentId);
    }

    private CommentDTO convertCommentToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setTitle(comment.getTitle());
        commentDTO.setComment(comment.getComment());
        commentDTO.setStaffingProcess(comment.getStaffingProcess());
        commentDTO.setCommentParent(comment.getCommentParent());
        return commentDTO;
    }

    public List<CommentDTO> findByStaffingProcessId(Long staffingId) {
        List<Comment> comments = repository.findByStaffingProcessId(staffingId);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
}
