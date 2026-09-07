package com.muyue.web.controller.tool;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysFile;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.tool.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附件工具控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/tool/file")
@RequiredArgsConstructor
public class SysFileController extends BaseController {

    private final ISysFileService fileService;

    /** 附件列表（分页） */
    @PreAuthorize("@ps.hasPermi('tool:file:list')")
    @GetMapping("/list")
    public TableDataInfo<SysFile> list(SysFile query) {
        Page<SysFile> page = fileService.selectPage(getPage(), query);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /** 上传附件 */
    @Log(title = "附件工具", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('tool:file:upload')")
    @PostMapping("/upload")
    public R<SysFile> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(file, SecurityUtils.getUsername()));
    }

    /** 在线预览（内联） */
    @PreAuthorize("@ps.hasPermi('tool:file:list')")
    @GetMapping("/preview/{fileId}")
    public org.springframework.http.ResponseEntity<byte[]> preview(@PathVariable Long fileId) throws IOException {
        return inline(fileId, false);
    }

    /** 下载附件 */
    @Log(title = "附件工具", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ps.hasPermi('tool:file:list')")
    @GetMapping("/download/{fileId}")
    public org.springframework.http.ResponseEntity<byte[]> download(@PathVariable Long fileId) throws IOException {
        return inline(fileId, true);
    }

    private org.springframework.http.ResponseEntity<byte[]> inline(Long fileId, boolean attachment) throws IOException {
        SysFile file = fileService.getById(fileId);
        if (file == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        Path path = java.nio.file.Paths.get(fileService.resolvePath(file.getFilePath()));
        if (!Files.exists(path)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        String contentType = switch (file.getFileType() == null ? "" : file.getFileType()) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt", "md", "csv" -> "text/plain";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
        String encodedName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = (attachment ? "attachment" : "inline")
                + "; filename*=UTF-8''" + encodedName;
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", disposition)
                .body(Files.readAllBytes(path));
    }

    /** 删除附件 */
    @Log(title = "附件工具", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('tool:file:remove')")
    @DeleteMapping("/{fileIds}")
    public R<Void> remove(@PathVariable String fileIds) {
        List<Long> ids = Arrays.stream(fileIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return toAjax(fileService.deleteByIds(ids));
    }
}
