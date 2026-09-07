package com.muyue.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyue.common.core.domain.R;
import com.muyue.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未认证（未登录 / 令牌失效）处理
 *
 * @author muyue
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        int code = HttpStatus.UNAUTHORIZED.value();
        String msg = "认证失败，无法访问系统资源（请先登录）";
        try {
            String json = new ObjectMapper().writeValueAsString(R.fail(code, msg));
            ServletUtils.renderString(response, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
