package com.muyue.web.init;

import com.muyue.common.config.properties.JwtProperties;
import com.muyue.common.config.properties.UserProperties;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化：保证 admin 账号存在且密码为密文；安全配置自检
 *
 * @author muyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_JWT_SECRET = "muyueScaffoldSecretKeyForJwtTokenSignMustBeLongEnough2026";

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;
    private final JwtProperties jwtProperties;

    @Override
    public void run(String... args) {
        checkSecurity();
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

    /** 安全自检：默认 JWT 密钥 / 默认密码给出显著告警 */
    private void checkSecurity() {
        if (DEFAULT_JWT_SECRET.equals(jwtProperties.getSecret())) {
            log.warn("======================================================================");
            log.warn("当前正在使用默认 JWT 密钥！任何拿到源码的人都能伪造登录令牌。");
            log.warn("生产环境请设置环境变量 MUYUE_JWT_SECRET 为随机长字符串后重新启动。");
            log.warn("======================================================================");
        }
        if ("admin123".equals(userProperties.getDefaultPassword())) {
            log.warn("用户默认密码仍为 admin123，上线前请修改 muyue.user.default-password 并重置相关用户密码。");
        }
    }
}
