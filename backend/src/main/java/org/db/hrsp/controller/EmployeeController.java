package org.db.hrsp.controller;


import org.db.hrsp.dto.EmployeeDTO;
import org.db.hrsp.service.EmployeeService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

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

    @Transactional
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@RequestBody EmployeeDTO employee) {
        return ResponseEntity.ok(service.updateEmployee(employee));
    }

    @Transactional
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("employeeId") Long employeeId) {
        service.deleteEmployeeById(employeeId);
        return ResponseEntity.noContent().build();
    }
}
