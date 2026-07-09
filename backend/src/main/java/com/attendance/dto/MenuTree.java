package com.attendance.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuTree {
    private Integer id;
    private Integer parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sortOrder;
    private List<MenuTree> children;
}
