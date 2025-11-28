package com.example.employee.model;

/**
 * Simple record/class to mirror the GraphQL type AttendanceRecord
 * (date: String!, present: Boolean!).
 */
public record AttendanceRecord(String date, Boolean present) {}