package com.muyue.tool.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 代码生成 - 列元数据
 *
 * @author muyue
 */
@Data
public class GenColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 列名（下划线） */
    private String columnName;

    /** 数据库类型 */
    private String dataType;

    /** 列备注 */
    private String columnComment;

    /** Java 属性名（驼峰） */
    private String javaField;

    /** Java 类型 */
    private String javaType;

    /** 是否主键（1是 0否） */
    private String isPk;
}
