package com.zjy.blog.repository;

import com.zjy.blog.entity.Talk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TalkRepository extends JpaRepository<Talk, Long> {

    @Query("SELECT t FROM Talk t WHERE t.status = 1 ORDER BY t.isTop DESC, t.createTime DESC")
    Page<Talk> findPublished(Pageable pageable);
}
