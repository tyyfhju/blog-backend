package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.Photo;
import com.zjy.blog.entity.PhotoAlbum;
import com.zjy.blog.repository.PhotoRepository;
import com.zjy.blog.service.PhotoAlbumService;
import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/photos")
public class AdminPhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoAlbumService albumService;

    @Autowired
    private CosUtil cosUtil;

    // 相册列表
    @GetMapping("/albums")
    public List<PhotoAlbum> listAlbums() {
        return albumService.list();
    }

    // 新建相册
    @PostMapping("/albums")
    public PhotoAlbum createAlbum(@RequestBody PhotoAlbum album) {
        return albumService.create(album);
    }

    // 删除相册
    @DeleteMapping("/albums/{id}")
    public void deleteAlbum(@PathVariable Long id) {
        albumService.delete(id);
    }

    // 上传照片到相册
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "albumId", required = false) Long albumId) throws IOException {
        String key = cosUtil.upload(file);
        String url = "/api/files/" + key.substring(key.lastIndexOf("/") + 1);
        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setAlbumId(albumId);
        photo.setDescription("");
        photoRepository.save(photo);
        Map<String, String> res = new HashMap<>();
        res.put("url", url);
        return ResponseEntity.ok(res);
    }

    // 按相册查询照片
    @GetMapping
    public List<Photo> listPhotos(@RequestParam(required = false) Long albumId) {
        if (albumId != null) {
            return photoRepository.findByAlbumId(albumId);
        }
        return photoRepository.findAll();
    }

    // 删除照片
    @DeleteMapping("/{id}")
    public void deletePhoto(@PathVariable Long id) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo != null) {
            cosUtil.deleteByUrl(photo.getUrl());
        }
        photoRepository.deleteById(id);
    }
}