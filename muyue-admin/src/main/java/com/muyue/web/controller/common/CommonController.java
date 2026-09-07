package com.muyue.web.controller.common;

import com.muyue.common.config.properties.FileProperties;
import com.muyue.common.core.domain.R;
import com.muyue.common.utils.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用接口（文件上传）
 *
 * @author muyue
 */
@RestController
@RequiredArgsConstructor
public class CommonController {

    private final FileProperties fileProperties;

    /**
     * 通用上传请求
     */
    @PostMapping("/common/upload")
    public R<Map<String, Object>> uploadFile(MultipartFile file) {
        String relativePath = FileUploadUtils.uploadImage(fileProperties, file, "upload");
        Map<String, Object> data = new HashMap<>();
        data.put("url", FileUploadUtils.toUrl(fileProperties, relativePath));
        data.put("fileName", relativePath);
        data.put("originalFilename", file.getOriginalFilename());
        return R.ok(data);
    }
}
