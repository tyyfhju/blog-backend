package com.zjy.blog.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(length = 512)
    private String summary;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "is_top")
    private Integer isTop = 0;

    @Column(name = "is_recommend")
    private Integer isRecommend = 0;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @Transient
    private String categoryName;

    @Transient
    private java.util.List<String> tagNames;

    @Transient
    private Long commentCount = 0L;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public java.util.List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(java.util.List<String> tagNames) {
        this.tagNames = tagNames;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}