package org.db.hrsp.api.controller;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
@LogMethodExecution
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping()
    public EmployeeDTO addEmployee(@RequestParam String name) {
        return service.createEmployee(name);
    }

    @GetMapping("/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable("employeeId") Long employeeId) {
        return service.getEmployee(employeeId);
    }

    @GetMapping()
    public List<EmployeeDTO> getAllEmployees() {
        return service.getAllEmployees();
    }
}
