package com.muyue.common.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 树结构下拉选择对象
 *
 * @author muyue
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TreeSelect implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 是否禁用 */
    private Boolean disabled;

    /** 子节点 */
    private List<TreeSelect> children;

    public TreeSelect() {
    }

    public TreeSelect(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public TreeSelect(Long id, String label, List<TreeSelect> children) {
        this.id = id;
        this.label = label;
        this.children = children;
    }
}
