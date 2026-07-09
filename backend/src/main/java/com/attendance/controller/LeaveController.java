package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.entity.Employee;
import com.attendance.service.LeaveService;
import com.attendance.service.EmployeeService;
import com.attendance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired private LeaveService leaveService;
    @Autowired private EmployeeService employeeService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/submit")
    public ApiResponse<?> submit(@RequestBody LeaveRequestDTO dto, HttpServletRequest request) {
        String token = getToken(request);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (dto.getEmployeeId() == null) {
            Employee emp = employeeService.findByUserId(userId);
            if (emp == null) throw new IllegalArgumentException("请先完善员工档案");
            dto.setEmployeeId(emp.getId());
        }
        return ApiResponse.success("请假申请已提交", leaveService.submit(dto));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyAuthority('系统管理员','管理员','部门经理')")
    public ApiResponse<?> approve(@RequestBody ApproveRequest req, HttpServletRequest request) {
        String token = getToken(request);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        return ApiResponse.success("审批完成", leaveService.approve(req, userId));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<?>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        // 非管理员只能查看自己的请假记录
        String token = getToken(httpRequest);
        String role = jwtUtil.getRoleFromToken(token);
        if (role != null && !role.contains("管理员") && !role.contains("经理")) {
            Integer userId = jwtUtil.getUserIdFromToken(token);
            Employee emp = employeeService.findByUserId(userId);
            if (emp != null) {
                employeeId = emp.getId();
            }
        }
        return ApiResponse.success(leaveService.list(employeeId, status, page, size));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('系统管理员','管理员','部门经理')")
    public ApiResponse<PageResult<?>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(leaveService.list(null, "PENDING", page, size));
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeader());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            return token.substring(jwtUtil.getTokenPrefix().length());
        }
        return token;
    }
}