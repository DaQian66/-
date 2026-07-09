package com.attendance.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    private Integer totalEmployees;
    private Integer todayCheckIn;
    private Integer todayAbsent;
    private Integer todayLate;
    private Integer pendingLeaves;
    private Integer onDuty;
    private Double attendanceRate;
    private List<ChartData> departmentAttendance;
    private List<ChartData> dailyTrend;
}
