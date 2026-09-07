package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱发送配置表 sys_mail_config（单行，config_id = 1）
 *
 * @author muyue
 */
@Data
@TableName("sys_mail_config")
public class SysMailConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @TableId
    private Long configId;

    /** SMTP 服务器地址 */
    private String host;

    /** SMTP 端口 */
    private Integer port;

    /** 发件账号 */
    private String username;

    /** 授权码/密码 */
    private String password;

    /** 发件人邮箱 */
    private String fromAddr;

    /** 发件人昵称 */
    private String nickname;

    /** 是否启用 SSL（1是 0否） */
    private String sslEnabled;
}
