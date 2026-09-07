package com.muyue.common.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端路由（Vue Router）对象
 *
 * @author muyue
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 路由名字 */
    private String name;

    /** 路由地址 */
    private String path;

    /** 是否隐藏路由（1 隐藏） */
    private boolean hidden;

    /** 重定向地址 */
    private String redirect;

    /** 组件地址 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 当设置 true 的时候该路由不会在侧边栏出现 */
    private boolean alwaysShow;

    /** 其他元素 */
    private MetaVo meta;

    /** 子路由 */
    private List<RouterVo> children;
}
