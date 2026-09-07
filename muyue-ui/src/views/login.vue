<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 品牌区 -->
      <div class="login-brand">
        <h1>{{ title }}</h1>
        <div class="brand-sub">Vue3 · Element Plus · Spring Boot3 · MyBatis-Plus</div>
        <div class="brand-list">
          <div class="brand-item">
            <span class="brand-dot"><el-icon><Lock /></el-icon></span>
            JWT 无状态认证，安全可靠
          </div>
          <div class="brand-item">
            <span class="brand-dot"><el-icon><Menu /></el-icon></span>
            角色菜单动态路由，权限到按钮
          </div>
          <div class="brand-item">
            <span class="brand-dot"><el-icon><DataLine /></el-icon></span>
            操作留痕，日志全程可追溯
          </div>
          <div class="brand-item">
            <span class="brand-dot"><el-icon><Coin /></el-icon></span>
            Oracle / SQLite 双数据源自由切换
          </div>
        </div>
      </div>

      <!-- 表单区 -->
      <div class="login-form-area">
        <div class="login-title">欢迎登录</div>
        <div class="login-tip">请使用您的账号进入沐月管理后台</div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入账号" clearable :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item v-if="captchaEnabled" prop="code">
            <div class="login-captcha" style="width: 100%">
              <el-input
                v-model="loginForm.code"
                placeholder="请输入计算结果"
                style="flex: 1"
                @keyup.enter="handleLogin"
              />
              <img :src="codeUrl" alt="验证码" title="点击刷新" @click="getCode" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button class="login-btn" type="primary" :loading="loading" @click.prevent="handleLogin">
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer-tip">默认账号：admin / admin123</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Coin, DataLine, Lock, Menu, User } from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const title = import.meta.env.VITE_APP_TITLE || '沐月管理系统'
const loginFormRef = ref(null)
const codeUrl = ref('')
const loading = ref(false)
const captchaEnabled = ref(true)
const redirect = ref(undefined)

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
  code: '',
  uuid: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入您的账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入您的密码' }],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}

function getCode() {
  getCodeImg().then((res) => {
    const data = res.data
    captchaEnabled.value = data.captchaEnabled === undefined ? true : data.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = data.img
      loginForm.value.uuid = data.key
    }
  })
}

function handleLogin() {
  loginFormRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    userStore
      .login(loginForm.value)
      .then(() => {
        router.push({ path: redirect.value || '/' })
      })
      .catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
  })
}

onMounted(() => {
  getCode()
  redirect.value = route.query.redirect
})
</script>
