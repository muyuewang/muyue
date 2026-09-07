package com.muyue.common.core.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 路由 meta 信息
 *
 * @author muyue
 */
@Data
public class MetaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设置该路由在侧边栏和面包屑中展示的名字 */
    private String title;

    /** 设置该路由的图标 */
    private String icon;

    /** 设置为 true，则不会被 <keep-alive> 缓存 */
    private boolean noCache;

    /** 内嵌地址 */
    private String link;

    public MetaVo() {
    }

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }
}
