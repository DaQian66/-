package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.entity.Department;
import com.attendance.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired private DepartmentService departmentService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(departmentService.listWithCounts());
    }

    @GetMapping("/tree")
    public ApiResponse<?> tree() {
        return ApiResponse.success(departmentService.buildTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Department> create(@RequestBody Department dept) {
        return ApiResponse.success("新增部门成功", departmentService.create(dept));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Department> update(@PathVariable Integer id, @RequestBody Department dept) {
        dept.setId(id);
        return ApiResponse.success("更新部门成功", departmentService.update(dept));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<?> delete(@PathVariable Integer id) {
        departmentService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}