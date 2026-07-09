package com.attendance.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "role_menus")
public class RoleMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "menu_id", nullable = false)
    private Integer menuId;
}