package com.ecommerce.ecommerce.service.jwt;

import com.ecommerce.ecommerce.config.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String extractRoleFromContext() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return JwtUtil.extractUserRoleFromUsername(username); // Fetch role from JWT or database
    }
}
