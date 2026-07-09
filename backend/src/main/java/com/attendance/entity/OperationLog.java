package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 50)
    private String username;

    @Column(length = 100)
    private String operation;

    @Column(length = 10)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(length = 50)
    private String ip;

    private Integer duration;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}