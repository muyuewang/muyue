<template>
  <div class="app-container page-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="文件名">
        <el-input v-model="queryParams.fileName" placeholder="请输入文件名关键字" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-toolbar">
      <el-col :span="1.5">
        <el-upload :show-file-list="false" :http-request="customUpload" accept=".pdf,.doc,.docx,.xls,.xlsx,.csv,.txt,.md,.ppt,.pptx,.zip,.rar,.7z">
          <el-button type="primary" plain :icon="Upload" v-hasPermi="['tool:file:upload']">上传文件</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" v-hasPermi="['tool:file:remove']" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table :data="fileList" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="文件名" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="handlePreview(row)">{{ row.fileName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag type="info" effect="plain">{{ (row.fileType || '').toUpperCase() }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="110" align="right">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="上传者" prop="createBy" width="110" align="center" />
      <el-table-column label="上传时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="180" align="center" fixed="right" class-name="op-column">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="handlePreview(row)">预览</el-button>
          <el-divider direction="vertical" />
          <el-dropdown :ref="el => setMoreRef(el, $index)" trigger="click" @command="(cmd) => handleAction(cmd, row, $index)">
            <el-button link type="primary">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="download" icon="Download">下载</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['tool:file:remove']">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="previewTitle" v-model="previewVisible" width="900px" top="5vh" append-to-body destroy-on-close @closed="cleanupPreview">
      <div v-if="previewType === 'unsupported'" class="preview-tip">该格式暂不支持在线预览，请下载后查看</div>
      <iframe v-else-if="previewType === 'pdf'" :src="blobUrl" class="preview-frame"></iframe>
      <div v-else-if="previewType === 'docx'" ref="docxRef" class="preview-docx"></div>
      <div v-else-if="previewType === 'excel'" class="preview-excel" v-html="excelHtml"></div>
      <pre v-else-if="previewType === 'text'" class="preview-text">{{ textContent }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload, Delete, View, ArrowDown } from '@element-plus/icons-vue'
import { renderAsync } from 'docx-preview'
import * as XLSX from 'xlsx'
import { listFile, uploadFile, previewFile, downloadFile, delFile } from '@/api/tool/file'

const loading = ref(false)
const fileList = ref([])
const total = ref(0)
const selectedIds = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, fileName: '' })

const previewVisible = ref(false)
const previewTitle = ref('')
const previewType = ref('')
const blobUrl = ref('')
const excelHtml = ref('')
const textContent = ref('')
const docxRef = ref()

function formatSize(size) {
  if (size === null || size === undefined) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

function getList() {
  loading.value = true
  listFile(queryParams).then((res) => {
    fileList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryParams.fileName = ''
  handleQuery()
}
function handleSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.fileId)
}

function customUpload(options) {
  uploadFile(options.file).then(() => {
    ElMessage.success('上传成功')
    getList()
  })
}

function cleanupPreview() {
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = ''
  }
  excelHtml.value = ''
  textContent.value = ''
}

async function handlePreview(row) {
  previewTitle.value = row.fileName
  previewVisible.value = true
  const ext = (row.fileType || '').toLowerCase()
  const blob = await previewFile(row.fileId)

  if (ext === 'pdf') {
    previewType.value = 'pdf'
    cleanupPreview()
    blobUrl.value = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
  } else if (ext === 'docx') {
    previewType.value = 'docx'
    const buffer = await blob.arrayBuffer()
    setTimeout(() => {
      if (docxRef.value) renderAsync(buffer, docxRef.value)
    }, 50)
  } else if (['xls', 'xlsx', 'csv'].includes(ext)) {
    previewType.value = 'excel'
    const buffer = await blob.arrayBuffer()
    const wb = XLSX.read(buffer, { type: 'array' })
    excelHtml.value = XLSX.utils.sheet_to_html(wb.Sheets[wb.SheetNames[0]])
  } else if (['txt', 'md'].includes(ext)) {
    previewType.value = 'text'
    textContent.value = await blob.text()
  } else {
    previewType.value = 'unsupported'
  }
}

function handleDownload(row) {
  downloadFile(row.fileId).then((blob) => {
    const url = URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('下载已开始')
  })
}

function handleDelete(row) {
  const ids = row.fileId ? [row.fileId] : selectedIds.value
  ElMessageBox.confirm('是否确认删除选中的附件？删除后不可恢复。', '提示', {
    type: 'warning'
  }).then(() => delFile(ids.join(','))).then(() => {
    ElMessage.success('删除成功')
    getList()
  })
}

const moreRefs = ref([])
function setMoreRef(el, index) { moreRefs.value[index] = el }
function closeMore(index) { moreRefs.value[index]?.handleClose?.() }

function handleAction(command, row, index) {
  closeMore(index)
  if (command === 'download') {
    handleDownload(row)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

onMounted(getList)
onBeforeUnmount(cleanupPreview)
</script>

<style scoped>
.preview-frame {
  width: 100%;
  height: 70vh;
  border: none;
}
.preview-docx {
  height: 70vh;
  overflow: auto;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 8px;
}
.preview-excel {
  height: 70vh;
  overflow: auto;
  padding: 12px;
  background: var(--bg-hover);
  border-radius: 8px;
}
.preview-excel :deep(table) {
  border-collapse: collapse;
  font-size: 12px;
}
.preview-excel :deep(td) {
  border: 1px solid var(--border-color);
  padding: 4px 10px;
  white-space: nowrap;
}
.preview-text {
  height: 70vh;
  overflow: auto;
  margin: 0;
  padding: 16px;
  font-size: 13px;
  line-height: 1.7;
  background: var(--bg-hover);
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-all;
}
.preview-tip {
  padding: 60px 0;
  text-align: center;
  color: var(--text-secondary);
}
</style>
