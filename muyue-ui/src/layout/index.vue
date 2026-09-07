<template>
  <div class="app-wrapper">
    <Sidebar class="sidebar-container" :class="{ 'is-collapse': !sidebar.opened }" />
    <div class="main-container" :class="{ 'is-collapse': !sidebar.opened }">
      <div class="fixed-header">
        <Navbar />
        <TagsView />
      </div>
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import useAppStore from '@/store/modules/app'
import Sidebar from './components/Sidebar/index.vue'
import Navbar from './components/Navbar.vue'
import TagsView from './components/TagsView/index.vue'
import AppMain from './components/AppMain.vue'

const appStore = useAppStore()
const sidebar = computed(() => appStore.sidebar)
</script>
