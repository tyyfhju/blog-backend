package com.zjy.blog.controller;

import com.zjy.blog.entity.User;
import com.zjy.blog.service.UserService;
import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CosUtil cosUtil;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication auth) {
        String email = (String) auth.getPrincipal();
        User user = userService.getCurrentUser(email);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User updateData, Authentication auth) {
        String email = (String) auth.getPrincipal();
        User user = userService.getCurrentUser(email);
        if (updateData.getNickname() != null) user.setNickname(updateData.getNickname());
        if (updateData.getAvatarUrl() != null) user.setAvatarUrl(updateData.getAvatarUrl());
        if (updateData.getWebsite() != null) user.setWebsite(updateData.getWebsite());
        if (updateData.getBio() != null) user.setBio(updateData.getBio());
        user = userService.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/upload/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                            Authentication auth) throws IOException {
        String key = cosUtil.upload(file);
        Map<String, String> res = new HashMap<>();
        res.put("url", "/api/files/" + key.substring(key.lastIndexOf("/") + 1));
        return ResponseEntity.ok(res);
    }
}