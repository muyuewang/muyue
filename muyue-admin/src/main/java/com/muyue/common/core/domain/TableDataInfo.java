package com.muyue.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页表格响应对象
 *
 * @author muyue
 */
@Data
public class TableDataInfo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<T> rows;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String msg;

    public TableDataInfo() {
    }

    public TableDataInfo(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
        this.code = R.SUCCESS;
        this.msg = "查询成功";
    }

    public static <T> TableDataInfo<T> of(List<T> rows, long total) {
        return new TableDataInfo<>(rows, total);
    }
}
