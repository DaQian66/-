package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.entity.Employee;
import com.attendance.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired private EmployeeService employeeService;

    @GetMapping
    public ApiResponse<PageResult<?>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(employeeService.list(page, size, keyword, departmentId, status));
    }

    @GetMapping("/all")
    public ApiResponse<?> all() {
        return ApiResponse.success(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable Integer id) {
        return ApiResponse.success(employeeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('系统管理员','管理员','部门经理')")
    public ApiResponse<Employee> create(@RequestBody Employee employee) {
        return ApiResponse.success("新增员工成功", employeeService.create(employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('系统管理员','管理员','部门经理')")
    public ApiResponse<Employee> update(@PathVariable Integer id, @RequestBody Employee employee) {
        employee.setId(id);
        return ApiResponse.success("更新员工成功", employeeService.update(employee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('系统管理员')")
    public ApiResponse<?> delete(@PathVariable Integer id) {
        employeeService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/batch-status")
    @PreAuthorize("hasAnyAuthority('系统管理员','管理员','部门经理','ROLE_ADMIN','ROLE_MANAGER')")
    public ApiResponse<?> batchStatus(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) payload.get("ids");
        Integer status = (Integer) payload.get("status");
        int updated = employeeService.batchUpdateStatus(ids, status);
        return ApiResponse.success("批量状态更新成功", updated);
    }
}

