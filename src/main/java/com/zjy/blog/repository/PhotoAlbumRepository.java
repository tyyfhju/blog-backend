package com.zjy.blog.repository;

import com.zjy.blog.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {
}