<template>
  <div class="sidebar-inner">
    <div class="sidebar-logo">
      <span class="logo-badge">
        <el-icon :size="18"><Monitor /></el-icon>
      </span>
      <span v-show="!isCollapse" class="logo-title">{{ title }}</span>
    </div>

    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
      >
        <SidebarItem
          v-for="(route, index) in sidebarRouters"
          :key="route.path + '-' + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import useAppStore from '@/store/modules/app'
import usePermissionStore from '@/store/modules/permission'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

const title = import.meta.env.VITE_APP_TITLE || '沐月管理系统'
const isCollapse = computed(() => !appStore.sidebar.opened)
const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-inner {
  height: 100%;
}
</style>
