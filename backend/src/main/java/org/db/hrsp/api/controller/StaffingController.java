package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.CommentService;
import org.db.hrsp.service.StaffingService;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/staffing-processes")
@LogMethodExecution
@AllArgsConstructor
public class StaffingController {

    private final StaffingService service;
    private final CommentService commentService;

    @Transactional
    @PostMapping()
    public ResponseEntity<StaffingProcessDTO> addStaffingProcess(@RequestBody StaffingProcessDTO staffingProcess) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createStaffingProcess(staffingProcess.getClient().getId(), staffingProcess.getEmployee().getId(), staffingProcess.getTitle()));
    }

    @GetMapping("/{staffingProcessId}")
    public ResponseEntity<StaffingProcessDTO> getStaffingProcess(@PathVariable("staffingProcessId") Long staffingProcessId) {
        StaffingProcessDTO staffingProcess = service.getStaffingProcess(staffingProcessId);

        return staffingProcess.getId() != null
                ? ResponseEntity.ok(staffingProcess)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping()
    public ResponseEntity<List<StaffingProcessDTO>> getAllStaffingProcesses() {
        List<StaffingProcessDTO> staffingProcesses = service.getAllStaffingProcesses();
        if (staffingProcesses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(staffingProcesses);
    }

    @Transactional
    @PutMapping("/{staffingProcessId}")
    public ResponseEntity<StaffingProcessDTO> updateEmployee(@RequestBody StaffingProcessDTO staffingProcess) {
        return ResponseEntity.ok(service.updateStaffingProcess(staffingProcess));
    }

    @Transactional
    @DeleteMapping("/{staffingProcessId}")
    public ResponseEntity<Void> deleteClient(@PathVariable("staffingProcessId") Long staffingProcessId) {
        service.deleteStaffingProcessById(staffingProcessId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{staffingId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long staffingId) {
        List<CommentDTO> comments = commentService.findByStaffingProcessId(staffingId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{staffingId}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long staffingId,@RequestBody CommentDTO dto) throws ChangeSetPersister.NotFoundException {
        CommentDTO saved = commentService.addComment(staffingId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        service.setInactive(id);
        return ResponseEntity.noContent().build();
    }

}
