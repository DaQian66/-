package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmployeeService;
import com.attendance.entity.Employee;
import com.attendance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceService attendanceService;
    @Autowired private EmployeeService employeeService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/check-in")
    public ApiResponse<Map<String, Object>> checkIn(@RequestBody CheckInRequest request,
                                                     HttpServletRequest httpRequest) {
        // 密码打卡已禁用，必须使用人脸识别
        throw new IllegalArgumentException("密码打卡已关闭，请使用人脸识别打卡");
    }

    @PostMapping("/face-check-in")
    public ApiResponse<Map<String, Object>> faceCheckIn(@RequestBody CheckInRequest request,
                                                         HttpServletRequest httpRequest) {
        String token = getToken(httpRequest);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (request.getEmployeeId() == null) {
            Employee emp = employeeService.findByUserId(userId);
            if (emp == null) throw new IllegalArgumentException("请先完善员工档案");
            request.setEmployeeId(emp.getId());
        }
        request.setType("FACE");
        return ApiResponse.success(attendanceService.faceCheckIn(request.getEmployeeId(), request.getFaceData()));
    }

    @PostMapping("/records")
    public ApiResponse<PageResult<Map<String, Object>>> records(@RequestBody AttendanceQuery query,
                                                                 HttpServletRequest httpRequest) {
        // 非管理员只能查看自己的考勤记录
        String token = getToken(httpRequest);
        String role = jwtUtil.getRoleFromToken(token);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (role != null && !role.contains("管理员") && !role.contains("经理")) {
            Employee emp = employeeService.findByUserId(userId);
            if (emp != null) {
                query.setEmployeeId(emp.getId());
            }
        }
        return ApiResponse.success(attendanceService.queryRecords(query));
    }

    @GetMapping("/today-status")
    public ApiResponse<Map<String, Object>> todayStatus(HttpServletRequest request) {
        String token = getToken(request);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        Employee emp = employeeService.findByUserId(userId);
        if (emp == null) throw new IllegalArgumentException("请先完善员工档案");
        return ApiResponse.success(attendanceService.todayStatus(emp.getId()));
    }

    @GetMapping("/statistics")
    public ApiResponse<StatisticsDTO> statistics() {
        return ApiResponse.success(attendanceService.getStatistics());
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) Integer departmentId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) LocalDate startDate,
                       @RequestParam(required = false) LocalDate endDate,
                       HttpServletRequest httpRequest,
                       HttpServletResponse response) throws IOException {
        AttendanceQuery query = new AttendanceQuery();
        query.setDepartmentId(departmentId);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate);

        String token = getToken(httpRequest);
        String role = jwtUtil.getRoleFromToken(token);
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (role != null && !role.contains("管理员") && !role.contains("经理")
                && !role.startsWith("ROLE_ADMIN") && !role.startsWith("ROLE_MANAGER")) {
            Employee emp = employeeService.findByUserId(userId);
            if (emp != null) {
                query.setEmployeeId(emp.getId());
            }
        }

        String csv = "\uFEFF" + attendanceService.exportCsv(query);
        String fileName = URLEncoder.encode("attendance-records.csv", StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.getWriter().write(csv);
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeader());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            return token.substring(jwtUtil.getTokenPrefix().length());
        }
        return token;
    }
}
