package com.attendance.controller;

import com.attendance.dto.*;
import com.attendance.service.UserService;
import com.attendance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody LoginRequest request) {
        // simple register - creates user with employee role
        com.attendance.entity.User user = new com.attendance.entity.User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getUsername());
        user.setRoleId(3); // employee
        user.setStatus(1);
        userService.create(user);
        return ApiResponse.success("注册成功，请等待管理员审批");
    }

    @GetMapping("/user/info")
    public ApiResponse<UserInfo> userInfo(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeader());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            token = token.substring(jwtUtil.getTokenPrefix().length());
        }
        Integer userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        UserInfo info = userService.getUserInfo(userId, username);
        return ApiResponse.success(info);
    }

    @GetMapping("/user/menus")
    public ApiResponse<?> userMenus(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeader());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            token = token.substring(jwtUtil.getTokenPrefix().length());
        }
        Integer userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        UserInfo info = userService.getUserInfo(userId, username);
        return ApiResponse.success(info != null ? info.getMenus() : null);
    }
}