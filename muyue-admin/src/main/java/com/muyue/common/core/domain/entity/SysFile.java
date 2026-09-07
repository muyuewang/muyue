package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件表 sys_file
 *
 * @author muyue
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long fileId;

    /** 原始文件名 */
    private String fileName;

    /** 存储相对路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件后缀 */
    private String fileType;

    /** 上传者 */
    private String createBy;

    /** 上传时间 */
    private LocalDateTime createTime;
}
