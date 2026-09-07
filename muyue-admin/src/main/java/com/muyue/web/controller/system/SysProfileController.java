package com.muyue.web.controller.system;

import com.muyue.common.annotation.Log;
import com.muyue.common.config.properties.FileProperties;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.utils.FileUploadUtils;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 个人信息
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
public class SysProfileController {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FileProperties fileProperties;

    /**
     * 当前登录人信息
     */
    @GetMapping
    public R<Map<String, Object>> profile() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roleGroup", String.join(",", loginUser.getRoles()));
        return R.ok(data);
    }

    /**
     * 修改个人信息
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser update = new SysUser();
        update.setUserId(loginUser.getUserId());
        update.setNickName(user.getNickName());
        update.setPhonenumber(user.getPhonenumber());
        update.setEmail(user.getEmail());
        update.setSex(user.getSex());
        boolean rows = userService.updateById(update);
        if (rows) {
            loginUser.getUser().setNickName(user.getNickName());
            loginUser.getUser().setPhonenumber(user.getPhonenumber());
            loginUser.getUser().setEmail(user.getEmail());
            loginUser.getUser().setSex(user.getSex());
        }
        return rows ? R.ok() : R.fail();
    }

    /**
     * 修改密码
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public R<Void> updatePwd(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = userService.selectUserByUserName(loginUser.getUsername());
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return R.fail("修改密码失败，旧密码错误");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return R.fail("新密码不能与旧密码相同");
        }
        return userService.resetUserPassword(user.getUserId(), newPassword) ? R.ok() : R.fail();
    }

    /**
     * 头像上传
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public R<Map<String, Object>> avatar(@RequestParam("avatarfile") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail("上传图片不能为空");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String relativePath = FileUploadUtils.uploadImage(fileProperties, file, fileProperties.getAvatarDir());
        String imgUrl = FileUploadUtils.toUrl(fileProperties, relativePath);

        SysUser update = new SysUser();
        update.setUserId(loginUser.getUserId());
        update.setAvatar(imgUrl);
        userService.updateById(update);
        loginUser.getUser().setAvatar(imgUrl);

        Map<String, Object> data = new HashMap<>();
        data.put("imgUrl", imgUrl);
        data.put("fileName", relativePath);
        return R.ok(data);
    }
}
