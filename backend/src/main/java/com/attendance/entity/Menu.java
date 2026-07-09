package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "parent_id")
    private Integer parentId = 0;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String path;

    @Column(length = 200)
    private String component;

    @Column(length = 50)
    private String icon;

    @Column(length = 10)
    private String type = "menu";

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 100)
    private String permission;

    @Column(name = "visible")
    private Integer visible = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}