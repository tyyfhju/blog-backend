package com.zjy.blog.repository;

import com.zjy.blog.entity.BlogSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogSettingRepository extends JpaRepository<BlogSetting, Long> {
}