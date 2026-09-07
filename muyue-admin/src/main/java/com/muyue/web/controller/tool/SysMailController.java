package com.muyue.web.controller.tool;

import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.entity.SysMailConfig;
import com.muyue.common.enums.BusinessType;
import com.muyue.tool.service.ISysMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 邮箱工具控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/tool/mail")
@RequiredArgsConstructor
public class SysMailController extends BaseController {

    private final ISysMailService mailService;

    /** 获取邮箱配置 */
    @PreAuthorize("@ps.hasPermi('tool:mail:list')")
    @GetMapping("/config")
    public R<SysMailConfig> getConfig() {
        return R.ok(mailService.getConfig());
    }

    /** 保存邮箱配置 */
    @Log(title = "邮箱工具", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('tool:mail:edit')")
    @PutMapping("/config")
    public R<Void> saveConfig(@RequestBody SysMailConfig config) {
        return toAjax(mailService.saveConfig(config));
    }

    /** 发送测试邮件（支持富文本） */
    @Log(title = "邮箱工具", businessType = BusinessType.OTHER)
    @PreAuthorize("@ps.hasPermi('tool:mail:send')")
    @PostMapping("/send")
    public R<Void> send(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String subject = body.get("subject");
        String content = body.get("content");
        if (to == null || to.isBlank() || subject == null || subject.isBlank()) {
            return R.fail("收件人和主题不能为空");
        }
        mailService.sendMail(to, subject, content == null ? "" : content);
        return R.ok();
    }
}
