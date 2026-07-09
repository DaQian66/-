package com.attendance.dto;

import lombok.Data;

@Data
public class CheckInRequest {
    private Integer employeeId;
    private String type;
    private String faceData;
}
