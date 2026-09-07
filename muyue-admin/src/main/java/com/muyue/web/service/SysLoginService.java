package com.muyue.web.service;

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

import java.time.LocalDateTime;

/**
 * 登录服务
 *
 * @author muyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ISysUserService userService;
    private final CaptchaService captchaService;
    private final ISysLogininforService logininforService;
    private final OnlineUserService onlineUserService;

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
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException e) {
            recordLogin(username, "1", "用户名或密码错误");
            throw new ServiceException("用户名或密码错误");
        } catch (Exception e) {
            recordLogin(username, "1", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
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
