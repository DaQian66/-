package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "check_in_type", length = 20)
    private String checkInType = "PASSWORD";

    @Column(length = 20)
    private String status = "NORMAL";

    @Column(length = 500)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}