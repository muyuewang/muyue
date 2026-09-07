<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="表名">
        <el-input v-model="queryParams.tableName" placeholder="请输入表名关键字" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="queryParams.tableName = ''; handleQuery()">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="选择数据库表，预览/下载按项目约定生成的后端与前端 CRUD 代码（Entity / Mapper / Service / Controller / api.js / index.vue / 菜单SQL）"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 12px"
    />

    <el-table :data="tableList" v-loading="loading" border class="page-container">
      <el-table-column label="表名" prop="tableName" min-width="180" show-overflow-tooltip />
      <el-table-column label="实体类名" prop="className" min-width="140" show-overflow-tooltip />
      <el-table-column label="业务名" prop="businessName" min-width="120" show-overflow-tooltip />
      <el-table-column label="字段数" width="90" align="center">
        <template #default="{ row }">{{ row.columns?.length || 0 }}</template>
      </el-table-column>
      <el-table-column label="主键" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.pkColumn ? row.pkColumn.columnName : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right" class-name="op-column">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" v-hasPermi="['tool:gen:query']" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" :icon="Download" v-hasPermi="['tool:gen:code']" @click="handleDownload(row)">下载代码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 代码预览 -->
    <el-dialog :title="'生成代码预览 - ' + previewTable" v-model="previewOpen" width="960px" top="6vh" append-to-body>
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane v-for="(content, name) in previewFiles" :key="name" :label="name" :name="name">
          <pre class="code-block"><code>{{ content }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Download } from '@element-plus/icons-vue'
import { listGenTables, previewGenTable, downloadGenCode } from '@/api/tool/gen'

const loading = ref(false)
const tableList = ref([])
const total = ref(0)
const previewOpen = ref(false)
const previewTable = ref('')
const previewFiles = ref({})
const activeTab = ref('')

const queryParams = reactive({ pageNum: 1, pageSize: 10, tableName: '' })

function getList() {
  loading.value = true
  listGenTables(queryParams).then((res) => {
    tableList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function handlePreview(row) {
  previewGenTable(row.tableName).then((res) => {
    previewFiles.value = res.data
    previewTable.value = row.tableName
    activeTab.value = Object.keys(res.data)[0] || ''
    previewOpen.value = true
  })
}
function handleDownload(row) {
  downloadGenCode(row.tableName).then((blob) => {
    const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/zip' }))
    const link = document.createElement('a')
    link.href = url
    link.download = row.tableName + '-code.zip'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('代码包下载已开始')
  })
}

onMounted(getList)
</script>

<style scoped>
.app-container {
  padding: 18px;
}
.code-block {
  max-height: 60vh;
  overflow: auto;
  margin: 0;
  padding: 14px;
  font-size: 12px;
  line-height: 1.6;
  background: var(--bg-hover);
  border-radius: 8px;
  white-space: pre;
}
</style>
