package com.zjy.blog.service;

import com.zjy.blog.dto.LoginRequest;
import com.zjy.blog.dto.RegisterRequest;
import com.zjy.blog.dto.ResetPasswordRequest;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.UserRepository;
import com.zjy.blog.security.JwtTokenProvider;
import com.zjy.blog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmailCode(String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("邮箱格式不正确");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已注册");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        redisUtil.set("emailCode:" + email, code, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【博客系统】邮箱验证码");
        message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露。");
        mailSender.send(message);
    }

    @Transactional
    public String register(RegisterRequest request) {
        String realCaptcha = redisUtil.get("captcha:" + request.getCaptchaKey());
        if (realCaptcha == null || !realCaptcha.equalsIgnoreCase(request.getCaptchaCode())) {
            throw new RuntimeException("图形验证码错误或已过期");
        }

        String realEmailCode = redisUtil.get("emailCode:" + request.getEmail());
        if (realEmailCode == null || !realEmailCode.equals(request.getEmailCode())) {
            throw new RuntimeException("邮箱验证码错误或已过期");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setNickname("用户" + System.currentTimeMillis() % 10000000000L);
        userRepository.save(user);

        redisUtil.delete("captcha:" + request.getCaptchaKey());
        redisUtil.delete("emailCode:" + request.getEmail());

        return jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
    }

    public void sendResetPasswordCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱未注册");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        redisUtil.set("resetCode:" + email, code, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【博客系统】重置密码验证码");
        message.setText("您的重置密码验证码是：" + code + "，有效期5分钟，请勿泄露。");
        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("该邮箱未注册");
        }

        String realCode = redisUtil.get("resetCode:" + request.getEmail());
        if (realCode == null || !realCode.equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        User user = userRepository.findByEmail(request.getEmail()).get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisUtil.delete("resetCode:" + request.getEmail());
    }
}