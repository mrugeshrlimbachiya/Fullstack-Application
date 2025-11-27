package com.example.employee.service;

import com.example.employee.dto.EmployeeInput;
import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Cacheable(value = "employees", key = "#id")
    public Employee getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);
        return employeeRepository.findByIdWithSubjects(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Page<Employee> getAllEmployees(Map<String, Object> filter, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching employees with filter: {}, page: {}, size: {}", filter, page, size);

        Sort sort = sortDir.equalsIgnoreCase("DESC") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (filter == null || filter.isEmpty()) {
            return employeeRepository.findAll(pageable);
        }

        Specification<Employee> spec = createSpecification(filter);
        return employeeRepository.findAll(spec, pageable);
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public Employee addEmployee(EmployeeInput input) {
        log.info("Adding new employee: {}", input.getName());

        Employee employee = Employee.builder()
                .name(input.getName())
                .age(input.getAge())
                .className(input.getClassName())
                .subjects(input.getSubjects())
                .email(input.getEmail())
                .phone(input.getPhone())
                .build();

        return employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public Employee updateEmployee(Long id, EmployeeInput input) {
        log.info("Updating employee with id: {}", id);

        Employee employee = getEmployeeById(id);

        employee.setName(input.getName());
        employee.setAge(input.getAge());
        employee.setClassName(input.getClassName());
        employee.setSubjects(input.getSubjects());
        employee.setEmail(input.getEmail());
        employee.setPhone(input.getPhone());

        return employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public boolean deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }

        employeeRepository.deleteById(id);
        return true;
    }

    @Transactional
    @CacheEvict(value = "employees", key = "#employeeId")
    public Employee markAttendance(Long employeeId, String date, Boolean present) {
        log.info("Marking attendance for employee: {}, date: {}, present: {}", employeeId, date, present);

        Employee employee = getEmployeeById(employeeId);
        employee.getAttendance().put(date, present);

        return employeeRepository.save(employee);
    }

    private Specification<Employee> createSpecification(Map<String, Object> filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.containsKey("name")) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.get("name").toString().toLowerCase() + "%"
                ));
            }

            if (filter.containsKey("minAge")) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("age"),
                        Integer.parseInt(filter.get("minAge").toString())
                ));
            }

            if (filter.containsKey("maxAge")) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("age"),
                        Integer.parseInt(filter.get("maxAge").toString())
                ));
            }

            if (filter.containsKey("className")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("className"),
                        filter.get("className")
                ));
            }

            if (filter.containsKey("subject")) {
                // Note: This requires a join to the subjects collection
                predicates.add(criteriaBuilder.isMember(
                        filter.get("subject").toString(),
                        root.get("subjects")
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}