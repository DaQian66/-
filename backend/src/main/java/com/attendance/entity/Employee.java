package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(length = 4)
    private String gender;

    private LocalDate birthday;

    @Column(name = "id_card", length = 18)
    private String idCard;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(length = 50)
    private String position;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "face_image", length = 500)
    private String faceImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient
    private String username;

    @Transient
    private String password;
}