package com.zjy.blog.controller;

import com.zjy.blog.entity.Tag;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;

    public TagController(TagRepository tagRepository, ArticleRepository articleRepository) {
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public List<Tag> list() {
        List<Tag> tags = tagRepository.findAll();
        for (Tag t : tags) {
            t.setArticleCount(articleRepository.countPublishedByTagId(t.getId(), 1));
        }
        return tags;
    }
}