package com.attendance.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserInfo {
    private Integer id;
    private String username;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private String role;
    private Integer roleId;
    private List<String> permissions;
    private List<MenuTree> menus;
}
