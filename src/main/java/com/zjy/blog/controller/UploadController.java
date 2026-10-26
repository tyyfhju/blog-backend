package com.zjy.blog.controller;

import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private CosUtil cosUtil;

    @PostMapping("/private/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String key = cosUtil.upload(file);
        Map<String, String> res = new HashMap<>();
        res.put("url", "/api/files/" + key.substring(key.lastIndexOf("/") + 1));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadPublicImage(@RequestParam("file") MultipartFile file) throws IOException {
        String key = cosUtil.upload(file);
        Map<String, String> res = new HashMap<>();
        res.put("url", "/api/files/" + key.substring(key.lastIndexOf("/") + 1));
        return ResponseEntity.ok(res);
    }
}