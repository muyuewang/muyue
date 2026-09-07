package com.muyue.common.security;

import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.utils.JwtUtils;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.service.OnlineUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * @author muyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final OnlineUserService onlineUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = jwtUtils.getToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            // 会话白名单校验：JWT 合法但会话不存在 = 已登出 / 已强退 / 已过期（服务端可主动作废）
            if (!onlineUserService.hasSession(token)) {
                log.warn("拦截失效会话 token={}", token.length() > 16 ? token.substring(0, 16) : token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效，请重新登录");
                return;
            }
            String username = jwtUtils.getUsernameFromToken(token);
            if (username != null && SecurityUtils.getAuthentication() == null) {
                try {
                    LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);
                    loginUser.setToken(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    log.warn("令牌解析用户信息失败：{}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(request, response);
    }
}
