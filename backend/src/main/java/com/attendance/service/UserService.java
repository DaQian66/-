package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.*;
import com.attendance.repository.*;
import com.attendance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private RoleMenuRepository roleMenuRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role.getName());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setRole(role.getName());
        response.setRoleId(role.getId());
        return response;
    }

    public UserInfo getUserInfo(Integer userId, String username) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setAvatar(user.getAvatar());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());

        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        if (role != null) {
            info.setRole(role.getName());
            info.setRoleId(role.getId());
        }

        // build menus
        List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(user.getRoleId());
        List<Integer> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        if (!menuIds.isEmpty()) {
            List<Menu> allMenus = menuRepository.findAllById(menuIds);
            List<MenuTree> menuTrees = buildMenuTree(allMenus.stream()
                    .filter(m -> m.getVisible() == 1 && "menu".equals(m.getType()))
                    .collect(Collectors.toList()), 0);
            info.setMenus(menuTrees);
            info.setPermissions(allMenus.stream().map(Menu::getPermission).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return info;
    }

    private List<MenuTree> buildMenuTree(List<Menu> menus, int parentId) {
        List<MenuTree> trees = new ArrayList<>();
        for (Menu menu : menus.stream().filter(m -> m.getParentId() == parentId)
                .sorted(Comparator.comparing(Menu::getSortOrder)).collect(Collectors.toList())) {
            MenuTree tree = new MenuTree();
            tree.setId(menu.getId());
            tree.setParentId(menu.getParentId());
            tree.setName(menu.getName());
            tree.setPath(menu.getPath());
            tree.setComponent(menu.getComponent());
            tree.setIcon(menu.getIcon());
            tree.setPermission(menu.getPermission());
            tree.setSortOrder(menu.getSortOrder());
            tree.setChildren(buildMenuTree(menus, menu.getId()));
            trees.add(tree);
        }
        return trees;
    }

    // ---- User CRUD ----
    public List<User> listAll() { return userRepository.findAll(); }

    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User update(User user) {
        User exist = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        exist.setRealName(user.getRealName());
        exist.setEmail(user.getEmail());
        exist.setPhone(user.getPhone());
        exist.setRoleId(user.getRoleId());
        exist.setDepartmentId(user.getDepartmentId());
        exist.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            exist.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        exist.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(exist);
    }

    public void delete(Integer id) {
        if (id == 1) throw new IllegalArgumentException("不能删除超级管理员");
        userRepository.deleteById(id);
    }
}