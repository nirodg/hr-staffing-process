package org.db.hrsp.service;


import org.db.hrsp.dto.EmployeeDTO;
import org.db.hrsp.model.Employee;
import org.db.hrsp.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public EmployeeDTO createEmployee(String name) {
        Employee employee = new Employee(name, true);
        repository.save(employee);
        logger.info("Employee created successfully with ID: {}", employee.getId());
        return convertEmployeeToDTO(employee);
    }

    public EmployeeDTO getEmployee(Long employeeId) {
        Employee employee = repository.findById(employeeId).orElseThrow();
        logger.info("Employee found with ID: {}", employee.getId());
        return convertEmployeeToDTO(employee);
    }

    public List<EmployeeDTO> getAllEmployees() {
        logger.info("Get all employees");
        return Streamable.of(repository.findAll()).toList().stream().map(this::convertEmployeeToDTO).toList();
    }

    public EmployeeDTO updateEmployee(EmployeeDTO employee) {
        Employee employeeToUpdate = repository.findById(employee.getId()).orElseThrow();
        employeeToUpdate.setAvailable(employee.isAvailable());
        employeeToUpdate.setName(employee.getName());
        employeeToUpdate = repository.save(employeeToUpdate);

        return convertEmployeeToDTO(employeeToUpdate);
    }

    public void deleteEmployeeById(Long employeeId) {
        logger.info("Delete employee with ID: {}", employeeId);
        repository.deleteById(employeeId);
    }

    private EmployeeDTO convertEmployeeToDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setName(employee.getName());
        employeeDTO.setAvailable(employee.isAvailable());
        employeeDTO.setStaffingProcesses(employee.getStaffingProcesses());
        return employeeDTO;
    }

}
