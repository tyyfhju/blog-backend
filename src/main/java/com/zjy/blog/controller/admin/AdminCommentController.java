package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.Comment;
import com.zjy.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/private/comments")
public class AdminCommentController {

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping
    public Page<Comment> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return commentRepository.findAll(PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        commentRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "删除成功");
        return ResponseEntity.ok(res);
    }
}
