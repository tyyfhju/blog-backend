package com.zjy.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "photo")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(length = 255)
    private String description;

    @Column(name = "album_id")
    private Long albumId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private PhotoAlbum album;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}