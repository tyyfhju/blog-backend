package com.zjy.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blog_setting")
public class BlogSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteTitle;
    private String siteSubtitle;
    private String authorName;
    private String authorBio;
    private String avatarUrl;
    private String footerText;
    private String icp;
    private String police;
    private String defaultAvatarUrl;
    private String defaultUserAvatarUrl;
    private String defaultAdminAvatarUrl;
    private String homeImageUrl;   // 首页主图

    private String githubUrl;       // GitHub 主页
    private String bilibiliUrl;     // B站主页
    private String qqNumber;        // QQ 号
    private String email;           // 邮箱
    private String wechatId;        // 微信号

    @Column(columnDefinition = "TEXT")
    private String welcomeText;     // 欢迎语

    @Column(columnDefinition = "TEXT")
    private String techStack;       // 技术栈

    @Column(columnDefinition = "TEXT")
    private String aboutContent;    // 关于页正文

    @Column(columnDefinition = "TEXT")
    private String frontendContent; // 前台可见文案覆盖（JSON）

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}