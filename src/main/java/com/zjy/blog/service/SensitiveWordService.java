package com.zjy.blog.service;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class SensitiveWordService {

    private static final List<String> ALLOW_WORDS = Arrays.asList(
            "用户", "管理员", "网站", "密码", "手机", "视频", "消息", "通知",
            "账号", "会员", "客户", "博客", "文章", "评论", "留言", "测试",
            "系统", "数据", "邮箱", "首页", "分类", "标签", "归档", "相册",
            "关于", "搜索"
    );

    private SensitiveWordBs sensitiveWordBs;

    @PostConstruct
    public void init() {
        sensitiveWordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(false)
                .enableNumCheck(false)
                .enableEmailCheck(false)
                .enableUrlCheck(false)
                .wordAllow(WordAllows.chains(WordAllows.defaults(), () -> ALLOW_WORDS))
                .init();
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        return sensitiveWordBs.replace(text);
    }

    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        return sensitiveWordBs.contains(text);
    }

    public String findAll(String text) {
        if (text == null || text.isEmpty()) return "";
        return String.join(", ", sensitiveWordBs.findAll(text));
    }
}
