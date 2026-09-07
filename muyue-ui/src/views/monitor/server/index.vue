<template>
  <div class="app-container">
    <div class="monitor-toolbar">
      <el-button :icon="Refresh" :loading="loading" @click="getServerInfo">刷新</el-button>
      <el-switch
        v-model="auto"
        active-text="自动刷新 (10s)"
        inline-prompt
        style="margin-left: 14px"
        @change="toggleAuto"
      />
    </div>

    <el-row :gutter="16">
      <!-- CPU -->
      <el-col :xs="24" :md="12" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">CPU</div>
          <div class="gauge-wrap">
            <el-progress type="dashboard" :percentage="num(server.cpu.total)" :color="colorOf(server.cpu.total)" />
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="核心数">{{ server.cpu.cpuNum }}</el-descriptions-item>
            <el-descriptions-item label="用户使用率">{{ server.cpu.used }}%</el-descriptions-item>
            <el-descriptions-item label="系统使用率">{{ server.cpu.sys }}%</el-descriptions-item>
            <el-descriptions-item label="等待">{{ server.cpu.wait }}%</el-descriptions-item>
            <el-descriptions-item label="空闲">{{ server.cpu.free }}%</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <!-- 内存 -->
      <el-col :xs="24" :md="12" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">内存</div>
          <div class="gauge-wrap">
            <el-progress type="dashboard" :percentage="num(server.mem.usage)" :color="colorOf(server.mem.usage)" />
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="总内存">{{ server.mem.total }} GB</el-descriptions-item>
            <el-descriptions-item label="已用">{{ server.mem.used }} GB</el-descriptions-item>
            <el-descriptions-item label="剩余">{{ server.mem.free }} GB</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 服务器信息 -->
      <el-col :xs="24" :md="12" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">服务器信息</div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="主机名称">{{ server.sys.computerName }}</el-descriptions-item>
            <el-descriptions-item label="IP 地址">{{ server.sys.computerIp }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ server.sys.osName }}</el-descriptions-item>
            <el-descriptions-item label="系统架构">{{ server.sys.osArch }}</el-descriptions-item>
            <el-descriptions-item label="运行目录">{{ server.sys.userDir }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <!-- JVM -->
      <el-col :xs="24" :md="12" style="margin-bottom: 16px">
        <div class="page-container">
          <div class="section-title">Java 虚拟机 (JVM)</div>
          <div class="gauge-wrap">
            <el-progress type="dashboard" :percentage="num(server.jvm.usage)" :color="colorOf(server.jvm.usage)" />
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="名称">{{ server.jvm.name }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ server.jvm.version }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ server.jvm.startTime }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ server.jvm.runTime }}</el-descriptions-item>
            <el-descriptions-item label="已用 / 最大">{{ server.jvm.used }} / {{ server.jvm.max }} MB</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>

    <!-- 磁盘 -->
    <div class="page-container" style="margin-bottom: 16px">
      <div class="section-title">磁盘状态</div>
      <el-table :data="server.sysFiles" border>
        <el-table-column label="盘符路径" prop="dirName" min-width="160" show-overflow-tooltip />
        <el-table-column label="文件系统" prop="sysTypeName" min-width="110" show-overflow-tooltip />
        <el-table-column label="盘符类型" prop="typeName" min-width="110" show-overflow-tooltip />
        <el-table-column label="总大小(GB)" prop="total" width="110" />
        <el-table-column label="可用(GB)" prop="free" width="110" />
        <el-table-column label="已用(GB)" prop="used" width="110" />
        <el-table-column label="使用率" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="num(row.usage)" :color="colorOf(row.usage)" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getServer } from '@/api/monitor/server'

const loading = ref(false)
const auto = ref(false)
let timer = null
const server = reactive({ cpu: {}, mem: {}, sys: {}, jvm: {}, sysFiles: [] })

function num(v) {
  return Number(v || 0)
}
function colorOf(v) {
  const n = Number(v || 0)
  return n < 60 ? '#67c23a' : n < 85 ? '#e6a23c' : '#f56c6c'
}
function getServerInfo() {
  loading.value = true
  getServer()
    .then((res) => {
      Object.assign(server, res.data)
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}
function toggleAuto(v) {
  if (v) {
    timer = setInterval(getServerInfo, 10000)
  } else if (timer) {
    clearInterval(timer)
  }
}

onMounted(getServerInfo)
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.app-container {
  padding: 18px;
}
.monitor-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.gauge-wrap {
  display: flex;
  justify-content: center;
  padding: 6px 0;
}
</style>
