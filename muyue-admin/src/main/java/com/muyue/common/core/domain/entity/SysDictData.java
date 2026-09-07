package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.muyue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表 sys_dict_data
 *
 * @author muyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 字典编码 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long dictCode;

    /** 字典排序 */
    private Integer dictSort = 0;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 字典类型 */
    private String dictType;

    /** 样式属性（回显样式） */
    private String cssClass;

    /** 表格回显样式（primary / success / info / warning / danger） */
    private String listClass;

    /** 是否默认（Y是 N否） */
    private String isDefault = "N";

    /** 状态（0正常 1停用） */
    private String status = "0";
}
