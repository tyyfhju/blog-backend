package com.zjy.blog.controller;

import com.zjy.blog.entity.FriendLink;
import com.zjy.blog.repository.FriendLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/friend-links")
public class FriendLinkController {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @GetMapping
    public List<FriendLink> list() {
        return friendLinkRepository.findAll();
    }
}
