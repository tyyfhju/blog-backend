package com.zjy.blog.service;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.entity.User;
import com.zjy.blog.repository.ArticleLikeRepository;
import com.zjy.blog.repository.CommentRepository;
import com.zjy.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    public Page<User> list(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public User updateUser(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        boolean lastAdmin = "admin".equals(user.getRole()) && userRepository.countByRole("admin") <= 1;
        if (lastAdmin && updated.getRole() != null && !"admin".equals(updated.getRole())) {
            throw new BusinessException("不能取消最后一个管理员");
        }
        if (lastAdmin && updated.getStatus() != null && updated.getStatus() != 1) {
            throw new BusinessException("不能禁用最后一个管理员");
        }
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getStatus() != null) user.setStatus(updated.getStatus());
        if (updated.getRole() != null) user.setRole(updated.getRole());
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("admin".equals(user.getRole()) && userRepository.countByRole("admin") <= 1) {
            throw new BusinessException("不能删除最后一个管理员");
        }
        commentRepository.deleteByUserId(id);
        articleLikeRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在")).getId();
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}