package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.Talk;
import com.zjy.blog.repository.TalkRepository;
import com.zjy.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/private/talks")
public class AdminTalkController {

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        Page<Talk> pageResult = talkRepository.findAll(PageRequest.of(page, size));
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageResult.getContent());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("totalElements", pageResult.getTotalElements());
        return result;
    }

    @PostMapping
    public ResponseEntity<Talk> create(@RequestBody Talk talk, Authentication auth) {
        String email = (String) auth.getPrincipal();
        talk.setUserId(userService.getUserIdByEmail(email));
        talk.setId(null);
        talk.setCreateTime(null);
        return ResponseEntity.ok(talkRepository.save(talk));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Talk> update(@PathVariable Long id, @RequestBody Talk talk) {
        Talk existing = talkRepository.findById(id).orElseThrow(() -> new RuntimeException("说说不存在"));
        if (talk.getContent() != null) existing.setContent(talk.getContent());
        if (talk.getImages() != null) existing.setImages(talk.getImages());
        if (talk.getIsTop() != null) existing.setIsTop(talk.getIsTop());
        if (talk.getStatus() != null) existing.setStatus(talk.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        return ResponseEntity.ok(talkRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        talkRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "删除成功");
        return ResponseEntity.ok(res);
    }
}
