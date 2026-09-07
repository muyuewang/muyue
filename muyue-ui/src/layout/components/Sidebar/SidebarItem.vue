<template>
  <div v-if="!item.hidden">
    <!-- 只有一个可显示子路由：直接渲染为菜单项 -->
    <template v-if="showingChild">
      <el-menu-item :index="resolvePath(showingChild.path, showingChild.query)">
        <el-icon v-if="showingChild.meta && showingChild.meta.icon">
          <component :is="showingChild.meta.icon" />
        </el-icon>
        <template #title>
          <span>{{ (showingChild.meta && showingChild.meta.title) || showingChild.name }}</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 多级菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ (item.meta && item.meta.title) || item.name }}</span>
      </template>
      <SidebarItem
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

const onlyOneChild = ref({})

/** 是否只有一个可显示的子路由 */
const showingChild = computed(() => {
  const children = (props.item.children || []).filter((child) => !child.hidden)
  if (children.length === 1 && !props.item.alwaysShow) {
    onlyOneChild.value = children[0]
    return onlyOneChild.value
  }
  if (children.length === 0) {
    onlyOneChild.value = { ...props.item, path: props.item.path, noShowingChildren: true }
    return onlyOneChild.value
  }
  return null
})

/** 拼接完整路由地址 */
function resolvePath(routePath, query) {
  if (!routePath) {
    return props.basePath
  }
  if (routePath.indexOf('http') === 0) {
    return routePath
  }
  if (routePath.startsWith('/')) {
    let full = routePath
    if (query) {
      full = full + '?' + query
    }
    return full
  }
  const base = props.basePath.endsWith('/') ? props.basePath.slice(0, -1) : props.basePath
  return base + '/' + routePath
}
</script>
