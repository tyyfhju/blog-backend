package com.zjy.blog.controller;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.entity.Message;
import com.zjy.blog.repository.MessageRepository;
import com.zjy.blog.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public List<Message> list() {
        return messageRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Message> create(@RequestBody Message message, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException("请先登录后再留言");
        }
        String content = message.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("留言内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException("留言内容不能超过500字");
        }
        message.setId(null);
        message.setCreateTime(LocalDateTime.now());
        message.setContent(content.trim());
        // 留言身份统一取登录邮箱，前端传的昵称不再使用
        message.setNickname((String) auth.getPrincipal());
        if (message.getAvatarUrl() != null && !message.getAvatarUrl().startsWith("/api/files/")) {
            message.setAvatarUrl(null);
        }
        // 敏感词过滤
        message.setContent(sensitiveWordService.filter(message.getContent()));
        return ResponseEntity.ok(messageRepository.save(message));
    }
}
