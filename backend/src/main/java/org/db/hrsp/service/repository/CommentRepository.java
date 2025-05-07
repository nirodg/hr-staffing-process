package org.db.hrsp.service.repository;

import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.model.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@LogMethodExecution
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByStaffingProcessId(Long staffingProcessId);
}
