package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.Message;
import com.zjy.blog.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/messages")
public class AdminMessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public List<Message> list() {
        return messageRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        messageRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "删除成功");
        return ResponseEntity.ok(res);
    }
}
