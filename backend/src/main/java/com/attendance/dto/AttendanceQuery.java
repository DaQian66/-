package com.attendance.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceQuery {
    private Integer employeeId;
    private Integer departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int page = 1;
    private int size = 10;
}
