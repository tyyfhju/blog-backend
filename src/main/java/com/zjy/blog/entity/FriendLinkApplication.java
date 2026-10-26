package com.zjy.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friend_link_application")
public class FriendLinkApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(length = 255)
    private String description;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", length = 128)
    private String userEmail;

    @Column(length = 16)
    private String status = "pending";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
