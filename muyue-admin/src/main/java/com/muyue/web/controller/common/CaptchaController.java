package com.muyue.web.controller.common;

import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.vo.CaptchaVo;
import com.muyue.web.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码接口
 *
 * @author muyue
 */
@RestController
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public R<CaptchaVo> captchaImage() {
        return R.ok(captchaService.createCaptcha());
    }
}
