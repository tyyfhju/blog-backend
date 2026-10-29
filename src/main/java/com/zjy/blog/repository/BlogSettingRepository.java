package com.zjy.blog.repository;

import com.zjy.blog.entity.BlogSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogSettingRepository extends JpaRepository<BlogSetting, Long> {
    Optional<BlogSetting> findTopByOrderByIdAsc();
}