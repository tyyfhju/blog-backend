package com.zjy.blog.controller.admin;

import com.zjy.blog.dto.DashboardStatsResponse;
import com.zjy.blog.entity.Article;
import com.zjy.blog.entity.Comment;
import com.zjy.blog.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/private/dashboard")
public class AdminDashboardController {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PhotoRepository photoRepository;

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        DashboardStatsResponse res = new DashboardStatsResponse();
        res.setArticleCount(articleRepository.count());
        res.setUserCount(userRepository.count());
        res.setCategoryCount(categoryRepository.count());
        res.setTagCount(tagRepository.count());
        res.setCommentCount(commentRepository.count());
        res.setMessageCount(messageRepository.count());
        res.setPhotoCount(photoRepository.count());

        PageRequest top5 = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createTime"));
        List<Article> recentArticles = articleRepository.findAll(top5).getContent();
        res.setRecentArticles(recentArticles);

        List<Comment> recentComments = commentRepository.findAll(top5).getContent();
        res.setRecentComments(recentComments);

        return res;
    }
}
