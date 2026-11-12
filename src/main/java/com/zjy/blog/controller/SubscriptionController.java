package com.zjy.blog.controller;

import com.zjy.blog.entity.BlogSetting;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.BlogSettingRepository;
import com.zjy.blog.repository.UserRepository;
import com.zjy.blog.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlogSettingRepository blogSettingRepository;

    @Value("${app.site-url:https://xn--hlry9isyc4tas57t.com}")
    private String siteUrl;

    @GetMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam(required = false) String token) {
        return unsubscribeInternal(token);
    }

    /**
     * 兼容邮件客户端的 List-Unsubscribe-Post 一键退订。
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribePost(@RequestParam(required = false) String token) {
        return unsubscribeInternal(token);
    }

    private ResponseEntity<String> unsubscribeInternal(String token) {
        BlogSetting setting = blogSettingRepository.findTopByOrderByIdAsc().orElse(null);
        String siteTitle = textOr(setting != null ? setting.getSiteTitle() : null, "我的博客");
        String avatar = absoluteUrl(setting != null ? setting.getAvatarUrl() : null);

        if (token == null || token.trim().isEmpty()) {
            return page(siteTitle, avatar, false, null,
                    "退订链接无效", "缺少退订参数，请从邮件里的“一键退订”链接进入。");
        }

        Long userId;
        try {
            userId = jwtTokenProvider.parseUnsubscribeUserId(token);
        } catch (Exception e) {
            return page(siteTitle, avatar, false, null,
                    "退订链接无效", "这个链接无效或已过期。你仍然可以登录个人中心关闭订阅。");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return page(siteTitle, avatar, false, null,
                    "退订链接无效", "这个链接对应的用户不存在。");
        }

        if (Boolean.TRUE.equals(user.getSubscribe())) {
            user.setSubscribe(false);
            userRepository.save(user);
        }

        return page(siteTitle, avatar, true, maskEmail(user.getEmail()),
                "已退订", "该邮箱已关闭新文章邮件通知，以后不会再收到本站的更新邮件。");
    }

    private ResponseEntity<String> page(String siteTitle, String avatar, boolean success,
                                        String email, String heading, String message) {
        String eyebrow = success ? "SUBSCRIPTION OFF" : "SUBSCRIPTION";
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
          .append("<title>").append(escapeHtml(heading)).append(" - ")
          .append(escapeHtml(siteTitle)).append("</title></head>")
          .append("<body style=\"margin:0;padding:0;background-color:#eef3fb;\">")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
          .append("style=\"background-color:#eef3fb;padding:48px 16px;\"><tr><td align=\"center\">")
          .append("<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
          .append("style=\"width:100%;max-width:560px;background-color:#ffffff;border:1px solid #e3eaf6;")
          .append("border-radius:18px;overflow:hidden;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',")
          .append("'PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif;\">")
          .append("<tr><td style=\"height:6px;background-color:#2563EB;")
          .append("background-image:linear-gradient(90deg,#2563EB 0%,#06B6D4 100%);")
          .append("font-size:0;line-height:0;\">&nbsp;</td></tr>")
          .append("<tr><td style=\"padding:30px 36px 0;\">")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>");
        if (avatar != null) {
            sb.append("<td width=\"42\" style=\"padding-right:10px;\">")
              .append("<img src=\"").append(escapeHtml(avatar)).append("\" width=\"42\" height=\"42\" alt=\"\" ")
              .append("style=\"display:block;border:0;border-radius:50%;\"></td>");
        }
        sb.append("<td style=\"font-size:17px;font-weight:700;color:#0f172a;\">")
          .append(escapeHtml(siteTitle)).append("</td>")
          .append("<td align=\"right\" style=\"font-size:10px;font-weight:700;letter-spacing:3px;color:#2563EB;\">")
          .append(eyebrow).append("</td></tr></table></td></tr>")
          .append("<tr><td style=\"padding:26px 36px 0;\">")
          .append("<h1 style=\"margin:0;font-size:28px;line-height:1.4;color:#0f172a;font-weight:800;\">")
          .append(escapeHtml(heading)).append("</h1>")
          .append("<p style=\"margin:14px 0 0;font-size:15px;line-height:1.85;color:#475569;\">")
          .append(escapeHtml(message)).append("</p>");
        if (email != null && !email.isEmpty()) {
            sb.append("<p style=\"margin:10px 0 0;font-size:13px;line-height:1.7;color:#94a3b8;\">")
              .append(escapeHtml(email)).append("</p>");
        }
        sb.append("</td></tr><tr><td style=\"padding:28px 36px 34px;\">")
          .append("<a href=\"").append(escapeHtml(baseUrl())).append("\" ")
          .append("style=\"display:inline-block;padding:13px 30px;border-radius:999px;background-color:#2563EB;")
          .append("color:#ffffff;font-size:14px;font-weight:700;text-decoration:none;\">返回博客 →</a>")
          .append("</td></tr>")
          .append("<tr><td style=\"padding:18px 36px 20px;background-color:#f8fafc;border-top:1px solid #eef2f7;\">")
          .append("<p style=\"margin:0;font-size:11px;line-height:1.8;color:#94a3b8;\">")
          .append(success ? "如果这是误操作，可以登录个人中心重新开启“邮件订阅”。"
                          : "你也可以登录个人中心管理邮件订阅。")
          .append("</p></td></tr></table></td></tr></table></body></html>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/html;charset=UTF-8"));
        headers.setCacheControl("no-store");
        return new ResponseEntity<>(sb.toString(), headers, HttpStatus.OK);
    }

    private String maskEmail(String email) {
        if (email == null) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String masked = local.length() <= 3
                ? local.substring(0, 1) + "***"
                : local.substring(0, 3) + "****";
        return masked + domain;
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
