package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.CommentDTO;
import org.db.hrsp.api.dto.StaffingProcessDTO;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.CommentService;
import org.db.hrsp.service.StaffingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staffing-processes")
@LogMethodExecution
@AllArgsConstructor
public class StaffingController {

    private final StaffingService service;
    private final CommentService commentService;

    @PostMapping()
    public StaffingProcessDTO addStaffingProcess(@RequestBody StaffingProcessDTO dto) {
        return service.createStaffingProcess(dto.getClient().getId(), dto.getEmployee().getId(), dto.getTitle());
    }

    @GetMapping("/{staffingProcessId}")
    public StaffingProcessDTO getStaffingProcess(@PathVariable("staffingProcessId") Long staffingProcessId) {
        return service.getStaffingProcess(staffingProcessId);
    }

    @GetMapping()
    public List<StaffingProcessDTO> getAllStaffingProcesses() {
        return service.getAllStaffingProcesses();
    }

    @PutMapping("/{staffingProcessId}")
    public StaffingProcessDTO updateEmployee(@RequestBody StaffingProcessDTO staffingProcess) {
        return service.updateStaffingProcess(staffingProcess);
    }

    @DeleteMapping("/{staffingProcessId}")
    public void deleteClient(@PathVariable("staffingProcessId") Long staffingProcessId) {
        service.deleteStaffingProcessById(staffingProcessId);
    }

    @GetMapping("/{staffingId}/comments")
    public List<CommentDTO> getComments(@PathVariable Long staffingId) {
        return commentService.findByStaffingProcessId(staffingId);
    }

    @PostMapping("/{staffingId}/comments")
    public CommentDTO addComment(@PathVariable Long staffingId, @RequestBody CommentDTO dto) {
        return commentService.addComment(staffingId, dto);
    }

    @PatchMapping("/{id}/complete")
    public void markAsCompleted(@PathVariable Long id) {
        service.setInactive(id);
    }

}
