package com.muyue.common.exception;

import com.muyue.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author muyue
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error("请求地址'{}'，业务异常：{}", request.getRequestURI(), e.getMessage());
        Integer code = e.getCode();
        return code == null ? R.fail(e.getMessage()) : R.fail(code, e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        return R.fail(message);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        return R.fail(message);
    }

    /** 拒绝访问（权限不足） */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e,
                                               HttpServletRequest request) {
        log.warn("请求地址'{}'，权限校验失败：{}", request.getRequestURI(), e.getMessage());
        return R.fail(403, "没有权限访问该资源");
    }

    /** 系统异常 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("请求地址'{}'，发生系统异常：", request.getRequestURI(), e);
        return R.fail(e.getMessage());
    }
}
