package com.muyue.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.config.properties.FileProperties;
import com.muyue.common.core.domain.entity.SysFile;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.StringUtils;
import com.muyue.tool.mapper.SysFileMapper;
import com.muyue.tool.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 附件工具服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final List<String> ALLOWED_EXT = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "csv", "txt", "md", "ppt", "pptx", "zip", "rar", "7z");

    private final SysFileMapper fileMapper;
    private final FileProperties fileProperties;

    @Override
    public Page<SysFile> selectPage(Page<SysFile> page, SysFile query) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .like(StringUtils.isNotBlank(query.getFileName()), SysFile::getFileName, query.getFileName())
                .orderByDesc(SysFile::getFileId);
        page.setRecords(fileMapper.selectList(wrapper));
        page.setTotal(fileMapper.selectCount(wrapper));
        return page;
    }

    @Override
    public SysFile upload(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > fileProperties.getMaxFileSize()) {
            throw new ServiceException("文件大小超出限制，最大 " + fileProperties.getMaxFileSize() / 1024 / 1024 + "MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.lastIndexOf('.') < 0) {
            throw new ServiceException("无法识别文件后缀");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ServiceException("仅支持上传：" + String.join("、", ALLOWED_EXT));
        }

        String datePath = DATE_PATH.format(LocalDate.now());
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(fileProperties.getProfile(), "file", datePath);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storedName).toFile());
        } catch (IOException e) {
            throw new ServiceException("保存文件失败：" + e.getMessage());
        }

        SysFile entity = new SysFile();
        entity.setFileName(originalName);
        entity.setFilePath("file/" + datePath + "/" + storedName);
        entity.setFileSize(file.getSize());
        entity.setFileType(ext);
        entity.setCreateBy(username);
        fileMapper.insert(entity);
        return entity;
    }

    @Override
    public String resolvePath(String relativePath) {
        return Paths.get(fileProperties.getProfile(), relativePath.split("/")).toString();
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            SysFile file = fileMapper.selectById(id);
            if (file == null) {
                continue;
            }
            try {
                Files.deleteIfExists(Paths.get(resolvePath(file.getFilePath())));
            } catch (IOException ignored) {
                // 物理文件删除失败不阻塞记录删除
            }
            fileMapper.deleteById(id);
        }
        return true;
    }
}
