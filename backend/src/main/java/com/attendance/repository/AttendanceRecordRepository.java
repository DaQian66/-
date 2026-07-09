package com.attendance.repository;
import com.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Integer employeeId, LocalDate date);
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetween(Integer employeeId, LocalDate start, LocalDate end);
    List<AttendanceRecord> findByAttendanceDate(LocalDate date);
    List<AttendanceRecord> findByAttendanceDateBetween(LocalDate start, LocalDate end);
    List<AttendanceRecord> findByEmployeeId(Integer employeeId);
}
