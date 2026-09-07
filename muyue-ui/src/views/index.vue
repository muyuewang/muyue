<template>
  <div>
    <!-- 欢迎横幅 -->
    <div class="welcome-banner" style="margin-bottom: 18px">
      <div style="position: relative; z-index: 1">
        <div class="welcome-title">{{ greeting }}，{{ userStore.nickName || userStore.name }}</div>
        <div class="welcome-desc">
          今天是 {{ today }} · 所属角色：{{ (userStore.roles || []).join('、') || '-' }} · 拥有 {{ userStore.permissions.length }} 项权限
        </div>
      </div>
      <el-icon :size="72" style="position: relative; z-index: 1; opacity: 0.85"><Promotion /></el-icon>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :xs="24" :sm="12" :md="6" style="margin-bottom: 16px">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: card.gradient }">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div>
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 快捷入口 -->
      <el-col :xs="24" :md="10" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">快捷入口</div>
          <el-row :gutter="8">
            <el-col v-for="item in quickList" :key="item.title" :span="8">
              <div class="quick-item" @click="$router.push(item.path)">
                <span class="quick-icon"><el-icon :size="18"><component :is="item.icon" /></el-icon></span>
                <span style="font-size: 13px">{{ item.title }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>

      <!-- 系统信息 -->
      <el-col :xs="24" :md="14" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">系统信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="当前登录人">
              {{ userStore.nickName }}（{{ userStore.name }}）
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in userStore.roles" :key="role" size="small" style="margin-right: 6px">
                {{ role }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="前端技术栈">Vue 3 + Vite + Element Plus</el-descriptions-item>
            <el-descriptions-item label="后端技术栈">Spring Boot 3 + MyBatis-Plus + JWT</el-descriptions-item>
            <el-descriptions-item label="数据库">Oracle / SQLite（可切换）</el-descriptions-item>
            <el-descriptions-item label="权限数量">{{ userStore.permissions.length }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const hours = new Date().getHours()
const greeting = hours < 6 ? '凌晨好' : hours < 12 ? '早上好' : hours < 18 ? '下午好' : '晚上好'
const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const cards = [
  { label: '系统用户', value: 2, icon: 'User', gradient: 'linear-gradient(135deg,#4f6ef7,#3b56d6)' },
  { label: '系统角色', value: 2, icon: 'UserFilled', gradient: 'linear-gradient(135deg,#22c55e,#16a34a)' },
  { label: '系统菜单', value: 25, icon: 'Menu', gradient: 'linear-gradient(135deg,#f59e0b,#d97706)' },
  { label: '组织架构', value: 6, icon: 'OfficeBuilding', gradient: 'linear-gradient(135deg,#7c3aed,#6d28d9)' }
]

const quickList = [
  { title: '用户管理', path: '/system/user', icon: 'User' },
  { title: '订单管理', path: '/order', icon: 'ShoppingCart' },
  { title: '通知公告', path: '/notice', icon: 'Bell' },
  { title: '部门管理', path: '/system/dept', icon: 'OfficeBuilding' },
  { title: '操作记录', path: '/monitor/operlog', icon: 'Document' },
  { title: '在线用户', path: '/monitor/online', icon: 'Connection' },
  { title: '服务器监控', path: '/monitor/server', icon: 'Cpu' },
  { title: '个人中心', path: '/user/profile', icon: 'Setting' }
]
</script>
