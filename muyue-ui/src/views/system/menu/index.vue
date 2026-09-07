<template>
  <div class="page-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="菜单名称">
        <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
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
      <el-button v-hasPermi="['system:menu:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
      <el-button icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
    </div>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuTable"
      row-key="menuId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      border
    >
      <el-table-column label="菜单名称" prop="menuName" min-width="180" show-overflow-tooltip />
      <el-table-column label="图标" width="70" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.icon && row.icon !== '#'"><component :is="row.icon" /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="orderNum" width="70" align="center" />
      <el-table-column label="权限标识" prop="perms" min-width="150" show-overflow-tooltip />
      <el-table-column label="组件路径" prop="component" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="170" align="center" fixed="right" class-name="op-column">
        <template #default="{ row, $index }">
          <el-button v-hasPermi="['system:menu:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
          <el-divider direction="vertical" />
          <el-dropdown :ref="el => setMoreRef(el, $index)" trigger="click" @command="(cmd) => handleAction(cmd, row, $index)">
            <el-button link type="primary">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="add" icon="Plus" v-hasPermi="['system:menu:add']">新增下级菜单</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['system:menu:remove']">删除菜单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改菜单 -->
    <el-dialog v-model="open" :title="title" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            check-strictly
            style="width: 100%"
            placeholder="请选择上级菜单"
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item label="菜单图标">
              <el-input v-model="form.icon" placeholder="如 Setting、User" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item label="路由地址">
              <el-input v-model="form.path" placeholder="如 user（一级目录为 system）" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType === 'C'" :span="24">
            <el-form-item label="组件路径">
              <el-input v-model="form.component" placeholder="如 system/user/index" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="权限字符">
              <el-input v-model="form.perms" placeholder="如 system:user:list" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12">
            <el-form-item label="显示状态">
              <el-radio-group v-model="form.visible">
                <el-radio value="0">显示</el-radio>
                <el-radio value="1">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addMenu, delMenu, getMenu, listMenu, treeselect, updateMenu } from '@/api/system/menu'
import { handleTree } from '@/utils/tree'

const loading = ref(false)
const open = ref(false)
const title = ref('')
const menuList = ref([])
const ids = ref([])
const menuOptions = ref([])
const refreshTable = ref(true)
const isExpandAll = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  menuName: undefined,
  status: undefined
})

const form = reactive({
  menuId: undefined,
  parentId: 0,
  menuName: undefined,
  orderNum: 0,
  path: undefined,
  component: undefined,
  query: undefined,
  isFrame: '1',
  isCache: '0',
  menuType: 'M',
  visible: '0',
  status: '0',
  perms: undefined,
  icon: undefined,
  remark: undefined
})

const rules = {
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
}

const menuTable = computed(() => handleTree(menuList.value, 'menuId', 'parentId', 'children'))

function getList() {
  loading.value = true
  listMenu(queryParams)
    .then((res) => {
      menuList.value = res.data || []
    })
    .finally(() => {
      loading.value = false
    })
}

function getTreeselect() {
  treeselect().then((res) => {
    menuOptions.value = [{ id: 0, label: '主类目', children: res.data || [] }]
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryParams.menuName = undefined
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
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    orderNum: 0,
    path: undefined,
    component: undefined,
    query: undefined,
    isFrame: '1',
    isCache: '0',
    menuType: 'M',
    visible: '0',
    status: '0',
    perms: undefined,
    icon: undefined,
    remark: undefined
  })
  formRef.value?.clearValidate()
}

function handleAdd(row) {
  reset()
  getTreeselect()
  if (row && row.menuId) {
    form.parentId = row.menuId
  }
  open.value = true
  title.value = '添加菜单'
}

function handleUpdate(row) {
  reset()
  getTreeselect()
  getMenu(row.menuId).then((res) => {
    Object.assign(form, res.data || {})
    open.value = true
    title.value = '修改菜单'
  })
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (form.menuId) {
      updateMenu(form).then(() => {
        ElMessage.success('修改成功')
        open.value = false
        getList()
      })
    } else {
      addMenu(form).then(() => {
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
  ElMessageBox.confirm(`是否确认删除名称为"${row.menuName}"的数据项？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delMenu(row.menuId))
    .then(() => {
      getList()
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
