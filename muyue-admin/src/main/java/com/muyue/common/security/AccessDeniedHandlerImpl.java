package com.muyue.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyue.common.core.domain.R;
import com.muyue.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 权限不足处理
 *
 * @author muyue
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        int code = HttpStatus.FORBIDDEN.value();
        String msg = "没有权限访问该资源";
        try {
            String json = new ObjectMapper().writeValueAsString(R.fail(code, msg));
            ServletUtils.renderString(response, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
