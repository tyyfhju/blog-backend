package com.zjy.blog.controller;

import com.zjy.blog.dto.*;
import com.zjy.blog.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/send-email-code")
    public ResponseEntity<Map<String, String>> sendEmailCode(@RequestParam String email) {
        authService.sendEmailCode(email);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "验证码已发送");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        Map<String, String> resp = new HashMap<>();
        resp.put("token", token);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        Map<String, String> resp = new HashMap<>();
        resp.put("token", token);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendResetPasswordCode(request.getEmail());
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "重置密码验证码已发送");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "密码重置成功");
        return ResponseEntity.ok(resp);
    }
}