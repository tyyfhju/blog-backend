package com.zjy.blog.security;

/**
 * 单设备登录会话 key。Redis 里只保留当前有效 token 的 jti。
 */
public final class AuthSession {

    public static final String KEY_PREFIX = "login:session:";

    public static String key(String email) {
        return KEY_PREFIX + email;
    }

    private AuthSession() {
    }
}
