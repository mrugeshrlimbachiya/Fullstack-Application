package com.example.employee.resolver;

import com.example.employee.dto.EmployeeInput;
import com.example.employee.model.Employee;
import com.example.employee.model.Role;
import com.example.employee.model.User;
import com.example.employee.service.AuthService;
import com.example.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
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
public class EmployeeMutationResolver {

    private final EmployeeService employeeService;
    private final AuthService authService;

    @MutationMapping
    public Map<String, Object> login(@Argument String username, @Argument String password) {
        log.info("Mutation: login for user: {}", username);

        String token = authService.login(username, password);
        User user = authService.getUserByUsername(username);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole().name());
        if (user.getEmployee() != null) {
            userMap.put("employeeId", user.getEmployee().getId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);

        return response;
    }

    @MutationMapping
    public Map<String, Object> register(
            @Argument String username,
            @Argument String password,
            @Argument String role
    ) {
        log.info("Mutation: register for user: {}, role: {}", username, role);

        Role userRole = Role.valueOf(role);
        User user = authService.register(username, password, userRole);
        String token = authService.login(username, password);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);

        return response;
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> addEmployee(@Argument EmployeeInput input) {
        log.info("Mutation: addEmployee with name: {}", input.getName());

        Employee employee = employeeService.addEmployee(input);
        return mapEmployeeToResponse(employee);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Map<String, Object> updateEmployee(@Argument Long id, @Argument EmployeeInput input) {
        log.info("Mutation: updateEmployee with id: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee.getUser() == null || !employee.getUser().getUsername().equals(auth.getName())) {
                throw new RuntimeException("Access denied: You can only update your own employee profile");
            }
        }

        Employee employee = employeeService.updateEmployee(id, input);
        return mapEmployeeToResponse(employee);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteEmployee(@Argument Long id) {
        log.info("Mutation: deleteEmployee with id: {}", id);
        return employeeService.deleteEmployee(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Map<String, Object> markAttendance(
            @Argument Long employeeId,
            @Argument String date,
            @Argument Boolean present
    ) {
        log.info("Mutation: markAttendance for employee: {}, date: {}", employeeId, date);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee.getUser() == null || !employee.getUser().getUsername().equals(auth.getName())) {
                throw new RuntimeException("Access denied: You can only mark your own attendance");
            }
        }

        Employee employee = employeeService.markAttendance(employeeId, date, present);
        return mapEmployeeToResponse(employee);
    }

    private Map<String, Object> mapEmployeeToResponse(Employee employee) {
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("id", employee.getId());
        employeeMap.put("name", employee.getName());
        employeeMap.put("age", employee.getAge());
        employeeMap.put("class", employee.getClassName());
        employeeMap.put("subjects", employee.getSubjects());
        employeeMap.put("email", employee.getEmail());
        employeeMap.put("phone", employee.getPhone());
        employeeMap.put("createdAt", employee.getCreatedAt().toString());
        employeeMap.put("updatedAt", employee.getUpdatedAt().toString());

        List<Map<String, Object>> attendance = employee.getAttendance().entrySet().stream()
                .map(entry -> {
                    Map<String, Object> record = new HashMap<>();
                    record.put("date", entry.getKey());
                    record.put("present", entry.getValue());
                    return record;
                })
                .collect(Collectors.toList());

        employeeMap.put("attendance", attendance);

        return employeeMap;
    }
}