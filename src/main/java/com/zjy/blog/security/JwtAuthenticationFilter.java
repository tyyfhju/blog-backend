package com.zjy.blog.security;

import com.zjy.blog.entity.User;
import com.zjy.blog.repository.UserRepository;
import com.zjy.blog.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = tokenProvider.parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = claims.getSubject();
        String sessionId = claims.getId();

        User user;
        try {
            user = userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            filterChain.doFilter(request, response);
            return;
        }

        String activeSessionId;
        try {
            activeSessionId = redisUtil.get(AuthSession.key(email));
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 同一账号后一次登录会覆盖 Redis 里的 jti，旧 token 到这里就失效。
        // 登录/注册/验证码接口必须放行，否则旧 token 会让用户无法重新登录。
        if (sessionId == null || !sessionId.equals(activeSessionId)) {
            if (isAuthPath(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
            writeSessionReplaced(response);
            return;
        }

        String role = user.getRole() != null ? user.getRole() : "user";
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isAuthPath(String uri) {
        return uri.startsWith("/api/auth/") || uri.startsWith("/api/captcha/");
    }

    private void writeSessionReplaced(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":\"SESSION_REPLACED\",\"message\":\"账号已在其他设备登录，请重新登录\"}");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
