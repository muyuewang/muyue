package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统访问记录表 sys_logininfor
 *
 * @author muyue
 */
@Data
@TableName("sys_logininfor")
public class SysLogininfor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long infoId;

    /** 用户账号 */
    private String userName;

    /** 登录地址 */
    private String ipaddr;

    /** 登录状态（0成功 1失败） */
    private String status;

    /** 提示消息 */
    private String msg;

    /** 访问时间 */
    private LocalDateTime accessTime;

    /** 查询：开始时间 */
    @TableField(exist = false)
    private String beginTime;

    /** 查询：结束时间 */
    @TableField(exist = false)
    private String endTime;
}
