package com.attendance.service;

import com.attendance.entity.Employee;
import com.attendance.entity.Department;
import com.attendance.entity.User;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.UserRepository;
import com.attendance.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    public PageResult<Map<String, Object>> list(int page, int size, String keyword, Integer departmentId, Integer status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);
        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(cb.or(
                cb.like(root.get("realName"), "%" + keyword + "%"),
                cb.like(root.get("employeeNo"), "%" + keyword + "%")
            ));
        }
        if (departmentId != null) {
            predicates.add(cb.equal(root.get("departmentId"), departmentId));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("id")));

        TypedQuery<Employee> query = entityManager.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        List<Employee> employees = query.getResultList();

        // count
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Employee> countRoot = countCq.from(Employee.class);
        countCq.select(cb.count(countRoot));
        List<Predicate> countPredicates = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            countPredicates.add(cb.or(
                cb.like(countRoot.get("realName"), "%" + keyword + "%"),
                cb.like(countRoot.get("employeeNo"), "%" + keyword + "%")
            ));
        }
        if (departmentId != null) countPredicates.add(cb.equal(countRoot.get("departmentId"), departmentId));
        if (status != null) countPredicates.add(cb.equal(countRoot.get("status"), status));
        countCq.where(countPredicates.toArray(new Predicate[0]));
        long total = entityManager.createQuery(countCq).getSingleResult();

        // enrich with department name
        List<Map<String, Object>> list = new ArrayList<>();
        for (Employee emp : employees) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", emp.getId());
            map.put("employeeNo", emp.getEmployeeNo());
            map.put("realName", emp.getRealName());
            map.put("gender", emp.getGender());
            map.put("phone", emp.getPhone());
            map.put("email", emp.getEmail());
            map.put("departmentId", emp.getDepartmentId());
            map.put("position", emp.getPosition());
            map.put("entryDate", emp.getEntryDate());
            map.put("status", emp.getStatus());
            map.put("faceImage", emp.getFaceImage());
            Department dept = emp.getDepartmentId() != null ?
                    departmentRepository.findById(emp.getDepartmentId()).orElse(null) : null;
            map.put("departmentName", dept != null ? dept.getName() : "-");
            list.add(map);
        }

        return new PageResult<>(total, page, size, list);
    }

    @Transactional
    public Employee create(Employee employee) {
        // 如果提供了登录账号，先创建 User
        if (employee.getUsername() != null && !employee.getUsername().isEmpty()
                && employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            if (userRepository.existsByUsername(employee.getUsername())) {
                throw new IllegalArgumentException("登录账号 [" + employee.getUsername() + "] 已存在");
            }
            User user = new User();
            user.setUsername(employee.getUsername());
            user.setPassword(passwordEncoder.encode(employee.getPassword()));
            user.setRealName(employee.getRealName());
            user.setEmail(employee.getEmail());
            user.setPhone(employee.getPhone());
            user.setDepartmentId(employee.getDepartmentId());
            user.setRoleId(3); // 员工角色
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            employee.setUserId(user.getId());
        }
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    public Employee update(Employee employee) {
        Employee exist = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        exist.setRealName(employee.getRealName());
        exist.setGender(employee.getGender());
        exist.setPhone(employee.getPhone());
        exist.setEmail(employee.getEmail());
        exist.setDepartmentId(employee.getDepartmentId());
        exist.setPosition(employee.getPosition());
        exist.setEntryDate(employee.getEntryDate());
        exist.setStatus(employee.getStatus());
        exist.setFaceImage(employee.getFaceImage());
        exist.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(exist);
    }

    public void delete(Integer id) {
        employeeRepository.deleteById(id);
    }

    @Transactional
    public int batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请选择需要处理的员工");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("员工状态不正确");
        }

        List<Employee> employees = employeeRepository.findAllById(ids);
        for (Employee employee : employees) {
            employee.setStatus(status);
            employee.setUpdatedAt(LocalDateTime.now());
        }
        employeeRepository.saveAll(employees);
        return employees.size();
    }

    public Employee findByUserId(Integer userId) {
        return employeeRepository.findByUserId(userId).orElse(null);
    }

    public Employee findById(Integer id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public List<Employee> findAll() {
        return employeeRepository.findByStatus(1);
    }
}
