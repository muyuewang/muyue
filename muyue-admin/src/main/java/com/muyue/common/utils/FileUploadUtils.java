package com.muyue.common.utils;

import com.muyue.common.config.properties.FileProperties;
import com.muyue.common.exception.ServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件上传工具（本地磁盘存储，生产可替换为 OSS/MinIO）
 *
 * @author muyue
 */
public class FileUploadUtils {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private FileUploadUtils() {
    }

    /**
     * 上传图片
     *
     * @param fileProperties 文件配置
     * @param file           上传的文件
     * @param dir            相对根目录的子目录
     * @return 相对路径（如 avatar/2026/09/06/xxx.png）
     */
    public static String uploadImage(FileProperties fileProperties, MultipartFile file, String dir) {
        assertAllowed(fileProperties, file);
        String extension = getExtension(file);
        String datePath = DATE_PATH.format(LocalDate.now());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path targetDir = Paths.get(fileProperties.getProfile(), dir, datePath);
        Path target = targetDir.resolve(fileName);
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new ServiceException("上传文件失败：" + e.getMessage());
        }
        return dir + "/" + datePath + "/" + fileName;
    }

    /**
     * 相对路径转访问地址
     */
    public static String toUrl(FileProperties fileProperties, String relativePath) {
        return fileProperties.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
    }

    /**
     * 校验文件
     */
    public static void assertAllowed(FileProperties fileProperties, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > fileProperties.getMaxFileSize()) {
            throw new ServiceException("上传文件大小超出限制，最大 " + fileProperties.getMaxFileSize() / 1024 / 1024 + "MB");
        }
        String extension = getExtension(file);
        List<String> allowed = Arrays.stream(fileProperties.getAllowImageExtensions())
                .map(e -> e.toLowerCase(Locale.ROOT))
                .toList();
        if (!allowed.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new ServiceException("文件格式不正确，仅支持：" + String.join("、", allowed));
        }
    }

    /**
     * 获取文件后缀
     */
    public static String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf('.') < 0) {
            throw new ServiceException("无法获取文件后缀");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
}
