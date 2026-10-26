package com.zjy.blog.controller;

import com.zjy.blog.entity.Talk;
import com.zjy.blog.repository.TalkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/talks")
public class TalkController {

    @Autowired
    private TalkRepository talkRepository;

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        Page<Talk> pageResult = talkRepository.findPublished(PageRequest.of(page, size));
        List<Map<String, Object>> content = pageResult.getContent().stream().map(talk -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", talk.getId());
            map.put("content", talk.getContent());
            map.put("createTime", talk.getCreateTime() != null ? talk.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            map.put("isTop", talk.getIsTop());
            if (talk.getImages() != null && !talk.getImages().trim().isEmpty()) {
                map.put("images", Arrays.asList(talk.getImages().split(",")));
            } else {
                map.put("images", java.util.Collections.emptyList());
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalPages", pageResult.getTotalPages());
        result.put("totalElements", pageResult.getTotalElements());
        return result;
    }
}
