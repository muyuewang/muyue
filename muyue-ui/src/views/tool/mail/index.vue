<template>
  <div class="app-container page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="邮箱配置" name="config">
        <el-form ref="configRef" :model="config" :rules="configRules" label-width="120px" style="max-width: 620px; margin-top: 10px">
          <el-form-item label="SMTP 服务器" prop="host">
            <el-input v-model="config.host" placeholder="如 smtp.qq.com / smtp.163.com" />
          </el-form-item>
          <el-form-item label="端口" prop="port">
            <el-input-number v-model="config.port" :min="1" :max="65535" controls-position="right" />
            <span class="form-tip">SSL 常用 465，STARTTLS 常用 587</span>
          </el-form-item>
          <el-form-item label="启用 SSL" prop="sslEnabled">
            <el-switch v-model="config.sslEnabled" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="发件人昵称">
            <el-input v-model="config.nickname" placeholder="收件人看到的发件人名称" />
          </el-form-item>
          <el-form-item label="发件账号" prop="username">
            <el-input v-model="config.username" placeholder="通常是邮箱地址" />
          </el-form-item>
          <el-form-item label="授权码/密码" prop="password">
            <el-input v-model="config.password" type="password" show-password placeholder="邮箱服务商生成的授权码" />
          </el-form-item>
          <el-form-item label="发件人邮箱">
            <el-input v-model="config.fromAddr" placeholder="留空则使用发件账号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" v-hasPermi="['tool:mail:edit']" @click="onSaveConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="发送邮件" name="send">
        <el-form ref="sendRef" :model="sendForm" :rules="sendRules" label-width="80px" style="margin-top: 10px">
          <el-form-item label="收件人" prop="to">
            <el-input v-model="sendForm.to" placeholder="多个收件人用英文逗号分隔" />
          </el-form-item>
          <el-form-item label="主题" prop="subject">
            <el-input v-model="sendForm.subject" placeholder="请输入邮件主题" />
          </el-form-item>
          <el-form-item label="正文">
            <div class="editor-wrap">
              <Toolbar class="editor-toolbar" :editor="editorRef" :default-config="toolbarConfig" />
              <Editor
                v-model="sendForm.content"
                class="editor-content"
                :default-config="editorConfig"
                @on-created="handleCreated"
              />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="sending" v-hasPermi="['tool:mail:send']" @click="handleSend">发 送</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
defineOptions({ name: 'Mail' })
import { onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { getMailConfig, saveMailConfig, sendMail } from '@/api/tool/mail'

const activeTab = ref('config')
const configRef = ref()
const sendRef = ref()
const sending = ref(false)

const defaultConfig = {
  configId: 1, host: '', port: 465, sslEnabled: '1',
  nickname: '', username: '', password: '', fromAddr: ''
}
const config = reactive({ ...defaultConfig })
const sendForm = reactive({ to: '', subject: '', content: '' })

const configRules = {
  host: [{ required: true, message: 'SMTP 服务器不能为空', trigger: 'blur' }],
  username: [{ required: true, message: '发件账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '授权码/密码不能为空', trigger: 'blur' }]
}
const sendRules = {
  to: [{ required: true, message: '收件人不能为空', trigger: 'blur' }],
  subject: [{ required: true, message: '主题不能为空', trigger: 'blur' }]
}

// 富文本编辑器
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入邮件正文，支持富文本...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        const reader = new FileReader()
        reader.onload = () => insertFn(reader.result, file.name, '')
        reader.readAsDataURL(file)
      }
    }
  }
}

function handleCreated(editor) {
  editorRef.value = editor
}

onMounted(async () => {
  const res = await getMailConfig()
  Object.assign(config, res.data || defaultConfig)
})

onBeforeUnmount(() => {
  if (editorRef.value) editorRef.value.destroy()
})

function onSaveConfig() {
  configRef.value.validate((valid) => {
    if (!valid) return
    saveMailConfig(config).then(() => ElMessage.success('配置已保存'))
  })
}

function handleSend() {
  sendRef.value.validate((valid) => {
    if (!valid) return
    sending.value = true
    sendMail(sendForm).then(() => {
      ElMessage.success('发送成功，请到收件箱查收')
    }).finally(() => {
      sending.value = false
    })
  })
}
</script>

<style scoped>
.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}
.editor-wrap {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  z-index: 100;
}
.editor-toolbar {
  border-bottom: 1px solid var(--border-color);
}
.editor-content {
  height: 320px;
  overflow-y: auto;
}
</style>
