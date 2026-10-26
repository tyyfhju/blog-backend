package com.zjy.blog.controller;

import com.zjy.blog.entity.Photo;
import com.zjy.blog.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @GetMapping
    public List<Photo> list(@RequestParam(required = false) Long albumId) {
        if (albumId != null) {
            return photoRepository.findByAlbumId(albumId);
        }
        return photoRepository.findAll();
    }
}