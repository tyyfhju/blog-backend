package com.zjy.blog.controller;

import com.zjy.blog.entity.Article;
import com.zjy.blog.entity.RobotDialogue;
import com.zjy.blog.repository.ArticleRepository;
import com.zjy.blog.repository.RobotDialogueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);

    private static final int RATE_LIMIT = 10;
    private static final long RATE_WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestLog = new ConcurrentHashMap<>();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.api-url:}")
    private String apiUrl;

    @Value("${app.ai.model:gpt-3.5-turbo}")
    private String model;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RobotDialogueRepository robotDialogueRepository;

    private final RestTemplate restTemplate;

    public AIChatController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
    }

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        ConcurrentLinkedDeque<Long> timestamps = requestLog.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > RATE_WINDOW_MS) {
            timestamps.pollFirst();
        }
        if (timestamps.size() >= RATE_LIMIT) {
            return true;
        }
        timestamps.addLast(now);
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/dialogues/random")
    public ResponseEntity<Map<String, Object>> randomDialogue() {
        Map<String, Object> result = new HashMap<>();
        RobotDialogue dialogue = robotDialogueRepository.findRandomEnabled();
        if (dialogue != null) {
            result.put("content", dialogue.getContent());
        } else {
            result.put("content", "你好！有什么可以帮你的吗？");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> body,
                                                     HttpServletRequest request) {
        String ip = getClientIp(request);
        if (isRateLimited(ip)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "请求太频繁，请稍后再试");
            log.warn("IP {} 触发频率限制", ip);
            return ResponseEntity.status(429).body(error);
        }

        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "message is required");
            return ResponseEntity.badRequest().body(error);
        }

        List<Article> relatedArticles = searchArticles(message);
        String context = buildContext(relatedArticles, message);
        String reply = callAI(message, context);

        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("relatedArticles", relatedArticles.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            return m;
        }).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    private List<Article> searchArticles(String keyword) {
        return articleRepository
                .searchPublished(1, keyword, PageRequest.of(0, 5))
                .getContent();
    }

    private String buildContext(List<Article> articles, String query) {
        if (articles.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("以下是博客中与用户问题相关的文章：\n");
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            String content = a.getContent() != null ? a.getContent() : "";
            content = content.replaceAll("<[^>]+>", "").trim();
            if (content.length() > 300) {
                content = content.substring(0, 300) + "...";
            }
            sb.append(i + 1).append(". 【").append(a.getTitle()).append("】").append(content).append("\n");
        }
        return sb.toString();
    }

    private String callAI(String userMessage, String context) {
        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            log.warn("AI API 未配置, apiUrl={}, apiKey={}", apiUrl, apiKey.isEmpty() ? "(空)" : "(已设置)");
            return fallbackReply(userMessage);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, Object>> messages = new ArrayList<>();

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是博客小助手，请用简洁友好的方式回答用户问题。"
                    + (context.isEmpty() ? "" : "如果用户的问题能在以下文章中找到答案，请引用相关文章。"));
            messages.add(systemMsg);

            if (!context.isEmpty()) {
                Map<String, Object> ctxMsg = new HashMap<>();
                ctxMsg.put("role", "system");
                ctxMsg.put("content", context);
                messages.add(ctxMsg);
            }

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", model);
            reqBody.put("messages", messages);
            reqBody.put("max_tokens", 500);
            reqBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);

            log.info("调用 AI API: url={}, model={}", apiUrl, model);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

            log.info("AI API 响应: {}", response);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> msg = (Map<String, Object>) choice.get("message");
                    Object content = msg.get("content");
                    return content != null ? content.toString() : "抱歉，我暂时无法回答这个问题。";
                }
            }

            log.warn("AI API 返回了意外的响应格式: {}", response);
            return "抱歉，AI 服务返回了意外的结果。";
        } catch (Exception e) {
            log.error("调用 AI API 失败: {}", e.getMessage(), e);
            return fallbackReply(userMessage);
        }
    }

    private String fallbackReply(String message) {
        List<Article> articles = articleRepository
                .searchPublished(1, message, PageRequest.of(0, 3))
                .getContent();

        if (articles.isEmpty()) {
            return "你好！我是博客小助手。关于「" + message + "」，暂时没有找到相关的文章。你可以尝试其他关键词搜索，或者直接浏览博客文章。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("关于「").append(message).append("」，博客中有以下相关文章：\n");
        for (int i = 0; i < articles.size(); i++) {
            sb.append(i + 1).append(". ").append(articles.get(i).getTitle()).append("\n");
        }
        sb.append("\n你可以点击文章标题查看详情。如有其他问题，随时问我！");
        return sb.toString();
    }
}
