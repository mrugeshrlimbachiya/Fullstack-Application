package com.example.employee.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Needed for the new getter

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_name", columnList = "name"),
        @Index(name = "idx_employee_class", columnList = "class_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Builder.Default // Added @Builder.Default back for safety
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_subjects", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "subject")
    private List<String> subjects = new ArrayList<>();

    // RENAMED FIELD: Changed 'attendance' to 'attendanceData' to avoid conflict
    // with the custom getter and reflect that this is the raw map data.
    @Builder.Default // Added @Builder.Default back for safety
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employee_attendance", joinColumns = @JoinColumn(name = "employee_id"))
    @MapKeyColumn(name = "attendance_date")
    @Column(name = "present")
    private Map<String, Boolean> attendanceData = new HashMap<>();

    @Column(unique = true)
    private String email;

    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    // --- START: FIX FOR GRAPHQL ATTENDANCE MAPPING ---

    // Custom getter: This method name (getAttendance) matches the GraphQL field.
    // It converts the internal Map to the required List of AttendanceRecord objects.
    public List<AttendanceRecord> getAttendance() {
        if (attendanceData == null) return new ArrayList<>();

        return attendanceData.entrySet().stream()
                .map(entry -> new AttendanceRecord(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // Expose the raw map data for use in service logic (like markAttendance)
    public Map<String, Boolean> getAttendanceData() {
        return attendanceData;
    }

    // --- END: FIX FOR GRAPHQL ATTENDANCE MAPPING ---

    // Helper method to avoid circular reference in JSON
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.setEmployee(this);
        }
    }
}