package com.muyue.web.service;

import com.muyue.common.cache.CacheDelegate;
import com.muyue.common.core.domain.entity.SysLogininfor;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.JwtUtils;
import com.muyue.common.utils.ServletUtils;
import com.muyue.system.service.ISysLogininforService;
import com.muyue.system.service.ISysUserService;
import com.muyue.system.service.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 登录服务（含密码错误次数锁定，防暴力破解）
 *
 * @author muyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginService {

    /** 密码错误计数缓存键前缀 */
    private static final String PWD_ERR_KEY = "pwd_err_cnt:";
    /** 允许的最大连续失败次数 */
    private static final int MAX_RETRY = 5;
    /** 锁定时长（每次失败重新计时） */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ISysUserService userService;
    private final CaptchaService captchaService;
    private final ISysLogininforService logininforService;
    private final OnlineUserService onlineUserService;
    private final CacheDelegate cacheDelegate;

    /**
     * 登录并返回令牌
     */
    public String login(String username, String password, String code, String uuid) {
        try {
            captchaService.validateCaptcha(uuid, code);
        } catch (Exception e) {
            recordLogin(username, "1", e.getMessage());
            throw e;
        }
        // 防暴力破解：连续失败达到上限后锁定
        Integer errCount = cacheDelegate.get(PWD_ERR_KEY + username, Integer.class);
        if (errCount != null && errCount >= MAX_RETRY) {
            recordLogin(username, "1", "密码错误次数过多，账号已锁定");
            throw new ServiceException("密码错误次数过多，账号已锁定 10 分钟，请稍后再试");
        }
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException e) {
            String msg = incrementPwdErrCount(username);
            recordLogin(username, "1", "用户名或密码错误：" + msg);
            throw new ServiceException(msg);
        } catch (Exception e) {
            recordLogin(username, "1", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        // 登录成功清空失败计数
        cacheDelegate.delete(PWD_ERR_KEY + username);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            loginUser.setIpaddr(ServletUtils.getIpAddress(request));
        }
        recordLoginInfo(loginUser.getUserId(), loginUser.getIpaddr());
        recordLogin(username, "0", "登录成功");
        String token = jwtUtils.createToken(loginUser);
        loginUser.setToken(token);
        loginUser.setLoginTime(System.currentTimeMillis());
        onlineUserService.save(token, loginUser);
        return token;
    }

    /**
     * 累计密码失败次数并返回提示信息
     */
    private String incrementPwdErrCount(String username) {
        Integer cnt = cacheDelegate.get(PWD_ERR_KEY + username, Integer.class);
        int next = (cnt == null ? 0 : cnt) + 1;
        cacheDelegate.put(PWD_ERR_KEY + username, next, LOCK_DURATION);
        int remain = MAX_RETRY - next;
        if (remain <= 0) {
            return "用户名或密码错误，失败次数过多，账号已锁定 10 分钟";
        }
        return "用户名或密码错误，还可尝试 " + remain + " 次";
    }

    /**
     * 记录登录信息到用户表（最后登录 IP / 时间）
     */
    public void recordLoginInfo(Long userId, String ip) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setLoginIp(ip);
        user.setLoginDate(LocalDateTime.now());
        userService.updateById(user);
    }

    /**
     * 记录登录日志
     */
    private void recordLogin(String username, String status, String msg) {
        SysLogininfor info = new SysLogininfor();
        info.setUserName(username);
        info.setIpaddr(ServletUtils.getIpAddress(ServletUtils.getRequest()));
        info.setStatus(status);
        info.setMsg(msg == null ? "" : msg);
        info.setAccessTime(LocalDateTime.now());
        logininforService.save(info);
    }
}
