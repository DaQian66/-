package com.attendance.service;

import com.attendance.entity.*;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DepartmentService {

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private EmployeeRepository employeeRepository;

    public List<Department> listAll() {
        return departmentRepository.findAll();
    }

    public List<Map<String, Object>> listWithCounts() {
        List<Department> depts = departmentRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Department dept : depts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dept.getId());
            map.put("name", dept.getName());
            map.put("parentId", dept.getParentId());
            map.put("description", dept.getDescription());
            List<Employee> emps = employeeRepository.findByDepartmentId(dept.getId());
            map.put("employeeCount", emps.size());
            result.add(map);
        }
        return result;
    }

    public Department create(Department dept) {
        dept.setCreatedAt(LocalDateTime.now());
        return departmentRepository.save(dept);
    }

    public Department update(Department dept) {
        Department exist = departmentRepository.findById(dept.getId())
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        exist.setName(dept.getName());
        exist.setParentId(dept.getParentId());
        exist.setDescription(dept.getDescription());
        exist.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(exist);
    }

    public void delete(Integer id) {
        if (!employeeRepository.findByDepartmentId(id).isEmpty()) {
            throw new IllegalArgumentException("该部门下有员工，无法删除");
        }
        departmentRepository.deleteById(id);
    }

    public List<Department> buildTree() {
        List<Department> all = departmentRepository.findAll();
        Map<Integer, List<Department>> parentMap = new HashMap<>();
        for (Department d : all) {
            int pid = d.getParentId() != null ? d.getParentId() : 0;
            parentMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(d);
        }
        List<Department> roots = parentMap.getOrDefault(0, new ArrayList<>());
        for (Department d : all) {
            List<Department> children = parentMap.getOrDefault(d.getId(), new ArrayList<>());
        }
        return roots;
    }
}