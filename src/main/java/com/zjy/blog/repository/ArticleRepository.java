package com.zjy.blog.repository;

import com.zjy.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Optional<Article> findTopByIdLessThanAndStatusOrderByIdDesc(Long id, Integer status);
    Optional<Article> findTopByIdGreaterThanAndStatusOrderByIdAsc(Long id, Integer status);

    List<Article> findTop2ByStatusAndIsRecommendOrderByCreateTimeDesc(Integer status, Integer isRecommend);

    long countByCategoryIdAndStatus(Long categoryId, Integer status);

    Page<Article> findByCategoryIdAndStatusOrderByCreateTimeDesc(Long categoryId, Integer status, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM article a " +
            "INNER JOIN article_tag at ON a.id = at.article_id " +
            "WHERE at.tag_id = :tagId AND a.status = :status", nativeQuery = true)
    long countPublishedByTagId(@Param("tagId") Long tagId, @Param("status") Integer status);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.id = :tagId AND a.status = :status ORDER BY a.createTime DESC")
    Page<Article> findPublishedByTagId(@Param("tagId") Long tagId, @Param("status") Integer status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY a.createTime DESC")
    Page<Article> searchPublished(@Param("status") Integer status,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount - 1 WHERE a.id = :id AND a.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    List<Article> findTop5ByCategoryIdAndStatusAndIdNotOrderByCreateTimeDesc(
            Long categoryId, Integer status, Long id, Pageable pageable);
}