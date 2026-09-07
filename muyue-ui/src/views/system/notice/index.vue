<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="search-form">
      <el-form-item label="公告标题" prop="noticeTitle">
        <el-input v-model="queryParams.noticeTitle" placeholder="请输入公告标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="公告类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="公告类型" clearable style="width: 120px">
          <el-option label="通知" value="1" />
          <el-option label="公告" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" v-hasPermi="['system:notice:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" v-hasPermi="['system:notice:remove']" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table :data="noticeList" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="公告编号" prop="noticeId" width="100" align="center" />
      <el-table-column label="公告标题">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.noticeTitle }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="公告类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.noticeType === '1' ? 'warning' : 'success'">{{ row.noticeType === '1' ? '通知' : '公告' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'primary' : 'info'">{{ row.status === '0' ? '正常' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建者" prop="createBy" width="100" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isRead === 1" type="info" effect="plain">已读</el-tag>
          <el-tag v-else type="danger" effect="dark">未读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="150" align="center" class-name="op-column">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" v-hasPermi="['system:notice:query']" @click="handleView(row)">详情</el-button>
          <el-divider direction="vertical" />
          <el-dropdown trigger="click" @command="(cmd) => handleAction(cmd, row)">
            <span class="op-more">
              更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit" icon="Edit" v-hasPermi="['system:notice:edit']">修改公告</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['system:notice:remove']">删除公告</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 公告详情 -->
    <el-dialog :title="viewNotice?.noticeTitle" v-model="viewOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公告类型">
          <el-tag :type="viewNotice?.noticeType === '1' ? 'warning' : 'success'">{{ viewNotice?.noticeType === '1' ? '通知' : '公告' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ viewNotice?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="notice-content">{{ viewNotice?.noticeContent }}</div>
      <div class="notice-footer">—— {{ viewNotice?.createBy }} 发布</div>
    </el-dialog>

    <!-- 新增/修改公告 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio value="1">通知</el-radio>
            <el-radio value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input v-model="form.noticeContent" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">发 布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, View } from '@element-plus/icons-vue'
import { listNotice, getNotice, addNotice, updateNotice, delNotice } from '@/api/system/notice'

const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const open = ref(false)
const viewOpen = ref(false)
const viewNotice = ref(null)
const title = ref('')
const selectedIds = ref([])
const queryRef = ref()
const formRef = ref()

const queryParams = reactive({ pageNum: 1, pageSize: 10, noticeTitle: '', noticeType: '' })
const defaultForm = { noticeId: undefined, noticeTitle: '', noticeType: '1', noticeContent: '', status: '0' }
const form = reactive({ ...defaultForm })
const rules = {
  noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
  noticeContent: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listNotice(queryParams).then((res) => {
    noticeList.value = res.rows
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
  selectedIds.value = rows.map((r) => r.noticeId)
}
function reset() {
  Object.assign(form, defaultForm)
}
function handleAdd() {
  reset()
  open.value = true
  title.value = '发布公告'
}
function handleUpdate(row) {
  reset()
  getNotice(row.noticeId).then((res) => {
    Object.assign(form, res.data)
    open.value = true
    title.value = '修改公告'
  })
}
function handleView(row) {
  getNotice(row.noticeId).then((res) => {
    viewNotice.value = res.data
    viewOpen.value = true
    // 后端已标记已读，刷新列表状态
    getList()
  })
}
function cancel() {
  open.value = false
}
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    const action = form.noticeId ? updateNotice(form) : addNotice(form)
    action.then(() => {
      ElMessage.success(form.noticeId ? '修改成功' : '发布成功')
      open.value = false
      getList()
    })
  })
}
function handleDelete(row) {
  const ids = row.noticeId ? [row.noticeId] : selectedIds.value
  ElMessageBox.confirm('是否确认删除公告编号为"' + ids.join(',') + '"的数据项？', '提示', {
    type: 'warning'
  }).then(() => delNotice(ids.join(','))).then(() => {
    ElMessage.success('删除成功')
    getList()
  })
}

function handleAction(command, row) {
  if (command === 'edit') {
    handleUpdate(row)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

onMounted(getList)
</script>

<style scoped>
.app-container {
  padding: 18px;
}
.notice-content {
  margin-top: 16px;
  padding: 16px;
  line-height: 1.8;
  font-size: 14px;
  color: var(--text-regular);
  background: var(--bg-hover);
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-all;
}
.notice-footer {
  margin-top: 12px;
  text-align: right;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
