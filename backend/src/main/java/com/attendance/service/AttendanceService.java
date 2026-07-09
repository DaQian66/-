package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.*;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired private AttendanceRecordRepository attendanceRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private FaceRecognitionService faceRecognitionService;

    /** 员工打卡签到/签退 */
    @Transactional
    public Map<String, Object> checkIn(CheckInRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        Optional<AttendanceRecord> existOpt = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), today);

        AttendanceRecord record;
        if (existOpt.isPresent()) {
            record = existOpt.get();
            if (record.getCheckOutTime() != null) {
                throw new IllegalArgumentException("今日已完成签到签退，请勿重复操作");
            }
            // 签退
            record.setCheckOutTime(now);
            record.setRemark("签退成功");
        } else {
            record = new AttendanceRecord();
            record.setEmployeeId(request.getEmployeeId());
            record.setAttendanceDate(today);
            record.setCheckInTime(now);
            record.setCheckInType(request.getType() != null ? request.getType() : "PASSWORD");

            // 迟到判定：8:30之后
            if (now.isAfter(LocalTime.of(8, 30))) {
                record.setStatus("LATE");
                record.setRemark("迟到打卡");
            } else {
                record.setStatus("NORMAL");
                record.setRemark("签到成功");
            }
        }
        AttendanceRecord saved = attendanceRepository.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        result.put("checkInTime", saved.getCheckInTime());
        result.put("checkOutTime", saved.getCheckOutTime());
        result.put("remark", saved.getRemark());
        result.put("type", saved.getCheckInType());
        return result;
    }

    /** 查询打卡记录 */
    public PageResult<Map<String, Object>> queryRecords(AttendanceQuery query) {
        List<AttendanceRecord> records;
        if (query.getEmployeeId() != null && query.getStartDate() != null && query.getEndDate() != null) {
            records = attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(
                    query.getEmployeeId(), query.getStartDate(), query.getEndDate());
        } else if (query.getStartDate() != null && query.getEndDate() != null) {
            records = attendanceRepository.findByAttendanceDateBetween(query.getStartDate(), query.getEndDate());
        } else if (query.getEmployeeId() != null) {
            records = attendanceRepository.findByEmployeeId(query.getEmployeeId());
        } else {
            records = attendanceRepository.findAll();
        }

        // filter
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            records = records.stream().filter(r -> query.getStatus().equals(r.getStatus()))
                    .collect(Collectors.toList());
        }

        if (query.getDepartmentId() != null) {
            List<Integer> deptEmpIds = employeeRepository.findByDepartmentId(query.getDepartmentId())
                    .stream().map(Employee::getId).collect(Collectors.toList());
            records = records.stream().filter(r -> deptEmpIds.contains(r.getEmployeeId()))
                    .collect(Collectors.toList());
        }

        // sort desc
        records.sort((a, b) -> b.getAttendanceDate().compareTo(a.getAttendanceDate()));

        int total = records.size();
        int from = (query.getPage() - 1) * query.getSize();
        int to = Math.min(from + query.getSize(), total);
        records = records.subList(Math.min(from, total), to);

        List<Map<String, Object>> list = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("attendanceDate", r.getAttendanceDate());
            m.put("checkInTime", r.getCheckInTime());
            m.put("checkOutTime", r.getCheckOutTime());
            m.put("checkInType", r.getCheckInType());
            m.put("status", r.getStatus());
            m.put("remark", r.getRemark());
            Employee emp = employeeRepository.findById(r.getEmployeeId()).orElse(null);
            m.put("employeeId", r.getEmployeeId());
            m.put("employeeName", emp != null ? emp.getRealName() : "-");
            m.put("employeeNo", emp != null ? emp.getEmployeeNo() : "-");
            if (emp != null && emp.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(emp.getDepartmentId()).orElse(null);
                m.put("departmentName", dept != null ? dept.getName() : "-");
            } else {
                m.put("departmentName", "-");
            }
            return m;
        }).collect(Collectors.toList());

        return new PageResult<>((long) total, query.getPage(), query.getSize(), list);
    }

    public String exportCsv(AttendanceQuery query) {
        int originalPage = query.getPage();
        int originalSize = query.getSize();
        query.setPage(1);
        query.setSize(Integer.MAX_VALUE);
        PageResult<Map<String, Object>> result = queryRecords(query);
        query.setPage(originalPage);
        query.setSize(originalSize);

        StringBuilder csv = new StringBuilder();
        csv.append("员工,工号,部门,日期,签到时间,签退时间,打卡方式,状态,备注\n");
        for (Map<String, Object> row : result.getList()) {
            csv.append(csvValue(row.get("employeeName"))).append(',')
                    .append(csvValue(row.get("employeeNo"))).append(',')
                    .append(csvValue(row.get("departmentName"))).append(',')
                    .append(csvValue(row.get("attendanceDate"))).append(',')
                    .append(csvValue(row.get("checkInTime"))).append(',')
                    .append(csvValue(row.get("checkOutTime"))).append(',')
                    .append(csvValue(row.get("checkInType"))).append(',')
                    .append(csvValue(row.get("status"))).append(',')
                    .append(csvValue(row.get("remark"))).append('\n');
        }
        return csv.toString();
    }

    private String csvValue(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    /** 考勤统计数据 */
    public StatisticsDTO getStatistics() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus(1);
        List<AttendanceRecord> todayRecords = attendanceRepository.findByAttendanceDate(today);

        int totalEmp = activeEmployees.size();
        int checkInCount = (int) todayRecords.stream().filter(r -> r.getCheckInTime() != null).count();
        int lateCount = (int) todayRecords.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        int absentCount = totalEmp - checkInCount;
        int pendingLeaves = leaveRepository.findByStatus("PENDING").size();

        // 出勤人数 = 已签到的
        int onDuty = checkInCount;
        double rate = totalEmp > 0 ? (double) onDuty / totalEmp * 100 : 0;

        // 各部门出勤统计
        List<ChartData> deptAttendance = new ArrayList<>();
        List<Department> depts = departmentRepository.findAll();
        for (Department dept : depts) {
            List<Employee> deptEmps = employeeRepository.findByDepartmentId(dept.getId());
            if (deptEmps.isEmpty()) continue;
            long deptCheckedIn = todayRecords.stream()
                    .filter(r -> deptEmps.stream().anyMatch(e -> e.getId().equals(r.getEmployeeId()))
                            && r.getCheckInTime() != null)
                    .count();
            deptAttendance.add(new ChartData(dept.getName(),
                    Math.round(deptCheckedIn * 100.0 / deptEmps.size())));
        }

        // 近7天趋势
        List<ChartData> dailyTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<AttendanceRecord> dayRecords = attendanceRepository.findByAttendanceDate(date);
            long checkedIn = dayRecords.stream().filter(r -> r.getCheckInTime() != null).count();
            double dayRate = totalEmp > 0 ? (double) checkedIn / totalEmp * 100 : 0;
            dailyTrend.add(new ChartData(date.format(fmt), Math.round(dayRate)));
        }

        StatisticsDTO dto = new StatisticsDTO();
        dto.setTotalEmployees(totalEmp);
        dto.setTodayCheckIn(checkInCount);
        dto.setTodayAbsent(absentCount);
        dto.setTodayLate(lateCount);
        dto.setPendingLeaves(pendingLeaves);
        dto.setOnDuty(onDuty);
        dto.setAttendanceRate(Math.round(rate * 10.0) / 10.0);
        dto.setDepartmentAttendance(deptAttendance);
        dto.setDailyTrend(dailyTrend);
        return dto;
    }

    /** 人脸识别打卡 - 真实人脸比对 */
    public Map<String, Object> faceCheckIn(Integer employeeId, String faceData) {
        // 1. 校验 faceData 不为空
        if (faceData == null || faceData.isEmpty()) {
            throw new IllegalArgumentException("未收到人脸图像数据，请重新拍摄");
        }

        // 2. 校验员工存在且有注册人脸
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        if (emp.getFaceImage() == null || emp.getFaceImage().isEmpty()) {
            throw new IllegalArgumentException("您尚未注册人脸信息，请联系管理员上传人脸照片");
        }

        // 3. 调用人脸识别服务验证
        FaceRecognitionService.FaceMatchResult matchResult =
                faceRecognitionService.verifyFace(employeeId, faceData);

        // 4. 未匹配 → 返回失败，不执行签到
        if (!matchResult.isMatched()) {
            Map<String, Object> failResult = new HashMap<>();
            failResult.put("matched", false);
            failResult.put("confidence", Math.round(matchResult.getConfidence()));
            failResult.put("message", matchResult.getMessage());
            return failResult;
        }

        // 5. 匹配成功 → 执行签到
        CheckInRequest req = new CheckInRequest();
        req.setEmployeeId(employeeId);
        req.setType("FACE");

        Map<String, Object> result = checkIn(req);
        result.put("confidence", Math.round(matchResult.getConfidence()));
        result.put("matched", true);
        result.put("message", "人脸识别成功，置信度: " + Math.round(matchResult.getConfidence()) + "%");
        return result;
    }

    /** 今日打卡状态查询 */
    public Map<String, Object> todayStatus(Integer employeeId) {
        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> opt = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today);
        Map<String, Object> result = new HashMap<>();
        result.put("checkedIn", opt.isPresent() && opt.get().getCheckInTime() != null);
        result.put("checkedOut", opt.isPresent() && opt.get().getCheckOutTime() != null);
        if (opt.isPresent()) {
            result.put("checkInTime", opt.get().getCheckInTime());
            result.put("checkOutTime", opt.get().getCheckOutTime());
            result.put("status", opt.get().getStatus());
        }
        return result;
    }
}
