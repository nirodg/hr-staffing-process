package org.db.hrsp.service;


import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.mapper.EmployeeMapper;
import org.db.hrsp.service.repository.model.Employee;
import org.db.hrsp.service.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;

    public EmployeeDTO createEmployee(String name) {
        Employee employee = new Employee(name, true);
        repository.save(employee);
        return employeeMapper.toDto(employee);
    }

    public EmployeeDTO getEmployee(Long employeeId) {
        Employee employee = repository.findById(employeeId).orElseThrow();
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.toDtos(repository.findAll());
    }

}
