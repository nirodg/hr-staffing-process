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

@Controller
@RequestMapping("/api/employees")
@AllArgsConstructor
@LogMethodExecution
public class EmployeeController {

    private final EmployeeService service;

    @Transactional
    @PostMapping()
    public ResponseEntity<EmployeeDTO> addEmployee(@RequestParam String name) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEmployee(name));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable("employeeId") Long employeeId) {
        EmployeeDTO employee = service.getEmployee(employeeId);

        return employee.getId() != null
                ? ResponseEntity.ok(employee)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping()
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = service.getAllEmployees();
        if (employees.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employees);
    }
}
