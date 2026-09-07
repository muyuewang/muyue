package com.muyue.common.utils;

import com.muyue.common.config.properties.JwtProperties;
import com.muyue.common.core.domain.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author muyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_DEPT_ID = "deptId";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌
     */
    public String createToken(LoginUser loginUser) {
        long now = System.currentTimeMillis();
        long expire = jwtProperties.getExpireTime() * 60 * 1000L;
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, loginUser.getUserId());
        claims.put(CLAIM_USERNAME, loginUser.getUsername());
        claims.put(CLAIM_DEPT_ID, loginUser.getDeptId());
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(now + expire);
        return Jwts.builder()
                .claims(claims)
                .subject(loginUser.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expire))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析令牌
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT 校验失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求中获取令牌
     */
    public String getToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
