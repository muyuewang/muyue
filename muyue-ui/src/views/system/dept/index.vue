<template>
  <div class="page-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="部门名称">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 110px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-toolbar">
      <el-button v-hasPermi="['system:dept:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
      <el-button icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
    </div>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="deptTable"
      row-key="deptId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      border
    >
      <el-table-column label="部门名称" prop="deptName" min-width="180" show-overflow-tooltip />
      <el-table-column label="排序" prop="orderNum" width="70" align="center" />
      <el-table-column label="负责人" prop="leader" width="100" />
      <el-table-column label="联系电话" prop="phone" width="130" />
      <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="170" align="center" fixed="right" class-name="op-column">
        <template #default="{ row, $index }">
          <el-button v-hasPermi="['system:dept:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
          <el-divider direction="vertical" />
          <el-dropdown :ref="el => setMoreRef(el, $index)" trigger="click" @command="(cmd) => handleAction(cmd, row, $index)">
            <el-button link type="primary" class="op-more-trigger">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="add" icon="Plus" v-hasPermi="['system:dept:add']">新增下级部门</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['system:dept:remove']">删除部门</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改部门 -->
    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            check-strictly
            style="width: 100%"
            placeholder="请选择上级部门"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入部门名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.leader" maxlength="20" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" maxlength="11" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" maxlength="50" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'Dept' })
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDept, delDept, deptTreeSelect, getDept, listDept, updateDept } from '@/api/system/dept'
import { handleTree } from '@/utils/tree'

const loading = ref(false)
const open = ref(false)
const title = ref('')
const deptList = ref([])
const deptOptions = ref([])
const refreshTable = ref(true)
const isExpandAll = ref(true)
const formRef = ref(null)

const queryParams = reactive({
  deptName: undefined,
  status: undefined
})

const form = reactive({
  deptId: undefined,
  parentId: 0,
  deptName: undefined,
  orderNum: 0,
  leader: undefined,
  phone: undefined,
  email: undefined,
  status: '0'
})

const rules = {
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
}

const deptTable = computed(() => handleTree(deptList.value, 'deptId', 'parentId', 'children'))

function getList() {
  loading.value = true
  listDept(queryParams)
    .then((res) => {
      deptList.value = res.data || []
    })
    .finally(() => {
      loading.value = false
    })
}

function getTreeselect() {
  deptTreeSelect().then((res) => {
    deptOptions.value = [{ id: 0, label: '主类目', children: res.data || [] }]
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryParams.deptName = undefined
  queryParams.status = undefined
  getList()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function reset() {
  Object.assign(form, {
    deptId: undefined,
    parentId: 0,
    deptName: undefined,
    orderNum: 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: '0'
  })
  formRef.value?.clearValidate()
}

function handleAdd(row) {
  reset()
  getTreeselect()
  if (row && row.deptId) {
    form.parentId = row.deptId
  }
  open.value = true
  title.value = '添加部门'
}

function handleUpdate(row) {
  reset()
  getTreeselect()
  getDept(row.deptId).then((res) => {
    Object.assign(form, res.data || {})
    open.value = true
    title.value = '修改部门'
  })
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (form.deptId) {
      updateDept(form).then(() => {
        ElMessage.success('修改成功')
        open.value = false
        getList()
      })
    } else {
      addDept(form).then(() => {
        ElMessage.success('新增成功')
        open.value = false
        getList()
        getTreeselect()
      })
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleDelete(row) {
  ElMessageBox.confirm(`是否确认删除名称为"${row.deptName}"的数据项？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delDept(row.deptId))
    .then(() => {
      getList()
      getTreeselect()
      ElMessage.success('删除成功')
    })
}

const moreRefs = ref([])
function setMoreRef(el, index) { moreRefs.value[index] = el }
function closeMore() {
  // 兜底：隐藏所有 dropdown 弹出层（含因行 DOM 重建而失联的孤儿弹出层）
  document.querySelectorAll('.el-dropdown__popper').forEach((el) => {
    el.style.display = 'none'
  })
}

function handleAction(command, row, index) {
  closeMore(index)
  if (command === 'add') {
    handleAdd(row)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

onMounted(() => {
  getList()
  getTreeselect()
})
</script>
