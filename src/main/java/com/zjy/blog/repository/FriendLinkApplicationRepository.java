package com.zjy.blog.repository;

import com.zjy.blog.entity.FriendLinkApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendLinkApplicationRepository extends JpaRepository<FriendLinkApplication, Long> {

    List<FriendLinkApplication> findAllByOrderByCreateTimeDesc();

    List<FriendLinkApplication> findByUrl(String url);

    void deleteByUrl(String url);
}
