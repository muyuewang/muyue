<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="route.name" />
        </keep-alive>
      </transition>
    </router-view>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import useTagsViewStore from '@/store/modules/tagsView'

const tagsViewStore = useTagsViewStore()
// keep-alive 只缓存「组件名 = 路由名」且未设置 noCache 的页面（各页面已通过 defineOptions 声明 name）
const cachedViews = computed(() => tagsViewStore.cachedViews)
</script>
