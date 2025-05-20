package org.db.hrsp.graphql;

import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.service.EmployeeService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class EmployeeGraphQLController {
    private final EmployeeService employeeService;

    public EmployeeGraphQLController(EmployeeService EmployeeService) {
        this.employeeService = EmployeeService;
    }

    @QueryMapping
    public EmployeeDTO getEmployee(@Argument Long id) {
        return employeeService.getEmployee(id);
    }

    @QueryMapping
    public List<EmployeeDTO> getEmployees() {
        return employeeService.getAllEmployees();
    }


}
