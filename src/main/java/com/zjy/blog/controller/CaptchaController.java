package com.zjy.blog.controller;

import com.zjy.blog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generate() throws Exception {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        String captchaCode = code.toString();
        String key = UUID.randomUUID().toString();
        redisUtil.set("captcha:" + key, captchaCode, 5, TimeUnit.MINUTES);

        int width = 120, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            g.drawLine((int) (Math.random() * width), (int) (Math.random() * height),
                    (int) (Math.random() * width), (int) (Math.random() * height));
        }
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.BLACK);
        g.drawString(captchaCode, 10, 28);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", base64);
        return ResponseEntity.ok(result);
    }
}