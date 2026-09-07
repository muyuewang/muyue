package com.muyue.web.init;

import com.muyue.common.config.properties.UserProperties;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化：保证 admin 账号存在且密码为密文
 *
 * @author muyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;

    @Override
    public void run(String... args) {
        try {
            SysUser admin = userService.selectUserByUserName("admin");
            if (admin == null) {
                log.warn("未检测到 admin 用户，请先执行 sql/oracle/*.sql 初始化脚本");
                return;
            }
            // 初始化脚本中的明文密码自动转为 BCrypt 密文（admin 重置为默认密码，其余用户按原明文加密）
            if (admin.getPassword() != null && admin.getPassword().length() < 60) {
                userService.resetUserPassword(admin.getUserId(), userProperties.getDefaultPassword());
                log.info("已初始化 admin 用户密码为：{}", userProperties.getDefaultPassword());
            }
            for (SysUser user : userService.list()) {
                if (user.getPassword() != null && user.getPassword().length() < 60) {
                    userService.resetUserPassword(user.getUserId(), user.getPassword());
                    log.info("已初始化用户 {} 的登录密码", user.getUserName());
                }
            }
        } catch (Exception e) {
            log.warn("初始化检查失败：{}", e.getMessage());
        }
    }
}
