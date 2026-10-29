package com.zjy.blog.controller;

import com.zjy.blog.entity.Article;
import com.zjy.blog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
public class SitemapController {

    @Autowired
    private ArticleRepository articleRepository;

    private static final String BASE_URL = "https://xn--hlry9isyc4tas57t.com";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首页
        sb.append("  <url>\n");
        sb.append("    <loc>").append(BASE_URL).append("</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("  </url>\n");

        // 主要页面
        String[] pages = {"/articles", "/archive", "/category", "/photos", "/about"};
        for (String p : pages) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(BASE_URL).append(p).append("</loc>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        // 已发布文章
        Page<Article> articles = articleRepository.findByStatusOrderByCreateTimeDesc(1, PageRequest.of(0, 1000));
        for (Article a : articles.getContent()) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(BASE_URL).append("/article/").append(a.getId()).append("</loc>\n");
            if (a.getUpdateTime() != null) {
                sb.append("    <lastmod>").append(a.getUpdateTime().format(DATE_FMT)).append("</lastmod>\n");
            } else if (a.getCreateTime() != null) {
                sb.append("    <lastmod>").append(a.getCreateTime().format(DATE_FMT)).append("</lastmod>\n");
            }
            sb.append("    <changefreq>monthly</changefreq>\n");
            sb.append("    <priority>0.6</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }
}
