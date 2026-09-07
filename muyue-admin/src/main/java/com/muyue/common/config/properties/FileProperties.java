package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.file")
public class FileProperties {

    /** 上传文件根目录（本地磁盘路径） */
    private String profile = "D:/muyue/uploadPath";

    /** 头像存放目录（相对根目录） */
    private String avatarDir = "avatar";

    /** 单个文件大小上限（字节） */
    private long maxFileSize = 5 * 1024 * 1024L;

    /** 允许上传的图片后缀 */
    private String[] allowImageExtensions = {"jpg", "jpeg", "png", "gif", "bmp"};

    /** 静态资源访问前缀 */
    private String urlPrefix = "/profile";
}
