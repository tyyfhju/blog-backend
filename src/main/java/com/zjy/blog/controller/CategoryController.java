package com.zjy.blog.controller;

import com.zjy.blog.entity.Category;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public CategoryController(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public List<Category> list() {
        List<Category> categories = categoryRepository.findAll();
        for (Category c : categories) {
            c.setArticleCount(articleRepository.countByCategoryIdAndStatus(c.getId(), 1));
        }
        return categories;
    }
}