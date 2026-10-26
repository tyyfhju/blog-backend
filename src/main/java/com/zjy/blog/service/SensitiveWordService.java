package com.zjy.blog.service;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SensitiveWordService {

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
