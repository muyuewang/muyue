package com.muyue.tool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件工具服务接口
 *
 * @author muyue
 */
public interface ISysFileService extends IService<SysFile> {

    /** 分页查询附件 */
    Page<SysFile> selectPage(Page<SysFile> page, SysFile query);

    /** 上传附件 */
    SysFile upload(MultipartFile file, String username);

    /** 解析物理路径 */
    String resolvePath(String relativePath);

    /** 批量删除（含物理文件） */
    boolean deleteByIds(List<Long> ids);
}
