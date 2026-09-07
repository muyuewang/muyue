package com.muyue.web.controller.system;

import com.muyue.common.annotation.Log;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.core.domain.model.LoginBody;
import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.core.domain.vo.RouterVo;
import com.muyue.common.core.domain.vo.UserInfoVo;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.utils.JwtUtils;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.service.ISysMenuService;
import com.muyue.system.service.OnlineUserService;
import com.muyue.web.service.SysLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录认证
 *
 * @author muyue
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SysLoginService loginService;
    private final ISysMenuService menuService;
    private final JwtUtils jwtUtils;
    private final OnlineUserService onlineUserService;

    /**
     * 登录
     */
    @Log(title = "登录", businessType = BusinessType.OTHER, isSaveResponseData = false)
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody loginBody) {
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(),
                loginBody.getCode(), loginBody.getUuid());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return R.ok("登录成功", data);
    }

    /**
     * 获取当前登录用户信息（角色、权限）
     */
    @GetMapping("/getInfo")
    public R<UserInfoVo> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return R.fail(401, "未登录或登录状态已过期");
        }
        SysUser user = loginUser.getUser();
        user.setPassword(null);
        UserInfoVo vo = new UserInfoVo();
        vo.setUser(user);
        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        return R.ok(vo);
    }

    /**
     * 根据当前登录人角色获取动态路由（菜单）
     */
    @GetMapping("/getRouters")
    public R<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<RouterVo> routers = menuService.buildMenus(menuService.selectMenusByUserId(userId));
        return R.ok(routers);
    }

    /**
     * 退出登录（服务端主动作废令牌，多实例部署全节点生效）
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = jwtUtils.getToken(request);
        if (token != null) {
            onlineUserService.remove(token);
        }
        return R.ok();
    }
}
