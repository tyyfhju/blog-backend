package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.FriendLink;
import com.zjy.blog.entity.FriendLinkApplication;
import com.zjy.blog.repository.FriendLinkApplicationRepository;
import com.zjy.blog.repository.FriendLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/private/friend-links")
public class AdminFriendLinkController {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Autowired
    private FriendLinkApplicationRepository applicationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<FriendLink> list = friendLinkRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody FriendLink friendLink) {
        if (friendLink.getCreateTime() == null) {
            friendLink.setCreateTime(java.time.LocalDateTime.now());
        }
        friendLinkRepository.save(friendLink);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "添加成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody FriendLink friendLink) {
        FriendLink exist = friendLinkRepository.findById(id).orElse(null);
        if (exist == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "友链不存在");
            return ResponseEntity.ok(result);
        }
        exist.setName(friendLink.getName());
        exist.setUrl(friendLink.getUrl());
        exist.setAvatarUrl(friendLink.getAvatarUrl());
        exist.setDescription(friendLink.getDescription());
        friendLinkRepository.save(exist);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        FriendLink friendLink = friendLinkRepository.findById(id).orElse(null);
        if (friendLink != null) {
            String url = friendLink.getUrl();
            // 同步删除对应的申请记录（用 find + deleteAll 替代 @Modifying deleteByUrl，确保事务生效）
            List<FriendLinkApplication> apps = applicationRepository.findByUrl(url);
            if (!apps.isEmpty()) {
                applicationRepository.deleteAll(apps);
            }
            friendLinkRepository.deleteById(id);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }
}
