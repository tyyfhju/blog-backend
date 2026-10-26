package com.zjy.blog.service;

import com.zjy.blog.entity.Subscribe;
import com.zjy.blog.repository.SubscribeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Value("${spring.mail.username}")
    private String from;

    public void sendNewArticleEmail(String articleTitle, String articleUrl) {
        List<Subscribe> subscribers = subscribeRepository.findByIsActive(1);
        if (subscribers.isEmpty()) return;

        String subject = "【Aurora Blog】新文章发布：《" + articleTitle + "》";
        String text = "您订阅的博客发布了新文章：\n\n"
                + "标题：" + articleTitle + "\n"
                + "链接：" + articleUrl + "\n\n"
                + "点击链接即可阅读全文。\n"
                + "如需取消订阅，请回复此邮件。\n\n"
                + "—— Aurora Blog";

        for (Subscribe sub : subscribers) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(sub.getEmail());
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
            } catch (Exception e) {
                // 单个发送失败不影响其他人
            }
        }
    }
}
