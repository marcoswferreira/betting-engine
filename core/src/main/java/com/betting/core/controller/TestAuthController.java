package com.betting.core.controller;

import com.betting.core.infra.security.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class TestAuthController {

    private final JwtUtil jwtUtil;

    public TestAuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/token")
    public Map<String, String> getToken(
            @RequestParam(defaultValue = "00000000-0000-0000-0000-000000000001") String userId,
            @RequestParam(defaultValue = "11111111-1111-1111-1111-111111111111") String tenantId) {
        
        String token = jwtUtil.generateToken(userId, UUID.fromString(tenantId));
        return Map.of("token", token);
    }
}
