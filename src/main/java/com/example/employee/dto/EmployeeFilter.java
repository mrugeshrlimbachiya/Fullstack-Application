package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// EmployeeFilter.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilter {
    private String name;
    private Integer minAge;
    private Integer maxAge;
    private String className;
    private String subject;
}