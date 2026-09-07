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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import { listNotice, getUnreadCount } from '@/api/system/notice'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isFullscreen = ref(false)
const isDark = ref(localStorage.getItem('muyue_theme') === 'dark')
const noticeCount = ref(0)
const recentNotices = ref([])

function loadNotices() {
  listNotice({ pageNum: 1, pageSize: 5 }).then((res) => {
    recentNotices.value = res.rows || []
  }).catch(() => {})
  getUnreadCount().then((res) => {
    noticeCount.value = res.data || 0
  }).catch(() => {})
}

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
  loadNotices()
})

// 路由变化时刷新未读数（从通知公告页读完返回后角标即时更新）
watch(() => route.path, () => {
  loadNotices()
})
</script>
