package com.zjy.blog.controller;

import com.zjy.blog.entity.Article;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.service.ArticleService;
import com.zjy.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public Page<Article> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Long tagId) {
        return articleService.getPublished(page, size, categoryId, tagId);
    }

    @GetMapping("/search")
    public Page<Article> search(@RequestParam String q,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return articleRepository.searchPublished(1, q, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id, Authentication auth) {
        Article article = articleService.getById(id);
        // 阅读量 +1
        articleService.incrementViewCount(id);
        if (article != null && article.getViewCount() != null) {
            article.setViewCount(article.getViewCount() + 1);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("article", article);
        result.put("previous", articleService.getPreviousArticle(id));
        result.put("next", articleService.getNextArticle(id));
        // 当前用户是否已点赞
        boolean liked = false;
        if (auth != null && auth.isAuthenticated()) {
            try {
                String email = (String) auth.getPrincipal();
                Long userId = userService.getUserIdByEmail(email);
                liked = articleService.isLiked(id, userId);
            } catch (Exception ignored) {}
        }
        result.put("liked", liked);
        return result;
    }

    @GetMapping("/{id}/previous")
    public Article previous(@PathVariable Long id) {
        return articleService.getPreviousArticle(id);
    }

    @GetMapping("/{id}/next")
    public Article next(@PathVariable Long id) {
        return articleService.getNextArticle(id);
    }

    @GetMapping("/{id}/related")
    public List<Article> related(@PathVariable Long id) {
        return articleService.getRelatedArticles(id);
    }

    @GetMapping("/recommended")
    public List<Article> recommended() {
        return articleService.getRecommended();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long id, Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        String email = (String) auth.getPrincipal();
        Long userId = userService.getUserIdByEmail(email);
        boolean ok = articleService.like(id, userId);
        result.put("success", ok);
        result.put("message", ok ? "点赞成功" : "已经点过赞了");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> unlike(@PathVariable Long id, Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.ok(result);
        }
        String email = (String) auth.getPrincipal();
        Long userId = userService.getUserIdByEmail(email);
        boolean ok = articleService.unlike(id, userId);
        result.put("success", ok);
        result.put("message", ok ? "已取消点赞" : "尚未点赞");
        return ResponseEntity.ok(result);
    }
}
