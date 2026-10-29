package com.zjy.blog.controller;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.entity.Comment;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.CommentRepository;
import com.zjy.blog.service.SensitiveWordService;
import com.zjy.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ArticleRepository articleRepository;
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
        if (comment.getArticleId() == null || !articleRepository.existsById(comment.getArticleId())) {
            throw new BusinessException("文章不存在");
        }
        String content = comment.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }
        String email = (String) auth.getPrincipal();
        Long userId = userService.getUserIdByEmail(email);
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        // 敏感词过滤
        comment.setContent(sensitiveWordService.filter(content.trim()));
        return ResponseEntity.ok(commentRepository.save(comment));
    }
}