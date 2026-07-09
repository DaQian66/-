package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.entity.*;
import com.attendance.service.UserService;
import com.attendance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys")
public class SysController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private RoleMenuRepository roleMenuRepository;
    @Autowired private OperationLogRepository operationLogRepository;

    // ---- 用户管理 ----
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<?> listUsers(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Page<User> p = userRepository.findAll(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return ApiResponse.success(new PageResult<>(p.getTotalElements(), page, size, p.getContent()));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success("新增用户成功", userService.create(user));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        return ApiResponse.success("更新用户成功", userService.update(user));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    // ---- 角色管理 ----
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<Role>> listRoles() {
        return ApiResponse.success(roleRepository.findAll());
    }

    // ---- 菜单管理 ----
    @GetMapping("/menus")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<Menu>> listMenus() {
        return ApiResponse.success(menuRepository.findAllOrderBySort());
    }

    @GetMapping("/menus/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<?> getRoleMenus(@PathVariable Integer roleId) {
        List<RoleMenu> rm = roleMenuRepository.findByRoleId(roleId);
        List<Integer> ids = rm.stream().map(RoleMenu::getMenuId).collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(ids);
    }

    @PutMapping("/menus/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<?> updateRoleMenus(@PathVariable Integer roleId, @RequestBody List<Integer> menuIds) {
        roleMenuRepository.deleteByRoleId(roleId);
        for (Integer menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuRepository.save(rm);
        }
        return ApiResponse.success("权限更新成功", null);
    }

    // ---- 操作日志 ----
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<PageResult<OperationLog>> listLogs(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Page<OperationLog> p = operationLogRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(new PageResult<>(p.getTotalElements(), page, size, p.getContent()));
    }
}