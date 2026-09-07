<template>
  <div class="page-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="角色名称">
        <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="权限字符">
        <el-input v-model="queryParams.roleKey" placeholder="请输入权限字符" clearable style="width: 180px" @keyup.enter="handleQuery" />
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
      <el-button v-hasPermi="['system:role:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
      <el-button v-hasPermi="['system:role:remove']" type="danger" icon="Delete" :disabled="ids.length === 0" @click="handleDelete()">删除</el-button>
    </div>

    <el-table v-loading="loading" :data="roleList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="角色编号" prop="roleId" width="100" />
      <el-table-column label="角色名称" prop="roleName" min-width="140" show-overflow-tooltip />
      <el-table-column label="权限字符" prop="roleKey" min-width="140" show-overflow-tooltip />
      <el-table-column label="显示顺序" prop="roleSort" width="100" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['system:role:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
          <el-button v-hasPermi="['system:role:remove']" link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <!-- 新增/修改角色 -->
    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入权限字符" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option label="全部数据权限" value="1" />
            <el-option label="自定义数据权限" value="2" />
            <el-option label="本部门数据权限" value="3" />
            <el-option label="本部门及以下数据权限" value="4" />
            <el-option label="仅本人数据权限" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand">展开/折叠</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll">全选/全不选</el-checkbox>
          <el-tree
            ref="menuRef"
            :data="menuOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            show-checkbox
            :check-strictly="!menuCheckStrictly"
            style="width: 100%; margin-top: 6px; border: 1px solid #e4e7ed; border-radius: 4px; padding: 6px"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addRole, changeRoleStatus, delRole, getRole, listRole, updateRole } from '@/api/system/role'
import { roleMenuTreeselect, treeselect } from '@/api/system/menu'

const loading = ref(false)
const open = ref(false)
const title = ref('')
const roleList = ref([])
const total = ref(0)
const ids = ref([])
const menuOptions = ref([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const menuCheckStrictly = ref(true)
const menuRef = ref(null)
const formRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: undefined,
  roleKey: undefined,
  status: undefined
})

const form = reactive({
  roleId: undefined,
  roleName: undefined,
  roleKey: undefined,
  roleSort: 0,
  dataScope: '1',
  status: '0',
  remark: undefined,
  menuIds: []
})

const rules = {
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
  roleKey: [{ required: true, message: '权限字符不能为空', trigger: 'blur' }],
  roleSort: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listRole(queryParams)
    .then((res) => {
      roleList.value = res.data.rows || []
      total.value = res.data.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function getMenuTreeselect() {
  treeselect().then((res) => {
    menuOptions.value = res.data || []
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.roleName = undefined
  queryParams.roleKey = undefined
  queryParams.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.roleId)
}

function reset() {
  Object.assign(form, {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    dataScope: '1',
    status: '0',
    remark: undefined,
    menuIds: []
  })
  formRef.value?.clearValidate()
}

function handleAdd() {
  reset()
  getMenuTreeselect()
  nextTick(() => {
    menuRef.value?.setCheckedKeys([])
  })
  menuCheckStrictly.value = false
  open.value = true
  title.value = '添加角色'
}

function handleUpdate(row) {
  reset()
  const roleId = row.roleId || ids.value[0]
  menuCheckStrictly.value = true
  Promise.all([getRole(roleId), roleMenuTreeselect(roleId)]).then(([roleRes, treeRes]) => {
    Object.assign(form, roleRes.data || {}, { menuIds: [] })
    menuOptions.value = treeRes.data.menus || []
    nextTick(() => {
      menuRef.value?.setCheckedKeys(treeRes.data.checkedKeys || [])
      menuCheckStrictly.value = false
    })
  })
  open.value = true
  title.value = '修改角色'
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  ElMessageBox.confirm(`确认要${text}"${row.roleName}"角色吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => changeRoleStatus(row.roleId, row.status))
    .then(() => {
      ElMessage.success(text + '成功')
    })
    .catch(() => {
      row.status = row.status === '0' ? '1' : '0'
    })
}

/** 展开/折叠 */
function handleCheckedTreeExpand(value) {
  const nodesMap = menuRef.value?.store?.nodesMap || {}
  for (const key in nodesMap) {
    nodesMap[key].expanded = value
  }
}

/** 全选/全不选 */
function handleCheckedTreeNodeAll(value) {
  menuRef.value?.setCheckedNodes(value ? (menuOptions.value) : [])
}

function getAllMenuIds() {
  const checked = menuRef.value?.getCheckedKeys() || []
  const halfChecked = menuRef.value?.getHalfCheckedKeys() || []
  return checked.concat(halfChecked)
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    form.menuIds = getAllMenuIds()
    if (form.roleId) {
      updateRole(form).then(() => {
        ElMessage.success('修改成功')
        open.value = false
        getList()
      })
    } else {
      addRole(form).then(() => {
        ElMessage.success('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleDelete(row) {
  const roleIds = row?.roleId || ids.value.join(',')
  ElMessageBox.confirm(`是否确认删除角色编号为"${roleIds}"的数据项？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delRole(roleIds))
    .then(() => {
      getList()
      ElMessage.success('删除成功')
    })
}

onMounted(() => {
  getList()
  getMenuTreeselect()
})
</script>
