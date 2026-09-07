package com.muyue.tool.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成 - 表元数据
 *
 * @author muyue
 */
@Data
public class GenTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表名 */
    private String tableName;

    /** 表备注 */
    private String tableComment;

    /** 实体类名（如 Order） */
    private String className;

    /** 模块名（表名前缀，如 biz） */
    private String moduleName;

    /** 业务名（如 order） */
    private String businessName;

    /** 功能名（用于注释/菜单） */
    private String functionName;

    /** 主键列 */
    private GenColumn pkColumn;

    /** 全部列 */
    private List<GenColumn> columns = new ArrayList<>();
}
