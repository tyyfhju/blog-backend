package com.zjy.blog.repository;

import com.zjy.blog.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
    long countByArticleId(Long articleId);
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    void deleteByArticleId(Long articleId);
    void deleteByUserId(Long userId);
}
