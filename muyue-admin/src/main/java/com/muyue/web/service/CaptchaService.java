package com.muyue.web.service;

import com.muyue.common.cache.CacheDelegate;
import com.muyue.common.config.properties.CaptchaProperties;
import com.muyue.common.core.domain.vo.CaptchaVo;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.CaptchaUtils;
import com.muyue.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 验证码服务（存储走 CacheDelegate：memory 单机 / redis 多机共享）
 *
 * @author muyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String KEY_PREFIX = "captcha:";
    private static final Duration CAPTCHA_TTL = Duration.ofSeconds(180);

    private final CaptchaProperties captchaProperties;
    private final CacheDelegate cacheDelegate;

    /**
     * 生成验证码
     */
    public CaptchaVo createCaptcha() {
        boolean enabled = Boolean.TRUE.equals(captchaProperties.getEnabled());
        CaptchaVo vo = new CaptchaVo();
        vo.setCaptchaEnabled(enabled);
        if (!enabled) {
            return vo;
        }
        String[] captcha = "char".equalsIgnoreCase(captchaProperties.getType())
                ? CaptchaUtils.generateChar(4)
                : CaptchaUtils.generateMath();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        cacheDelegate.put(KEY_PREFIX + uuid, captcha[1], CAPTCHA_TTL);
        vo.setKey(uuid);
        vo.setImg(CaptchaUtils.toBase64(captcha[0]));
        return vo;
    }

    /**
     * 校验验证码
     */
    public void validateCaptcha(String uuid, String code) {
        if (!Boolean.TRUE.equals(captchaProperties.getEnabled())) {
            return;
        }
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            throw new ServiceException("验证码不能为空");
        }
        String answer = cacheDelegate.get(KEY_PREFIX + uuid, String.class);
        cacheDelegate.delete(KEY_PREFIX + uuid);
        if (answer == null) {
            throw new ServiceException("验证码已失效");
        }
        if (!answer.equalsIgnoreCase(code.trim())) {
            throw new ServiceException("验证码错误");
        }
    }
}
