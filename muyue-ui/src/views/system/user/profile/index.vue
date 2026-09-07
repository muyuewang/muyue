<template>
  <div class="page-container">
    <el-row :gutter="16">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>个人信息</span>
          </template>
          <div style="text-align: center; padding: 10px 0 16px">
            <el-avatar :size="100" :src="userStore.avatar || ''" :icon="UserFilled" />
            <div style="margin-top: 14px">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                name="avatarfile"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
              >
                <el-button type="primary" size="small" icon="Upload">更换头像</el-button>
              </el-upload>
            </div>
            <div style="margin-top: 8px; font-size: 12px; color: #909399">支持 jpg / png / gif，小于 5MB</div>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="登录账号">{{ user.userName }}</el-descriptions-item>
            <el-descriptions-item label="用户昵称">{{ user.nickName }}</el-descriptions-item>
            <el-descriptions-item label="手机号码">{{ user.phonenumber }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">{{ user.deptName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属角色">{{ roleGroup || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建日期">{{ user.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧资料维护 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>基本资料</span>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="90px" style="max-width: 480px">
                <el-form-item label="用户昵称" prop="nickName">
                  <el-input v-model="infoForm.nickName" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phonenumber">
                  <el-input v-model="infoForm.phonenumber" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="infoForm.sex">
                    <el-radio value="0">男</el-radio>
                    <el-radio value="1">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitInfo">保存</el-button>
                  <el-button @click="loadProfile">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="resetPwd">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px" style="max-width: 480px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitPwd">保存</el-button>
                  <el-button @click="resetPwdForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'Profile' })
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'
import { getUserProfile, updateUserProfile, updateUserPwd } from '@/api/system/profile'

const userStore = useUserStore()

const activeTab = ref('userinfo')
const roleGroup = ref('')
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/system/user/profile/avatar'
const uploadHeaders = { Authorization: getToken() || '' }

const user = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '0',
  deptName: '',
  createTime: ''
})

const infoForm = reactive({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '0'
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

const equalToPassword = (rule, value, callback) => {
  if (pwdForm.newPassword !== value) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 5, max: 20, message: '长度必须在 5 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    { validator: equalToPassword, trigger: 'blur' }
  ]
}

function loadProfile() {
  getUserProfile().then((res) => {
    const data = res.data
    Object.assign(user, data.user, { deptName: (data.user && data.user.deptName) || '' })
    Object.assign(infoForm, {
      nickName: data.user.nickName,
      phonenumber: data.user.phonenumber,
      email: data.user.email,
      sex: data.user.sex
    })
    roleGroup.value = data.roleGroup
    userStore.avatar = data.user.avatar || ''
  })
}

function beforeAvatarUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp'].indexOf(file.type) !== -1
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('头像只能是 jpg / png / gif / bmp 格式')
  }
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB')
  }
  return isImage && isLt5M
}

function handleAvatarSuccess(res) {
  if (res.code === 200) {
    userStore.avatar = res.data.imgUrl
    ElMessage.success('头像修改成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

function handleAvatarError() {
  ElMessage.error('上传失败，请重试')
}

function submitInfo() {
  infoFormRef.value.validate((valid) => {
    if (!valid) return
    updateUserProfile(infoForm).then(() => {
      ElMessage.success('修改成功')
      loadProfile()
      userStore.nickName = infoForm.nickName
    })
  })
}

function submitPwd() {
  pwdFormRef.value.validate((valid) => {
    if (!valid) return
    updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword).then(() => {
      ElMessage.success('修改成功，请牢记新密码')
      resetPwdForm()
    })
  })
}

function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

onMounted(() => {
  loadProfile()
})
</script>
