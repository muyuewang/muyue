package com.muyue.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.domain.entity.SysOperLog;
import com.muyue.common.core.domain.model.LoginBody;
import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.utils.IpUtils;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.common.utils.ServletUtils;
import com.muyue.system.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 操作日志切面（基于 @Log 注解）
 *
 * @author muyue
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final int MAX_LENGTH = 1800;
    private static final String[] SENSITIVE_KEYS = {"password", "oldpassword", "newpassword", "secret"};

    private final ISysOperLogService operLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                handleLog(joinPoint, controllerLog, exception, result, System.currentTimeMillis() - start);
            } catch (Exception ignore) {
                log.warn("记录操作日志异常：{}", ignore.getMessage());
            }
        }
    }

    protected void handleLog(ProceedingJoinPoint joinPoint, Log controllerLog, Exception e, Object result, long costTime) {
        SysOperLog operLog = new SysOperLog();
        operLog.setOperTime(LocalDateTime.now());
        operLog.setCostTime(costTime);
        operLog.setStatus(e == null ? 0 : 1);
        operLog.setTitle(controllerLog.title());
        operLog.setBusinessType(controllerLog.businessType().value());
        operLog.setOperatorType(controllerLog.operatorType().value());

        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            operLog.setOperIp(IpUtils.getIpAddr(request));
            operLog.setOperUrl(substring(request.getRequestURI(), 255));
            operLog.setRequestMethod(request.getMethod());
        }

        // 操作人
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser != null ? loginUser.getUsername() : findUsername(joinPoint.getArgs());
        operLog.setOperName(username == null ? "anonymous" : username);
        operLog.setDeptName(loginUser != null && loginUser.getUser().getDeptName() != null
                ? loginUser.getUser().getDeptName() : "");

        // 方法全路径
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        operLog.setMethod(substring(joinPoint.getTarget().getClass().getName() + "." + method.getName() + "()", 200));

        if (controllerLog.isSaveRequestData()) {
            operLog.setOperParam(substring(toJson(buildParams(joinPoint)), MAX_LENGTH));
        }
        if (controllerLog.isSaveResponseData() && result != null) {
            operLog.setJsonResult(substring(toJson(result), MAX_LENGTH));
        }
        if (e != null) {
            operLog.setErrorMsg(substring(e.getMessage(), MAX_LENGTH));
        }
        // 采集完成后异步落库，避免日志写入拖慢业务接口响应
        CompletableFuture.runAsync(() -> operLogService.insertOperlog(operLog))
                .exceptionally(ex -> {
                    log.warn("异步保存操作日志失败：{}", ex.getMessage());
                    return null;
                });
    }

    /**
     * 组装请求参数（过滤流对象与文件，敏感字段脱敏）
     */
    private Map<String, Object> buildParams(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new LinkedHashMap<>();
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return params;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof BindingResult) {
                continue;
            }
            String name = names != null && i < names.length ? names[i] : "param" + i;
            params.put(name, isSensitive(name) ? "******" : arg);
        }
        return params;
    }

    private boolean isSensitive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String key : SENSITIVE_KEYS) {
            if (lower.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 未登录场景（如登录接口）尝试从参数中获取用户名
     */
    private String findUsername(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof LoginBody loginBody) {
                return loginBody.getUsername();
            }
        }
        return null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String substring(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() > length ? value.substring(0, length) : value;
    }
}
