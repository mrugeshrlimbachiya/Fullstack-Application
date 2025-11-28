package com.example.employee.resolver;

import com.example.employee.model.Employee;
import com.example.employee.model.User;
import com.example.employee.service.AuthService;
import com.example.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EmployeeQueryResolver {

    private final EmployeeService employeeService;
    private final AuthService authService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Map<String, Object> employees(
            @Argument Map<String, Object> filter,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument String sortDir
    ) {
        log.info("Query: employees with filter: {}, page: {}, size: {}", filter, page, size);

        // Method name matches the service: getAllEmployees
        Page<Employee> employeePage = employeeService.getAllEmployees(filter, page, size, sortBy, sortDir);

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", employeePage.getNumber());
        pageInfo.put("pageSize", employeePage.getSize());
        pageInfo.put("totalElements", employeePage.getTotalElements());
        pageInfo.put("totalPages", employeePage.getTotalPages());
        pageInfo.put("hasNext", employeePage.hasNext());
        pageInfo.put("hasPrevious", employeePage.hasPrevious());

        List<Map<String, Object>> content = employeePage.getContent().stream()
                .map(this::mapEmployeeToResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("pageInfo", pageInfo);

        return result;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Map<String, Object> employee(@Argument Long id) {
        log.info("Query: employee with id: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Employee employee = employeeService.getEmployeeById(id);

        if (!isAdmin && employee.getUser() != null) {
            String currentUsername = auth.getName();
            if (!employee.getUser().getUsername().equals(currentUsername)) {
                throw new RuntimeException("Access denied: You can only view your own employee profile");
            }
        }

        return mapEmployeeToResponse(employee);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Map<String, Object> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Query: me for user: {}", username);

        User user = authService.getUserByUsername(username);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole().name());
        if (user.getEmployee() != null) {
            userMap.put("employeeId", user.getEmployee().getId());
        }

        return userMap;
    }

    private Map<String, Object> mapEmployeeToResponse(Employee employee) {
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("id", employee.getId());
        employeeMap.put("name", employee.getName());
        employeeMap.put("age", employee.getAge());
        employeeMap.put("className", employee.getClassName());
        employeeMap.put("subjects", employee.getSubjects());
        employeeMap.put("email", employee.getEmail());
        employeeMap.put("phone", employee.getPhone());

        // Handling null timestamps gracefully, as we did in the Mutation Resolver
        employeeMap.put("createdAt", employee.getCreatedAt() != null ? employee.getCreatedAt().toString() : null);
        employeeMap.put("updatedAt", employee.getUpdatedAt() != null ? employee.getUpdatedAt().toString() : null);

        // FIX: The Employee model now has a getAttendance() method that returns
        // List<AttendanceRecord>. We rely on Spring GraphQL to serialize this List
        // into the required List of Maps.
        employeeMap.put("attendance", employee.getAttendance());

        return employeeMap;
    }
}