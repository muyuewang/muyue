<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="search-form">
      <el-form-item label="登录名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入登录名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="主机地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="请输入主机地址" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border class="page-container">
      <el-table-column label="会话编号" align="center" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ mask(row.tokenId) }}</template>
      </el-table-column>
      <el-table-column label="登录名称" prop="userName" min-width="120" show-overflow-tooltip />
      <el-table-column label="部门" prop="deptName" min-width="120" show-overflow-tooltip />
      <el-table-column label="主机" prop="ipaddr" min-width="130" />
      <el-table-column label="登录时间" min-width="170">
        <template #default="{ row }">{{ formatTime(row.loginTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" align="center" class-name="op-column">
        <template #default="{ row }">
          <el-button link type="danger" :icon="Delete" v-hasPermi="['monitor:online:forceLogout']" @click="handleForce(row)">强退</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { listOnline, forceLogout } from '@/api/monitor/online'

const loading = ref(false)
const list = ref([])
const queryRef = ref()
const queryParams = reactive({ userName: '', ipaddr: '' })

function getList() {
  loading.value = true
  listOnline(queryParams).then((res) => {
    list.value = res.rows
    loading.value = false
  })
}
function handleQuery() {
  getList()
}
function resetQuery() {
  queryRef.value.resetFields()
  getList()
}
function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
function mask(t) {
  if (!t || t.length < 14) return t
  return t.substring(0, 10) + '****' + t.substring(t.length - 6)
}
function handleForce(row) {
  ElMessageBox.confirm(`是否确认强退会话「${row.userName}」？强退后该用户需重新登录。`, '提示', {
    type: 'warning'
  })
    .then(() => forceLogout(row.tokenId))
    .then(() => {
      ElMessage.success('已强退')
      getList()
    })
}

onMounted(getList)
</script>

<style scoped>
.app-container {
  padding: 18px;
}
</style>
