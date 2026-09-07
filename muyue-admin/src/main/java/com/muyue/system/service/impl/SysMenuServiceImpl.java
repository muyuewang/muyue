package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysMenu;
import com.muyue.common.core.domain.vo.MetaVo;
import com.muyue.common.core.domain.vo.RouterVo;
import com.muyue.common.core.domain.vo.TreeSelect;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.common.utils.StringUtils;
import com.muyue.common.utils.TreeUtils;
import com.muyue.system.mapper.SysMenuMapper;
import com.muyue.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 菜单服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu) {
        return baseMapper.selectMenuList(menu);
    }

    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        if (SecurityUtils.isAdmin(userId)) {
            // 菜单类型默认为 M，查询全量菜单时需要清空，避免只查出目录
            SysMenu query = new SysMenu();
            query.setMenuType(null);
            return baseMapper.selectMenuList(query);
        }
        return baseMapper.selectMenuTreeByUserId(userId);
    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<SysMenu> tree = buildMenuTree(menus);
        List<RouterVo> routers = new ArrayList<>();
        for (SysMenu menu : tree) {
            RouterVo router = buildRouterVo(menu);
            if (router != null) {
                routers.add(router);
            }
        }
        return routers;
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        return buildMenuTree(menus).stream().map(this::toTreeSelect).toList();
    }

    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        return baseMapper.selectMenuPermsByUserId(userId);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        return baseMapper.selectById(menuId);
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId)) > 0;
    }

    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return baseMapper.countRoleMenuByMenuId(menuId) > 0;
    }

    @Override
    public boolean insertMenu(SysMenu menu) {
        return baseMapper.insert(menu) > 0;
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        return baseMapper.updateById(menu) > 0;
    }

    @Override
    public boolean deleteMenuById(Long menuId) {
        if (hasChildByMenuId(menuId)) {
            throw new ServiceException("存在子菜单，不允许删除");
        }
        if (checkMenuExistRole(menuId)) {
            throw new ServiceException("菜单已分配，不允许删除");
        }
        baseMapper.deleteRoleMenuByMenuId(menuId);
        return baseMapper.deleteById(menuId) > 0;
    }

    /**
     * 构建菜单树（仅保留 M、C 类型）
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> list = menus.stream()
                .filter(m -> "M".equals(m.getMenuType()) || "C".equals(m.getMenuType()))
                .peek(m -> m.setChildren(new ArrayList<>()))
                .toList();
        return TreeUtils.build(list, SysMenu::getMenuId, SysMenu::getParentId, SysMenu::setChildren);
    }

    private TreeSelect toTreeSelect(SysMenu menu) {
        if (menu.getChildren() == null || menu.getChildren().isEmpty()) {
            return new TreeSelect(menu.getMenuId(), menu.getMenuName());
        }
        return new TreeSelect(menu.getMenuId(), menu.getMenuName(),
                menu.getChildren().stream().map(this::toTreeSelect).toList());
    }

    /**
     * 菜单转前端路由
     */
    private RouterVo buildRouterVo(SysMenu menu) {
        RouterVo router = new RouterVo();
        router.setName(getRouteName(menu));
        router.setPath(getRouterPath(menu));
        router.setComponent(getComponent(menu));
        router.setQuery(menu.getQuery());
        router.setHidden("1".equals(menu.getVisible()));
        router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), "1".equals(menu.getIsCache())));

        List<SysMenu> children = menu.getChildren();
        if (children != null && !children.isEmpty() && "M".equals(menu.getMenuType())) {
            router.setAlwaysShow(true);
            router.setRedirect("noRedirect");
            List<RouterVo> childRoutes = children.stream()
                    .map(this::buildRouterVo)
                    .filter(Objects::nonNull)
                    .toList();
            router.setChildren(childRoutes);
        } else if (isMenuFrame(menu)) {
            // 一级 C 菜单：父路由包装 Layout（不命名，避免与子路由重名），子路由为菜单本身
            router.setName(null);
            router.setMeta(null);
            RouterVo child = new RouterVo();
            child.setPath(menu.getPath());
            child.setComponent(menu.getComponent());
            child.setName(StringUtils.isNotBlank(menu.getPath()) ? upperFirst(menu.getPath()) : "");
            child.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), "1".equals(menu.getIsCache())));
            child.setQuery(menu.getQuery());
            router.setChildren(List.of(child));
        }
        return router;
    }

    /**
     * 获取路由名称（首字母大写）
     */
    private String getRouteName(SysMenu menu) {
        String path = menu.getPath();
        if (StringUtils.isBlank(path)) {
            return String.valueOf(menu.getMenuId());
        }
        return upperFirst(path);
    }

    /**
     * 获取路由地址
     */
    private String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        // 一级目录
        if (isRoot(menu) && "1".equals(menu.getIsFrame()) && "M".equals(menu.getMenuType())) {
            routerPath = "/" + menu.getPath();
        } else if (isMenuFrame(menu)) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     */
    private String getComponent(SysMenu menu) {
        String component = "Layout";
        if (StringUtils.isNotBlank(menu.getComponent()) && !isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (StringUtils.isBlank(menu.getComponent()) && isMenuFrame(menu)) {
            component = "Layout";
        } else if (StringUtils.isBlank(menu.getComponent()) && !isRoot(menu) && "M".equals(menu.getMenuType())) {
            component = "ParentView";
        } else if (isInnerLink(menu)) {
            component = "InnerLink";
        }
        return component;
    }

    private boolean isRoot(SysMenu menu) {
        return menu.getParentId() == null || menu.getParentId() == 0L;
    }

    /**
     * 是否为一级菜单（parentId=0 且为 C 类型且非外链，is_frame=1 表示非外链）
     * 一级 C 菜单需包装为 Layout，子路由为菜单本身
     */
    private boolean isMenuFrame(SysMenu menu) {
        return isRoot(menu) && "C".equals(menu.getMenuType()) && "1".equals(menu.getIsFrame());
    }

    private boolean isInnerLink(SysMenu menu) {
        return "0".equals(menu.getIsFrame()) && StringUtils.isNotBlank(menu.getPath())
                && (menu.getPath().startsWith("http://") || menu.getPath().startsWith("https://"));
    }

    private String upperFirst(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
