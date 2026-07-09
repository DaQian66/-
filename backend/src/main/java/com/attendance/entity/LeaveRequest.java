package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "leave_type", nullable = false, length = 20)
    private String leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", length = 10)
    private String startTime;

    @Column(name = "end_time", length = 10)
    private String endTime;

    @Column(nullable = false)
    private Float duration;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 500)
    private String attachment;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(name = "approve_comment", length = 500)
    private String approveComment;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}