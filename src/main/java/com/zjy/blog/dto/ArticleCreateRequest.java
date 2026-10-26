package com.zjy.blog.dto;

import lombok.Data;
import java.util.Set;

@Data
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private Integer status;
    private Integer isTop;
    private Integer isRecommend;
    private String coverUrl;
    private Set<Long> tagIds;
}