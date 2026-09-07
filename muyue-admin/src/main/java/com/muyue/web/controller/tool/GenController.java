package com.muyue.web.controller.tool;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.exception.ServiceException;
import com.muyue.tool.domain.GenTable;
import com.muyue.tool.service.CodeGenerator;
import com.muyue.tool.service.GenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成控制器：库表列表、代码预览、zip 下载
 *
 * @author muyue
 */
@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
public class GenController extends BaseController {

    private final GenService genService;
    private final CodeGenerator codeGenerator;

    /** 库表列表（分页 + 模糊搜索） */
    @PreAuthorize("@ps.hasPermi('tool:gen:list')")
    @GetMapping("/db/list")
    public TableDataInfo<GenTable> dbList(@RequestParam(required = false) String tableName) {
        Page<GenTable> page = getPage();
        List<GenTable> tables = genService.listTables(tableName);
        int total = tables.size();
        int from = (int) Math.min((page.getCurrent() - 1) * page.getSize(), total);
        int to = (int) Math.min(from + page.getSize(), total);
        return new TableDataInfo<>(tables.subList(from, to), total);
    }

    /** 预览生成的代码（key = 文件名，value = 内容） */
    @PreAuthorize("@ps.hasPermi('tool:gen:query')")
    @GetMapping("/preview/{tableName}")
    public R<Map<String, String>> preview(@PathVariable String tableName) {
        GenTable table = genService.getTable(tableName);
        return R.ok(codeGenerator.generate(table));
    }

    /** 下载生成的代码（zip） */
    @PreAuthorize("@ps.hasPermi('tool:gen:code')")
    @GetMapping("/download/{tableName}")
    public void download(@PathVariable String tableName, HttpServletResponse response) throws IOException {
        GenTable table = genService.getTable(tableName);
        Map<String, String> files = codeGenerator.generate(table);
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        String fileName = table.getTableName() + "-code.zip";
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(table.getClassName() + "/" + entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new ServiceException("生成代码包失败：" + e.getMessage());
        }
    }
}
