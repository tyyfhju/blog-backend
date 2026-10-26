package com.zjy.blog.dto;

import com.zjy.blog.entity.Article;
import com.zjy.blog.entity.Comment;
import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsResponse {
    private long articleCount;
    private long userCount;
    private long categoryCount;
    private long tagCount;
    private long commentCount;
    private long messageCount;
    private long talkCount;
    private long photoCount;
    private List<Article> recentArticles;
    private List<Comment> recentComments;
}
