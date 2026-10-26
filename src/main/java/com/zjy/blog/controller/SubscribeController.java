package com.zjy.blog.controller;

import com.zjy.blog.entity.Subscribe;
import com.zjy.blog.repository.SubscribeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubscribeController {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @PostMapping("/subscribe")
    public Map<String, Object> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "邮箱不能为空");
            return result;
        }
        if (subscribeRepository.existsByEmail(email)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "该邮箱已订阅");
            return result;
        }
        Subscribe sub = new Subscribe();
        sub.setEmail(email);
        subscribeRepository.save(sub);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "订阅成功！新文章发布时会发送邮件通知");
        return result;
    }
}
