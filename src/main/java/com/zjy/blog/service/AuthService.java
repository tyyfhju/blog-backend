package com.zjy.blog.service;

import com.zjy.blog.exception.BusinessException;

import com.zjy.blog.dto.LoginRequest;
import com.zjy.blog.dto.RegisterRequest;
import com.zjy.blog.dto.ResetPasswordRequest;
import com.zjy.blog.entity.User;
import com.zjy.blog.repository.UserRepository;
import com.zjy.blog.security.JwtTokenProvider;
import com.zjy.blog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_LOGIN_FAILS = 5;
    private static final int MAX_RESET_ATTEMPTS = 5;

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

    private String nextCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }

    public void sendEmailCode(String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (redisUtil.get("emailCodeCooldown:" + email) != null) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("该邮箱已注册");
        }

        String code = nextCode();
        redisUtil.set("emailCode:" + email, code, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【博客系统】邮箱验证码");
        message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露。");
        try {
            mailSender.send(message);
            redisUtil.set("emailCodeCooldown:" + email, "1", 60, TimeUnit.SECONDS);
        } catch (MailException e) {
            throw new BusinessException("邮件发送失败，请检查邮箱 SMTP 授权码是否有效", e);
        }
    }

    @Transactional
    public String register(RegisterRequest request) {
        String realCaptcha = redisUtil.get("captcha:" + request.getCaptchaKey());
        if (realCaptcha == null || !realCaptcha.equalsIgnoreCase(request.getCaptchaCode())) {
            throw new BusinessException("图形验证码错误或已过期");
        }

        String realEmailCode = redisUtil.get("emailCode:" + request.getEmail());
        if (realEmailCode == null || !realEmailCode.equals(request.getEmailCode())) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已注册");
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
        String failKey = "loginFail:" + request.getEmail();
        int fails = 0;
        String failStr = redisUtil.get(failKey);
        if (failStr != null) {
            try {
                fails = Integer.parseInt(failStr);
            } catch (NumberFormatException ignored) {
                fails = 0;
            }
        }
        if (fails >= MAX_LOGIN_FAILS) {
            throw new BusinessException("尝试次数过多，请10分钟后再试");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            redisUtil.set(failKey, String.valueOf(fails + 1), 10, TimeUnit.MINUTES);
            throw new BusinessException("邮箱或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        redisUtil.delete(failKey);
        return jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
    }

    public void sendResetPasswordCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new BusinessException("该邮箱未注册");
        }
        if (redisUtil.get("resetCodeCooldown:" + email) != null) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String code = nextCode();
        redisUtil.set("resetCode:" + email, code, 5, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【博客系统】重置密码验证码");
        message.setText("您的重置密码验证码是：" + code + "，有效期5分钟，请勿泄露。");
        try {
            mailSender.send(message);
            redisUtil.set("resetCodeCooldown:" + email, "1", 60, TimeUnit.SECONDS);
        } catch (MailException e) {
            throw new BusinessException("邮件发送失败，请检查邮箱 SMTP 授权码是否有效", e);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("该邮箱未注册");
        }

        String attemptKey = "resetAttempt:" + request.getEmail();
        int attempts = 0;
        String attemptStr = redisUtil.get(attemptKey);
        if (attemptStr != null) {
            try {
                attempts = Integer.parseInt(attemptStr);
            } catch (NumberFormatException ignored) {
                attempts = 0;
            }
        }
        if (attempts >= MAX_RESET_ATTEMPTS) {
            throw new BusinessException("尝试次数过多，请10分钟后再试");
        }

        String realCode = redisUtil.get("resetCode:" + request.getEmail());
        if (realCode == null || !realCode.equals(request.getCode())) {
            redisUtil.set(attemptKey, String.valueOf(attempts + 1), 10, TimeUnit.MINUTES);
            throw new BusinessException("验证码错误或已过期");
        }

        User user = userRepository.findByEmail(request.getEmail()).get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisUtil.delete("resetCode:" + request.getEmail());
        redisUtil.delete(attemptKey);
    }
}