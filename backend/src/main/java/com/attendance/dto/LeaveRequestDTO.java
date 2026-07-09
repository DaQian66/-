package com.attendance.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    private Integer employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private Float duration;
    private String reason;
}
