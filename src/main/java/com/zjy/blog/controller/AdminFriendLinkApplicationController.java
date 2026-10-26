package com.zjy.blog.controller;

import com.zjy.blog.entity.FriendLink;
import com.zjy.blog.entity.FriendLinkApplication;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.FriendLinkApplicationRepository;
import com.zjy.blog.repository.FriendLinkRepository;
import com.zjy.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/private/friend-link-applications")
public class AdminFriendLinkApplicationController {

    @Autowired
    private FriendLinkApplicationRepository applicationRepository;

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<FriendLinkApplication> list = applicationRepository.findAllByOrderByCreateTimeDesc();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id) {
        FriendLinkApplication app = applicationRepository.findById(id).orElse(null);
        if (app == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "申请不存在");
            return ResponseEntity.ok(result);
        }

        // 统一加 https:// 前缀
        String url = app.getUrl();
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // 创建友链
        FriendLink link = new FriendLink();
        link.setName(app.getName());
        link.setUrl(url);
        link.setAvatarUrl(app.getAvatarUrl());
        link.setDescription(app.getDescription());
        link.setCreateTime(LocalDateTime.now());
        friendLinkRepository.save(link);

        // 更新申请状态
        app.setStatus("approved");
        applicationRepository.save(app);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "审核通过，已添加到友链列表");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id) {
        FriendLinkApplication app = applicationRepository.findById(id).orElse(null);
        if (app == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "申请不存在");
            return ResponseEntity.ok(result);
        }
        app.setStatus("rejected");
        applicationRepository.save(app);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已拒绝");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id) {
        applicationRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已删除");
        return ResponseEntity.ok(result);
    }
}
