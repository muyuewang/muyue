package com.muyue.tool.service;

import com.muyue.common.core.domain.entity.SysMailConfig;

/**
 * 邮箱工具服务接口
 *
 * @author muyue
 */
public interface ISysMailService {

    /** 获取邮箱配置 */
    SysMailConfig getConfig();

    /** 保存邮箱配置 */
    boolean saveConfig(SysMailConfig config);

    /** 发送富文本邮件 */
    void sendMail(String to, String subject, String content);
}
