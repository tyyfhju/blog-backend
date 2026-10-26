package com.zjy.blog.controller;

import com.zjy.blog.entity.Comment;
import com.zjy.blog.repository.CommentRepository;
import com.zjy.blog.service.SensitiveWordService;
import com.zjy.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public List<Comment> list(@RequestParam Long articleId) {
        return commentRepository.findByArticleIdOrderByCreateTimeDesc(articleId);
    }

    @PostMapping
    public ResponseEntity<Comment> create(@RequestBody Comment comment, Authentication auth) {
        String email = (String) auth.getPrincipal();
        Long userId = userService.getUserIdByEmail(email);
        comment.setUserId(userId);
        comment.setCreateTime(null);
        // 敏感词过滤
        comment.setContent(sensitiveWordService.filter(comment.getContent()));
        return ResponseEntity.ok(commentRepository.save(comment));
    }
}