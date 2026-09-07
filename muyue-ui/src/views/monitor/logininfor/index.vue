<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="search-form">
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="请输入登录地址" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="登录状态" clearable style="width: 120px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-toolbar">
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" v-hasPermi="['monitor:logininfor:remove']" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" v-hasPermi="['monitor:logininfor:remove']" @click="handleClean">清空</el-button>
      </el-col>
    </el-row>

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="访问编号" prop="infoId" width="100" align="center" />
      <el-table-column label="用户名称" prop="userName" width="130" />
      <el-table-column label="登录地址" prop="ipaddr" width="140" />
      <el-table-column label="登录状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" prop="msg" show-overflow-tooltip />
      <el-table-column label="访问时间" prop="accessTime" width="180" />
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button link type="danger" :icon="Delete" v-hasPermi="['monitor:logininfor:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { listLogininfor, delLogininfor, cleanLogininfor } from '@/api/monitor/logininfor'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedIds = ref([])
const queryRef = ref()
const queryParams = reactive({ pageNum: 1, pageSize: 10, ipaddr: '', userName: '', status: '' })

function getList() {
  loading.value = true
  listLogininfor(queryParams).then((res) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryRef.value.resetFields()
  handleQuery()
}
function handleSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.infoId)
}
function handleDelete(row) {
  const ids = row.infoId ? [row.infoId] : selectedIds.value
  ElMessageBox.confirm('是否确认删除登录日志编号为"' + ids.join(',') + '"的数据项？', '提示', {
    type: 'warning'
  }).then(() => delLogininfor(ids.join(','))).then(() => {
    ElMessage.success('删除成功')
    getList()
  })
}
function handleClean() {
  ElMessageBox.confirm('是否确认清空所有登录日志数据项？', '提示', { type: 'warning' })
    .then(() => cleanLogininfor())
    .then(() => {
      ElMessage.success('清空成功')
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
