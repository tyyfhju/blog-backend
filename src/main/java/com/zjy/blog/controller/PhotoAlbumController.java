package com.zjy.blog.controller;

import com.zjy.blog.entity.PhotoAlbum;
import com.zjy.blog.service.PhotoAlbumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/photo-albums")
public class PhotoAlbumController {
    private final PhotoAlbumService albumService;
    public PhotoAlbumController(PhotoAlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public List<PhotoAlbum> list() {
        return albumService.list();
    }
}