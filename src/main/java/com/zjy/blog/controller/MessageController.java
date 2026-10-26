package com.zjy.blog.controller;

import com.zjy.blog.entity.Message;
import com.zjy.blog.repository.MessageRepository;
import com.zjy.blog.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        message.setId(null);
        message.setCreateTime(null);
        if (message.getNickname() == null || message.getNickname().trim().isEmpty()) {
            message.setNickname("匿名");
        }
        // 敏感词过滤
        message.setContent(sensitiveWordService.filter(message.getContent()));
        message.setNickname(sensitiveWordService.filter(message.getNickname()));
        return ResponseEntity.ok(messageRepository.save(message));
    }
}
