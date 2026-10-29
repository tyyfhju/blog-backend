package com.zjy.blog.service;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.dto.ArticleCreateRequest;
import com.zjy.blog.dto.ArticleUpdateRequest;
import com.zjy.blog.entity.Article;
import com.zjy.blog.entity.ArticleLike;
import com.zjy.blog.entity.Category;
import com.zjy.blog.entity.Tag;
import com.zjy.blog.repository.ArticleLikeRepository;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.CategoryRepository;
import com.zjy.blog.repository.CommentRepository;
import com.zjy.blog.repository.TagRepository;
import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    @Autowired
    private CosUtil cosUtil;

    public Page<Article> getPublished(int page, int size) {
        return getPublished(page, size, null, null);
    }

    public Page<Article> search(String keyword, int page, int size) {
        Page<Article> result = articleRepository.searchPublished(1, keyword, PageRequest.of(page, size));
        result.getContent().forEach(this::enrichArticle);
        return result;
    }

    public Page<Article> getPublished(int page, int size, Long categoryId, Long tagId) {
        Page<Article> result;
        if (tagId != null) {
            result = articleRepository.findPublishedByTagId(tagId, 1, PageRequest.of(page, size));
        } else if (categoryId != null) {
            result = articleRepository.findByCategoryIdAndStatusOrderByCreateTimeDesc(categoryId, 1, PageRequest.of(page, size));
        } else {
            result = articleRepository.findByStatusOrderByCreateTimeDesc(1, PageRequest.of(page, size));
        }
        result.getContent().forEach(this::enrichArticle);
        return result;
    }

    public Article getById(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article != null) enrichArticle(article);
        return article;
    }

    private void enrichArticle(Article article) {
        // categoryName
        if (article.getCategoryId() != null) {
            categoryRepository.findById(article.getCategoryId()).ifPresent(c -> article.setCategoryName(c.getName()));
        }
        // tagNames
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            article.setTagNames(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        } else {
            article.setTagNames(new ArrayList<>());
        }
        // commentCount
        article.setCommentCount(commentRepository.countByArticleId(article.getId()));
    }

    public Page<Article> listAll(int page, int size) {
        return articleRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public Article create(ArticleCreateRequest req, Long userId) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCategoryId(req.getCategoryId());
        article.setUserId(userId);
        article.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        article.setIsTop(req.getIsTop() != null ? req.getIsTop() : 0);
        article.setIsRecommend(req.getIsRecommend() != null ? req.getIsRecommend() : 0);
        article.setCoverUrl(req.getCoverUrl());
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(req.getTagIds()));
            article.setTags(tags);
        }
        return articleRepository.save(article);
    }

    @Transactional
    public Article update(Long id, ArticleUpdateRequest req) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        if (req.getTitle() != null) article.setTitle(req.getTitle());
        if (req.getContent() != null) article.setContent(req.getContent());
        if (req.getSummary() != null) article.setSummary(req.getSummary());
        if (req.getCategoryId() != null) article.setCategoryId(req.getCategoryId());
        if (req.getStatus() != null) article.setStatus(req.getStatus());
        if (req.getIsTop() != null) article.setIsTop(req.getIsTop());
        if (req.getIsRecommend() != null) article.setIsRecommend(req.getIsRecommend());
        if (req.getCoverUrl() != null) article.setCoverUrl(req.getCoverUrl());
        if (req.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(req.getTagIds()));
            article.setTags(tags);
        }
        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article != null && article.getCoverUrl() != null) {
            cosUtil.deleteByUrl(article.getCoverUrl());
        }
        commentRepository.deleteByArticleId(id);
        articleLikeRepository.deleteByArticleId(id);
        if (article != null && article.getTags() != null) {
            article.getTags().clear();
            articleRepository.save(article);
        }
        articleRepository.deleteById(id);
    }

    public Article getPreviousArticle(Long id) {
        return articleRepository.findTopByIdLessThanAndStatusOrderByIdDesc(id, 1).orElse(null);
    }

    public Article getNextArticle(Long id) {
        return articleRepository.findTopByIdGreaterThanAndStatusOrderByIdAsc(id, 1).orElse(null);
    }

    public List<Article> getRecommended() {
        List<Article> list = articleRepository.findTop2ByStatusAndIsRecommendOrderByCreateTimeDesc(1, 1);
        list.forEach(this::enrichArticle);
        return list;
    }

    /**
     * 文章阅读量 +1
     */
    @Transactional
    public void incrementViewCount(Long articleId) {
        articleRepository.incrementViewCount(articleId);
    }

    /**
     * 相关文章推荐（同分类，排除当前文章）
     */
    public List<Article> getRelatedArticles(Long articleId) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null || article.getCategoryId() == null) return new ArrayList<>();
        List<Article> list = articleRepository
                .findTop5ByCategoryIdAndStatusAndIdNotOrderByCreateTimeDesc(
                        article.getCategoryId(), 1, articleId, PageRequest.of(0, 5));
        list.forEach(this::enrichArticle);
        return list;
    }

    /**
     * 点赞
     */
    @Transactional
    public boolean like(Long articleId, Long userId) {
        if (articleLikeRepository.existsByArticleIdAndUserId(articleId, userId)) {
            return false;
        }
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        articleLikeRepository.save(like);
        articleRepository.incrementLikeCount(articleId);
        return true;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlike(Long articleId, Long userId) {
        Optional<ArticleLike> existing = articleLikeRepository.findByArticleIdAndUserId(articleId, userId);
        if (!existing.isPresent()) return false;
        articleLikeRepository.delete(existing.get());
        articleRepository.decrementLikeCount(articleId);
        return true;
    }

    /**
     * 检查当前用户是否已点赞
     */
    public boolean isLiked(Long articleId, Long userId) {
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
    }

}