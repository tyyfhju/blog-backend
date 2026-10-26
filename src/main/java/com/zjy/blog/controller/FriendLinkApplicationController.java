package com.zjy.blog.controller;

import com.zjy.blog.entity.FriendLinkApplication;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.FriendLinkApplicationRepository;
import com.zjy.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/friend-links")
public class FriendLinkApplicationController {

    @Autowired
    private FriendLinkApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody FriendLinkApplication application) {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            result.put("success", false);
            result.put("message", "请先登录后再提交友链申请");
            return ResponseEntity.status(401).body(result);
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(result);
        }

        if (application.getName() == null || application.getName().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "网站名称不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        if (application.getUrl() == null || application.getUrl().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "网站地址不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        application.setId(null);
        application.setUserId(user.getId());
        application.setUserEmail(email);
        application.setStatus("pending");
        applicationRepository.save(application);

        result.put("success", true);
        result.put("message", "友链申请已提交，等待管理员审核");
        return ResponseEntity.ok(result);
    }
}
