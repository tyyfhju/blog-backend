package com.zjy.blog.service;

import com.zjy.blog.entity.BlogSetting;
import com.zjy.blog.repository.BlogSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogSettingService {

    @Autowired
    private BlogSettingRepository settingRepository;

    public BlogSetting getSetting() {
        return settingRepository.findTopByOrderByIdAsc().orElse(null);
    }

    @Transactional
    public BlogSetting updateSetting(BlogSetting setting) {
        BlogSetting existing = settingRepository.findTopByOrderByIdAsc().orElse(null);

        if (existing == null) {
            setting.setId(null);
            return settingRepository.save(setting);
        }

        if (setting.getSiteTitle() != null) existing.setSiteTitle(setting.getSiteTitle());
        if (setting.getSiteSubtitle() != null) existing.setSiteSubtitle(setting.getSiteSubtitle());
        if (setting.getAuthorName() != null) existing.setAuthorName(setting.getAuthorName());
        if (setting.getAuthorBio() != null) existing.setAuthorBio(setting.getAuthorBio());
        if (setting.getAvatarUrl() != null) existing.setAvatarUrl(setting.getAvatarUrl());
        if (setting.getFooterText() != null) existing.setFooterText(setting.getFooterText());
        if (setting.getIcp() != null) existing.setIcp(setting.getIcp());
        if (setting.getPolice() != null) existing.setPolice(setting.getPolice());
        if (setting.getDefaultAvatarUrl() != null) existing.setDefaultAvatarUrl(setting.getDefaultAvatarUrl());
        if (setting.getDefaultUserAvatarUrl() != null) existing.setDefaultUserAvatarUrl(setting.getDefaultUserAvatarUrl());
        if (setting.getDefaultAdminAvatarUrl() != null) existing.setDefaultAdminAvatarUrl(setting.getDefaultAdminAvatarUrl());
        if (setting.getHomeImageUrl() != null) existing.setHomeImageUrl(setting.getHomeImageUrl());
        if (setting.getGithubUrl() != null) existing.setGithubUrl(setting.getGithubUrl());
        if (setting.getBilibiliUrl() != null) existing.setBilibiliUrl(setting.getBilibiliUrl());
        if (setting.getQqNumber() != null) existing.setQqNumber(setting.getQqNumber());
        if (setting.getEmail() != null) existing.setEmail(setting.getEmail());
        if (setting.getWechatId() != null) existing.setWechatId(setting.getWechatId());
        if (setting.getWelcomeText() != null) existing.setWelcomeText(setting.getWelcomeText());
        if (setting.getTechStack() != null) existing.setTechStack(setting.getTechStack());
        if (setting.getAboutContent() != null) existing.setAboutContent(setting.getAboutContent());
        if (setting.getFrontendContent() != null) existing.setFrontendContent(setting.getFrontendContent());

        return settingRepository.save(existing);
    }
}
