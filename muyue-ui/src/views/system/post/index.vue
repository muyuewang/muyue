<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="search-form">
      <el-form-item label="岗位编码" prop="postCode">
        <el-input v-model="queryParams.postCode" placeholder="请输入岗位编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗位名称" prop="postName">
        <el-input v-model="queryParams.postName" placeholder="请输入岗位名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="岗位状态" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" v-hasPermi="['system:post:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" v-hasPermi="['system:post:remove']" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table :data="postList" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="岗位编号" prop="postId" width="100" align="center" />
      <el-table-column label="岗位编码" prop="postCode" min-width="110" show-overflow-tooltip />
      <el-table-column label="岗位名称" prop="postName" min-width="120" show-overflow-tooltip />
      <el-table-column label="显示顺序" prop="postSort" width="100" align="center" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="180" align="center" fixed="right" class-name="op-column">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" v-hasPermi="['system:post:edit']" @click="handleUpdate(row)">修改</el-button>
          <el-button link type="primary" :icon="Delete" v-hasPermi="['system:post:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="form.postName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="form.postCode" placeholder="请输入编码名称" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="postSort">
          <el-input-number v-model="form.postSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="岗位状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'Post' })
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { listPost, getPost, addPost, updatePost, delPost } from '@/api/system/post'

const loading = ref(false)
const postList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const selectedIds = ref([])
const queryRef = ref()
const formRef = ref()

const queryParams = reactive({ pageNum: 1, pageSize: 10, postCode: '', postName: '', status: '' })
const form = reactive({ postId: undefined, postCode: '', postName: '', postSort: 0, status: '0', remark: '' })
const rules = {
  postName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
  postCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listPost(queryParams).then((res) => {
    postList.value = res.rows
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
  selectedIds.value = rows.map((r) => r.postId)
}
function reset() {
  Object.assign(form, { postId: undefined, postCode: '', postName: '', postSort: 0, status: '0', remark: '' })
}
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增岗位'
}
function handleUpdate(row) {
  reset()
  getPost(row.postId).then((res) => {
    Object.assign(form, res.data)
    open.value = true
    title.value = '修改岗位'
  })
}
function cancel() {
  open.value = false
}
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    const action = form.postId ? updatePost(form) : addPost(form)
    action.then(() => {
      ElMessage.success(form.postId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}
function handleDelete(row) {
  const ids = row.postId ? [row.postId] : selectedIds.value
  ElMessageBox.confirm('是否确认删除岗位编号为"' + ids.join(',') + '"的数据项？', '提示', {
    type: 'warning'
  }).then(() => delPost(ids.join(','))).then(() => {
    ElMessage.success('删除成功')
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
