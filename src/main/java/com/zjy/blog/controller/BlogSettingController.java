package com.zjy.blog.controller;

import com.zjy.blog.entity.BlogSetting;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.CategoryRepository;
import com.zjy.blog.repository.TagRepository;
import com.zjy.blog.service.BlogSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class BlogSettingController {

    @Autowired
    private BlogSettingService settingService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/api/blog-setting")
    public BlogSetting getSetting() {
        return settingService.getSetting();
    }

    @GetMapping("/api/home/counts")
    public Map<String, Long> getCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("articles", articleRepository.countByStatus(1));
        counts.put("categories", categoryRepository.count());
        counts.put("tags", tagRepository.count());
        return counts;
    }
}