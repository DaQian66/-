package com.attendance.service;

import com.attendance.entity.*;
import com.attendance.repository.*;
import com.attendance.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;

    /** 员工提交请假申请 */
    public LeaveRequest submit(LeaveRequestDTO dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(dto.getEmployeeId());
        lr.setLeaveType(dto.getLeaveType());
        lr.setStartDate(dto.getStartDate());
        lr.setEndDate(dto.getEndDate());
        lr.setStartTime(dto.getStartTime());
        lr.setEndTime(dto.getEndTime());
        lr.setDuration(dto.getDuration());
        lr.setReason(dto.getReason());
        lr.setStatus("PENDING");
        lr.setCreatedAt(LocalDateTime.now());
        lr.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(lr);
    }

    /** 审批请假 */
    @Transactional
    public LeaveRequest approve(ApproveRequest req, Integer approverId) {
        LeaveRequest lr = leaveRepository.findById(req.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("请假申请不存在"));
        if (!"PENDING".equals(lr.getStatus())) {
            throw new IllegalArgumentException("该申请已审批过");
        }
        lr.setStatus(req.getStatus());
        lr.setApproverId(approverId);
        lr.setApproveComment(req.getComment());
        lr.setApproveTime(LocalDateTime.now());
        lr.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(lr);
    }

    /** 查询请假列表 */
    public PageResult<Map<String, Object>> list(Integer employeeId, String status, int page, int size) {
        List<LeaveRequest> all;
        if (employeeId != null) {
            all = leaveRepository.findByEmployeeId(employeeId);
        } else if (status != null && !status.isEmpty()) {
            all = leaveRepository.findByStatus(status);
        } else {
            all = leaveRepository.findAll();
        }

        // sort desc
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        all = all.subList(Math.min(from, total), to);

        List<Map<String, Object>> list = all.stream().map(lr -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", lr.getId());
            m.put("leaveType", lr.getLeaveType());
            m.put("startDate", lr.getStartDate());
            m.put("endDate", lr.getEndDate());
            m.put("duration", lr.getDuration());
            m.put("reason", lr.getReason());
            m.put("status", lr.getStatus());
            m.put("approveComment", lr.getApproveComment());
            m.put("approveTime", lr.getApproveTime());
            m.put("createdAt", lr.getCreatedAt());

            Employee emp = employeeRepository.findById(lr.getEmployeeId()).orElse(null);
            m.put("employeeId", lr.getEmployeeId());
            m.put("employeeName", emp != null ? emp.getRealName() : "-");
            m.put("employeeNo", emp != null ? emp.getEmployeeNo() : "-");

            if (lr.getApproverId() != null) {
                User approver = userRepository.findById(lr.getApproverId()).orElse(null);
                m.put("approverName", approver != null ? approver.getRealName() : "-");
            } else {
                m.put("approverName", "-");
            }
            return m;
        }).collect(Collectors.toList());

        return new PageResult<>((long) total, page, size, list);
    }
}