package com.attendance.repository;
import com.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByEmployeeId(Integer employeeId);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByStatusIn(List<String> statuses);
}
