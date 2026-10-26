package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.BlogSetting;
import com.zjy.blog.service.BlogSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private/blog-setting")
public class AdminBlogSettingController {

    @Autowired
    private BlogSettingService settingService;

    @GetMapping
    public BlogSetting getSetting() {
        return settingService.getSetting();
    }

    @PutMapping
    public BlogSetting updateSetting(@RequestBody BlogSetting setting) {
        return settingService.updateSetting(setting);
    }
}