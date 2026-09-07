<template>
  <div class="navbar">
    <div class="navbar-left">
      <span class="icon-button" :title="appStore.sidebar.opened ? '收起菜单' : '展开菜单'" @click="toggleSideBar">
        <el-icon :size="18">
          <component :is="appStore.sidebar.opened ? 'Fold' : 'Expand'" />
        </el-icon>
      </span>

      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <el-dropdown trigger="click" @command="handleNoticeClick">
        <span class="icon-button" title="消息通知">
          <el-badge :value="noticeCount" :hidden="!noticeCount" :max="99">
            <el-icon :size="17"><Bell /></el-icon>
          </el-badge>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="!recentNotices.length" disabled>暂无消息通知</el-dropdown-item>
            <el-dropdown-item v-for="n in recentNotices" :key="n.noticeId" :command="n.noticeId">
              <el-tag size="small" :type="n.noticeType === '1' ? 'warning' : 'success'" style="margin-right: 6px">
                {{ n.noticeType === '1' ? '通知' : '公告' }}
              </el-tag>
              {{ n.noticeTitle }}
            </el-dropdown-item>
            <el-dropdown-item v-if="recentNotices.length" command="all" divided>
              <el-icon><More /></el-icon> 查看全部
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <span class="icon-button" title="数据大屏" @click="openScreen">
        <el-icon :size="17"><DataBoard /></el-icon>
      </span>

      <span class="icon-button" title="刷新" @click="refresh">
        <el-icon :size="17"><Refresh /></el-icon>
      </span>

      <span class="icon-button" :title="isDark ? '切换浅色' : '切换深色'" @click="toggleDark">
        <el-icon :size="17">
          <component :is="isDark ? 'Sunny' : 'Moon'" />
        </el-icon>
      </span>

      <span class="icon-button" :title="isFullscreen ? '退出全屏' : '全屏'" @click="toggleFullscreen">
        <el-icon :size="17"><FullScreen /></el-icon>
      </span>

      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-chip">
          <el-avatar :size="30" :src="userStore.avatar || ''" :icon="UserFilled" />
          <span class="user-name">{{ userStore.nickName || userStore.name }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useNoticeStore from '@/store/modules/notice'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isFullscreen = ref(false)
const isDark = ref(localStorage.getItem('muyue_theme') === 'dark')
const noticeStore = useNoticeStore()
const noticeCount = computed(() => noticeStore.unreadCount)
const recentNotices = computed(() => noticeStore.recentNotices)

let noticeTimer = null

function handleNoticeClick(command) {
  router.push('/notice')
}

function openScreen() {
  const { href } = router.resolve('/bigscreen')
  window.open(href, '_blank')
}

const breadcrumbs = computed(() => {
  const list = []
  route.matched.forEach((item) => {
    if (item.meta && item.meta.title) {
      list.push({ path: item.path, title: item.meta.title })
    }
  })
  return list
})

function applyTheme() {
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('muyue_theme', isDark.value ? 'dark' : 'light')
}

function toggleDark() {
  isDark.value = !isDark.value
  applyTheme()
}

function toggleSideBar() {
  appStore.toggleSideBar()
}

function refresh() {
  window.location.reload()
}

function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen()
    isFullscreen.value = false
  } else {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logOut().then(() => {
          window.location.href = '/login'
        })
      })
      .catch(() => {})
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}

onMounted(() => {
  applyTheme()
  noticeStore.refresh()
  // 轮询新公告（大厂做法是 WebSocket 推送，单体内嵌库场景用轻量轮询足够）
  noticeTimer = setInterval(() => noticeStore.fetchUnread(), 60000)
})

onUnmounted(() => {
  if (noticeTimer) clearInterval(noticeTimer)
})

// 路由切换时刷新（兜底，正常情况下读详情后通知页会主动触发刷新）
watch(() => route.path, () => {
  noticeStore.refresh()
})
</script>
