package com.attendance.dto;

import lombok.Data;

@Data
public class ApproveRequest {
    private Integer requestId;
    private String status;
    private String comment;
}
