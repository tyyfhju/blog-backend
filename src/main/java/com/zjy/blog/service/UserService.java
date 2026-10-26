package com.zjy.blog.service;

import com.zjy.blog.entity.User;
import com.zjy.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> list(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public User updateUser(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getStatus() != null) user.setStatus(updated.getStatus());
        if (updated.getRole() != null) user.setRole(updated.getRole());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在")).getId();
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}