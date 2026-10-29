package com.zjy.blog.controller;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.entity.Message;
import com.zjy.blog.repository.MessageRepository;
import com.zjy.blog.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Message> create(@RequestBody Message message) {
        String content = message.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("留言内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException("留言内容不能超过500字");
        }
        String nickname = message.getNickname();
        if (nickname != null && nickname.trim().length() > 30) {
            throw new BusinessException("昵称不能超过30字");
        }
        message.setId(null);
        message.setCreateTime(LocalDateTime.now());
        message.setContent(content.trim());
        if (nickname == null || nickname.trim().isEmpty()) {
            message.setNickname("匿名");
        } else {
            message.setNickname(nickname.trim());
        }
        if (message.getAvatarUrl() != null && !message.getAvatarUrl().startsWith("/api/files/")) {
            message.setAvatarUrl(null);
        }
        // 敏感词过滤
        message.setContent(sensitiveWordService.filter(message.getContent()));
        message.setNickname(sensitiveWordService.filter(message.getNickname()));
        return ResponseEntity.ok(messageRepository.save(message));
    }
}
