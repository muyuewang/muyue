<template>
  <div class="tags-view-wrapper">
    <el-scrollbar ref="scrollContainer" class="tags-view-container">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        :class="['tags-view-item', isActive(tag) ? 'active' : '']"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        {{ tag.title }}
        <span v-if="!isAffix(tag)" class="el-icon-close" @click.prevent.stop="closeSelectedTag(tag)">
          <el-icon><Close /></el-icon>
        </span>
      </router-link>
    </el-scrollbar>

    <teleport to="body">
      <ul v-show="visible" class="contextmenu" :style="{ left: left + 'px', top: top + 'px' }">
        <li @click="refreshSelectedTag(selectedTag)">刷新</li>
        <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">关闭</li>
        <li @click="closeOthersTags">关闭其他</li>
        <li @click="closeAllTags">关闭所有</li>
      </ul>
    </teleport>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useTagsViewStore from '@/store/modules/tagsView'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const permissionStore = usePermissionStore()

const visitedViews = computed(() => tagsViewStore.visitedViews)
const scrollContainer = ref(null)
const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})

watch(() => route.path, () => {
  addTags()
  scrollIntoView()
})

watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

onMounted(() => {
  initTags()
  addTags()
})

function isActive(r) {
  return r.path === route.path
}

function isAffix(r) {
  return r.meta && r.meta.affix
}

function filterAffixTags(routes, basePath = '') {
  let tags = []
  routes.forEach((route) => {
    if (route.meta && route.meta.affix) {
      const path = route.path.startsWith('/')
        ? route.path
        : basePath + '/' + route.path
      tags.push({
        ...route,
        path,
        fullPath: path,
        title: route.meta.title || route.name || 'no-name'
      })
    }
    if (route.children) {
      const childBasePath = route.path.startsWith('/') ? route.path : basePath + '/' + route.path
      const tempTags = filterAffixTags(route.children, childBasePath)
      if (tempTags.length) tags = [...tags, ...tempTags]
    }
  })
  return tags
}

function initTags() {
  // 使用完整路由树（含静态路由）计算 affix 标签，避免扁平化路由导致路径异常
  const affixTags = filterAffixTags(permissionStore.routes)
  affixTags.forEach((tag) => {
    tagsViewStore.addVisitedView(tag)
  })
}

function addTags() {
  const { meta, path } = route
  if (path === '/redirect') return
  if (meta && meta.title) {
    tagsViewStore.addView({
      name: route.name,
      path: route.path,
      fullPath: route.fullPath,
      meta: { ...route.meta },
      query: { ...route.query },
      params: { ...route.params },
      title: meta.title || 'no-name'
    })
  }
}

function refreshSelectedTag(view) {
  closeMenu()
  router.push('/redirect' + view.fullPath)
}

function closeSelectedTag(view) {
  closeMenu()
  tagsViewStore.delView(view).then(({ visitedViews }) => {
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function toLastView(visitedViews, view) {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath || latestView.path)
  } else {
    if (view.path === '/index') return
    router.push('/index')
  }
}

function closeOthersTags() {
  closeMenu()
  router.push(selectedTag.value.fullPath || selectedTag.value.path)
  tagsViewStore.delOthersViews(selectedTag.value)
}

function closeAllTags() {
  closeMenu()
  tagsViewStore.delAllViews().then(({ visitedViews }) => {
    if (isAffix(selectedTag.value)) return
    router.push('/index')
  })
}

function openMenu(tag, e) {
  const menuMinWidth = 105
  const menuHeight = 150
  const maxLeft = document.body.clientWidth - menuMinWidth
  const menuLeft = Math.max(Math.min(e.clientX, maxLeft), 0)
  let menuTop = e.clientY + 15
  if (menuTop + menuHeight > window.innerHeight) {
    menuTop = window.innerHeight - menuHeight
  }

  selectedTag.value = tag
  left.value = menuLeft
  top.value = menuTop
  visible.value = true
}

function closeMenu() {
  visible.value = false
  selectedTag.value = {}
}

/** 当前标签滚入可视区 */
function scrollIntoView() {
  nextTick(() => {
    document.querySelector('.tags-view-item.active')
      ?.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' })
  })
}
</script>

<style scoped>
.tags-view-wrapper {
  display: flex;
  align-items: center;
  height: var(--tags-view-height);
  padding: 0 16px;
  background: var(--bg-navbar);
  border-bottom: 1px solid var(--border-light);
  backdrop-filter: blur(10px);
}

.tags-view-container {
  flex: 1;
  height: 100%;
}

.tags-view-container :deep(.el-scrollbar__wrap) {
  display: flex;
  align-items: center;
}

.tags-view-container :deep(.el-scrollbar__view) {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.tags-view-item {
  display: inline-flex;
  align-items: center;
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
  height: 28px;
  line-height: 28px;
  padding: 0 12px;
  margin-right: 8px;
  font-size: 12px;
  color: var(--text-regular);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.tags-view-item:last-of-type {
  margin-right: 0;
}

.tags-view-item::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 6px;
  border-radius: 50%;
  background: var(--text-secondary);
  transition: background 0.2s;
}

.tags-view-item:hover {
  color: var(--muyue-primary);
  border-color: var(--muyue-primary-light-7);
}

.tags-view-item.active {
  color: var(--muyue-primary);
  background: var(--muyue-primary-soft);
  border-color: var(--muyue-primary);
  font-weight: 600;
}

.tags-view-item.active::before {
  background: var(--muyue-primary);
}

.tags-view-item .el-icon-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  margin-left: 6px;
  border-radius: 50%;
  transition: background 0.2s, color 0.2s;
}

.tags-view-item .el-icon-close:hover {
  background: var(--muyue-primary-light-7);
  color: #fff;
}

.contextmenu {
  position: fixed;
  z-index: 3000;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow);
  font-size: 13px;
}

.contextmenu li {
  padding: 8px 16px;
  cursor: pointer;
  color: var(--text-regular);
  white-space: nowrap;
  transition: background 0.2s, color 0.2s;
}

.contextmenu li:hover {
  background: var(--bg-hover);
  color: var(--muyue-primary);
}
</style>
