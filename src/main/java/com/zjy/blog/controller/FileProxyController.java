package com.zjy.blog.controller;

import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileProxyController {

    @Autowired
    private CosUtil cosUtil;

    @GetMapping("/{filename}")
    public void proxy(@PathVariable String filename, HttpServletResponse response) throws IOException {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replace("+", "%20");
        String key = "blog/" + encoded;
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 根据扩展名设置 Content-Type
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) response.setContentType("image/png");
        else if (lower.endsWith(".gif")) response.setContentType("image/gif");
        else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) response.setContentType("image/jpeg");
        else if (lower.endsWith(".webp")) response.setContentType("image/webp");
        else if (lower.endsWith(".svg")) {
            response.setContentType("image/svg+xml");
            response.setHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'");
        }
        else if (lower.endsWith(".ico")) response.setContentType("image/x-icon");
        else response.setContentType("application/octet-stream");

        byte[] bytes;
        try {
            bytes = cosUtil.download(key);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
