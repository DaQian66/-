package com.attendance.repository;

import com.attendance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmployeeNo(String employeeNo);
    Optional<Employee> findByUserId(Integer userId);
    List<Employee> findByDepartmentId(Integer departmentId);
    List<Employee> findByStatus(Integer status);
    List<Employee> findByRealNameContaining(String keyword);
}