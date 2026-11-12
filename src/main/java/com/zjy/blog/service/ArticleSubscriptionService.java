package com.zjy.blog.service;

import com.zjy.blog.entity.Article;
import com.zjy.blog.entity.BlogSetting;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.BlogSettingRepository;
import com.zjy.blog.repository.UserRepository;
import com.zjy.blog.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ArticleSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(ArticleSubscriptionService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlogSettingRepository blogSettingRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.site-url:https://xn--hlry9isyc4tas57t.com}")
    private String siteUrl;

    /**
     * 文章从草稿变为已发布时，给所有开启订阅的用户发邮件。
     * 发送失败只记录日志，不影响文章发布。
     */
    @Async
    public void notifyPublished(Article article) {
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            return;
        }
        List<User> subscribers = userRepository.findBySubscribeTrueAndStatus(1);
        if (subscribers.isEmpty()) {
            return;
        }

        BlogSetting setting = blogSettingRepository.findTopByOrderByIdAsc().orElse(null);
        String siteTitle = textOr(setting != null ? setting.getSiteTitle() : null, "我的博客");
        String subject = "【" + siteTitle + "】新文章：" + article.getTitle();
        String url = baseUrl() + "/article/" + article.getId();

        int sent = 0;
        for (User user : subscribers) {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                continue;
            }
            try {
                String token = jwtTokenProvider.generateUnsubscribeToken(user.getId());
                String unsubscribeUrl = baseUrl() + "/api/subscriptions/unsubscribe?token=" + token;
                String html = buildHtml(article, url, siteTitle, setting, unsubscribeUrl);
                String text = buildText(article, url, unsubscribeUrl);
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(user.getEmail());
                helper.setSubject(subject);
                mime.setHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");
                mime.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
                helper.setText(text, html);
                mailSender.send(mime);
                sent++;
            } catch (Exception e) {
                log.warn("新文章通知邮件发送失败: {}", user.getEmail(), e);
            }
        }
        log.info("新文章《{}》发布通知：订阅用户 {} 人，成功发送 {} 封",
                article.getTitle(), subscribers.size(), sent);
    }

    private String buildHtml(Article article, String url, String siteTitle, BlogSetting setting,
                             String unsubscribeUrl) {
        String title = escapeHtml(article.getTitle());
        String summary = article.getSummary() == null ? "" : article.getSummary().trim();
        String safeSummary = escapeHtml(summary);
        String date = article.getCreateTime() == null ? "" : article.getCreateTime().format(DATE_FORMAT);
        String author = escapeHtml(textOr(setting != null ? setting.getAuthorName() : null, "站长"));
        String avatar = absoluteUrl(setting != null ? setting.getAvatarUrl() : null);
        String cover = absoluteUrl(article.getCoverUrl());

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head>")
          .append("<meta charset=\"UTF-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
          .append("</head>")
          .append("<body style=\"margin:0;padding:0;background-color:#eef3fb;\">")
          .append("<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;\">")
          .append(safeSummary).append("</div>")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
          .append("style=\"background-color:#eef3fb;padding:36px 12px;\">")
          .append("<tr><td align=\"center\">")
          .append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
          .append("style=\"width:100%;max-width:600px;background-color:#ffffff;border:1px solid #e3eaf6;")
          .append("border-radius:18px;overflow:hidden;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',")
          .append("'PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif;\">")
          .append("<tr><td style=\"height:6px;background-color:#2563EB;")
          .append("background-image:linear-gradient(90deg,#2563EB 0%,#06B6D4 100%);")
          .append("font-size:0;line-height:0;\">&nbsp;</td></tr>")
          .append("<tr><td style=\"padding:26px 32px 0;\">")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>");
        if (avatar != null) {
            sb.append("<td width=\"42\" style=\"padding-right:10px;\">")
              .append("<img src=\"").append(escapeHtml(avatar)).append("\" width=\"42\" height=\"42\" alt=\"\" ")
              .append("style=\"display:block;border:0;border-radius:50%;\">")
              .append("</td>");
        }
        sb.append("<td style=\"font-size:18px;font-weight:700;color:#0f172a;letter-spacing:.2px;\">")
          .append(escapeHtml(siteTitle)).append("</td>")
          .append("<td align=\"right\" style=\"font-size:10px;font-weight:700;letter-spacing:3px;color:#2563EB;\">")
          .append("NEW ARTICLE</td></tr></table></td></tr>")
          .append("<tr><td style=\"padding:20px 32px 0;\">")
          .append("<h1 style=\"margin:0;font-size:26px;line-height:1.45;color:#0f172a;font-weight:800;letter-spacing:.2px;\">")
          .append(title).append("</h1>")
          .append("<p style=\"margin:12px 0 0;font-size:12px;line-height:1.6;color:#94a3b8;\">")
          .append(author);
        if (!date.isEmpty()) {
            sb.append(" &nbsp;·&nbsp; ").append(date);
        }
        sb.append("</p></td></tr>");
        if (cover != null) {
            sb.append("<tr><td style=\"padding:20px 32px 0;\">")
              .append("<img src=\"").append(escapeHtml(cover)).append("\" alt=\"\" width=\"536\" ")
              .append("style=\"display:block;width:100%;height:auto;border:0;border-radius:12px;\">")
              .append("</td></tr>");
        }
        if (!safeSummary.isEmpty()) {
            sb.append("<tr><td style=\"padding:22px 32px 0;\">")
              .append("<p style=\"margin:0;font-size:15px;line-height:1.85;color:#475569;\">")
              .append(safeSummary).append("</p></td></tr>");
        }
        sb.append("<tr><td style=\"padding:28px 32px 34px;\">")
          .append("<a href=\"").append(escapeHtml(url)).append("\" ")
          .append("style=\"display:inline-block;padding:13px 30px;border-radius:999px;background-color:#2563EB;")
          .append("color:#ffffff;font-size:14px;font-weight:700;text-decoration:none;letter-spacing:.5px;\">")
          .append("阅读全文 →</a></td></tr>")
          .append("<tr><td style=\"padding:18px 32px 20px;background-color:#f8fafc;border-top:1px solid #eef2f7;\">")
          .append("<p style=\"margin:0;font-size:11px;line-height:1.8;color:#94a3b8;\">")
          .append("你收到这封邮件，是因为在个人中心开启了“新文章邮件通知”。<br>")
          .append("不想再收到？<a href=\"").append(escapeHtml(unsubscribeUrl))
          .append("\" style=\"color:#2563EB;text-decoration:underline;\">一键退订</a>")
          .append("，也可以登录个人中心关闭该选项。</p>")
          .append("</td></tr></table></td></tr></table></body></html>");
        return sb.toString();
    }

    private String buildText(Article article, String url, String unsubscribeUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("你好，\n\n");
        sb.append("本站发布了新文章：\n\n");
        sb.append("《").append(article.getTitle()).append("》\n");
        if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
            sb.append(article.getSummary().trim()).append("\n");
        }
        sb.append("\n阅读全文：").append(url).append("\n\n");
        sb.append("你收到这封邮件，是因为在个人中心开启了“新文章邮件通知”。\n");
        sb.append("一键退订：").append(unsubscribeUrl).append("\n");
        sb.append("也可以登录个人中心关闭该选项。\n");
        return sb.toString();
    }

    private String absoluteUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String value = path.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return value.startsWith("/") ? baseUrl() + value : baseUrl() + "/" + value;
    }

    private String baseUrl() {
        return siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;
    }

    private String textOr(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
