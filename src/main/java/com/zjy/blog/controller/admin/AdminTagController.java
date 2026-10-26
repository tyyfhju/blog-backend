package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.Tag;
import com.zjy.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/private/tags")
public class AdminTagController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public List<Tag> list() {
        return tagRepository.findAll();
    }

    @PostMapping
    public Tag create(@RequestBody Tag tag) {
        return tagRepository.save(tag);
    }

    @PutMapping("/{id}")
    public Tag update(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        return tagRepository.save(tag);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tagRepository.deleteById(id);
    }
}