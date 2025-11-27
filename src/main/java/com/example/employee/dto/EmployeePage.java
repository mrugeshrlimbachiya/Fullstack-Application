package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// EmployeePage.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePage {
    private List<Object> content;
    private PageInfo pageInfo;
}