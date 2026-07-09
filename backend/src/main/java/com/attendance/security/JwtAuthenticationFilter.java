package com.attendance.security;

import com.attendance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(jwtUtil.getTokenPrefix())) {
            String token = header.substring(jwtUtil.getTokenPrefix().length());
            if (!jwtUtil.isTokenExpired(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                if (username != null && role != null) {
                    List<SimpleGrantedAuthority> authorities = buildAuthorities(role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(String role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        String roleCode = toRoleCode(role);
        if (roleCode != null && !roleCode.equals(role)) {
            authorities.add(new SimpleGrantedAuthority(roleCode));
        }
        return authorities;
    }

    private String toRoleCode(String role) {
        switch (role) {
            case "系统管理员":
            case "管理员":
                return "ROLE_ADMIN";
            case "部门经理":
                return "ROLE_MANAGER";
            case "普通员工":
                return "ROLE_EMPLOYEE";
            default:
                return role.startsWith("ROLE_") ? role : null;
        }
    }
}
