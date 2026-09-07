package com.muyue.common.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树构建工具
 *
 * @author muyue
 */
public class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 根据平铺列表构建树
     *
     * @param list           平铺列表
     * @param idGetter       主键
     * @param parentGetter   父级ID
     * @param childrenSetter 子节点设置器
     * @return 顶层节点集合
     */
    public static <T> List<T> build(List<T> list,
                                    Function<T, Long> idGetter,
                                    Function<T, Long> parentGetter,
                                    BiConsumer<T, List<T>> childrenSetter) {
        Map<Long, T> nodeMap = new LinkedHashMap<>();
        for (T node : list) {
            nodeMap.put(idGetter.apply(node), node);
        }
        List<T> roots = new ArrayList<>();
        for (T node : list) {
            Long parentId = parentGetter.apply(node);
            if (parentId == null || parentId == 0L || !nodeMap.containsKey(parentId)) {
                roots.add(node);
                continue;
            }
            T parent = nodeMap.get(parentId);
            List<T> children = getChildren(parent, idGetter, nodeMap, list);
            children.add(node);
        }
        return roots;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getChildren(T parent,
                                           Function<T, Long> idGetter,
                                           Map<Long, T> nodeMap,
                                           List<T> list) {
        try {
            java.lang.reflect.Method method = parent.getClass().getMethod("getChildren");
            Object children = method.invoke(parent);
            if (children == null) {
                children = new ArrayList<T>();
                java.lang.reflect.Method setter = parent.getClass().getMethod("setChildren", List.class);
                setter.invoke(parent, children);
            }
            return (List<T>) children;
        } catch (Exception e) {
            throw new RuntimeException("构建树失败，实体需要提供 getChildren/setChildren 方法", e);
        }
    }

    /**
     * 获取所有后代节点ID集合（包含自身）
     */
    public static <T> List<Long> collectIds(List<T> nodes, Function<T, Long> idGetter) {
        List<Long> ids = new ArrayList<>();
        collect(nodes, idGetter, ids);
        return ids;
    }

    private static <T> void collect(List<T> nodes, Function<T, Long> idGetter, List<Long> ids) {
        if (nodes == null) {
            return;
        }
        for (T node : nodes) {
            ids.add(idGetter.apply(node));
            try {
                java.lang.reflect.Method method = node.getClass().getMethod("getChildren");
                Object children = method.invoke(node);
                if (children instanceof List<?> list) {
                    collect((List<T>) list, idGetter, ids);
                }
            } catch (Exception ignored) {
                // 无子节点
            }
        }
    }
}
