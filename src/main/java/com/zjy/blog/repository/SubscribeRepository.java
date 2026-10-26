package com.zjy.blog.repository;

import com.zjy.blog.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {
    List<Subscribe> findByIsActive(Integer isActive);
    boolean existsByEmail(String email);
}
