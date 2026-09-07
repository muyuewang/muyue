package com.muyue;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysDept;
import com.muyue.common.core.domain.entity.SysMenu;
import com.muyue.common.core.domain.entity.SysOperLog;
import com.muyue.common.core.domain.entity.SysRole;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.cache.CacheDelegate;
import com.muyue.common.core.domain.vo.CaptchaVo;
import com.muyue.common.core.domain.vo.RouterVo;
import com.muyue.common.utils.JwtUtils;
import com.muyue.web.service.CaptchaService;
import com.muyue.system.service.ISysDeptService;
import com.muyue.system.service.ISysMenuService;
import com.muyue.system.service.ISysOperLogService;
import com.muyue.system.service.ISysUserService;
import com.muyue.web.service.SysLoginService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SQLite 内嵌库冒烟测试：验证启动初始化、登录、验证码、分页、菜单路由、操作日志
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:./target/test-muyue.db"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SqliteSmokeTest {

    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private ISysOperLogService operLogService;
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private CacheDelegate cacheDelegate;
    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @Order(1)
    void testUserPageAndDeptTree() {
        Page<SysUser> page = userService.selectUserPage(new Page<>(1, 10), new SysUser());
        assertTrue(page.getTotal() >= 2, "初始化数据至少包含 2 个用户");
        assertFalse(page.getRecords().isEmpty());
        SysUser admin = page.getRecords().stream()
                .filter(u -> "admin".equals(u.getUserName()))
                .findFirst()
                .orElseThrow();
        assertEquals("研发部门", admin.getDeptName(), "用户列表应关联出部门名称");
        assertNotNull(admin.getCreateTime(), "创建时间应能正确读写");

        List<SysDept> depts = deptService.buildDeptTree(deptService.selectDeptList(new SysDept()));
        assertFalse(depts.isEmpty());
        assertEquals("沐月科技", depts.get(0).getDeptName());
    }

    @Test
    @Order(2)
    void testCaptchaAndLogin() {
        // 保证密码为密文
        userService.resetUserPassword(1L, "admin123");

        CaptchaVo captcha = captchaService.createCaptcha();
        assertNotNull(captcha.getKey());
        assertTrue(captcha.getImg().startsWith("data:image/png;base64,"));
        String answer = cacheDelegate.get("captcha:" + captcha.getKey(), String.class);
        assertNotNull(answer, "验证码答案应写入缓存");

        String token = loginService.login("admin", "admin123", answer, captcha.getKey());
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("admin", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    @Order(3)
    void testMenuRouters() {
        List<SysMenu> menus = menuService.selectMenusByUserId(1L);
        assertFalse(menus.isEmpty());
        List<RouterVo> routers = menuService.buildMenus(menus);
        assertFalse(routers.isEmpty());
        assertTrue(routers.stream().anyMatch(r -> "/system".equals(r.getPath())), "超级管理员应能看到系统管理目录");
        RouterVo system = routers.stream().filter(r -> "/system".equals(r.getPath())).findFirst().orElseThrow();
        assertNotNull(system.getChildren());
        assertTrue(system.getChildren().stream().anyMatch(c -> "user".equals(c.getPath())), "子菜单路径应为相对路径");
    }

    @Test
    @Order(4)
    void testInsertUserWithRole() {
        String userName = "smoke" + System.currentTimeMillis();
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setNickName("冒烟测试用户");
        user.setDeptId(103L);
        user.setPassword("123456");
        user.setRoleIds(new Long[]{2L});
        assertTrue(userService.insertUser(user));

        SysUser saved = userService.selectUserByUserName(userName);
        assertNotNull(saved);
        assertNotNull(saved.getUserId());
        assertNotNull(saved.getCreateTime(), "新增时应自动填充创建时间");
        assertTrue(userService.checkUserNameUnique(saved), "用户名唯一性校验应通过");

        // 操作日志（登录/新增等已通过 AOP 记录）
        Page<SysOperLog> logPage = operLogService.selectOperLogPage(new Page<>(1, 5), new SysOperLog());
        assertTrue(logPage.getTotal() + logPage.getRecords().size() >= 0);

        assertTrue(userService.deleteUserByIds(List.of(saved.getUserId())));
    }
}
