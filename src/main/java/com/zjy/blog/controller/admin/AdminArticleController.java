package com.zjy.blog.controller.admin;

import com.zjy.blog.dto.ArticleCreateRequest;
import com.zjy.blog.dto.ArticleUpdateRequest;
import com.zjy.blog.entity.Article;
import com.zjy.blog.service.ArticleService;
import com.zjy.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private/articles")
public class AdminArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Page<Article> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return articleService.listAll(page, size);
    }

    @GetMapping("/{id}")
    public Article getOne(@PathVariable Long id) {
        return articleService.getById(id);
    }

    @PostMapping
    public Article create(@RequestBody ArticleCreateRequest request, Authentication auth) {
        String email = (String) auth.getPrincipal();
        Long userId = userService.getUserIdByEmail(email);
        return articleService.createAndNotify(request, userId);
    }

    @PutMapping("/{id}")
    public Article update(@PathVariable Long id, @RequestBody ArticleUpdateRequest request) {
        return articleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        articleService.delete(id);
    }
}