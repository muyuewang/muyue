<template>
  <div class="page-container">
    <el-row :gutter="12">
      <!-- 左侧部门树 -->
      <el-col :span="5">
        <el-input v-model="deptName" placeholder="请输入部门名称" clearable prefix-icon="Search" />
        <el-tree
          ref="deptTreeRef"
          :data="deptOptions"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          highlight-current
          default-expand-all
          style="margin-top: 10px"
          @node-click="handleNodeClick"
        />
      </el-col>

      <!-- 右侧用户列表 -->
      <el-col :span="19">
        <el-form :model="queryParams" :inline="true" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable style="width: 170px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号" clearable style="width: 170px" @keyup.enter="handleQuery" />
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
          <el-button v-hasPermi="['system:user:add']" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
          <el-button v-hasPermi="['system:user:remove']" type="danger" icon="Delete" :disabled="ids.length === 0" @click="handleDelete()">删除</el-button>
        </div>

        <el-table v-loading="loading" :data="userList" border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户ID" prop="userId" width="90" />
          <el-table-column label="登录账号" prop="userName" min-width="110" show-overflow-tooltip />
          <el-table-column label="用户昵称" prop="nickName" min-width="110" show-overflow-tooltip />
          <el-table-column label="部门" prop="deptName" min-width="120" show-overflow-tooltip />
          <el-table-column label="手机号码" prop="phonenumber" min-width="120" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="handleStatusChange(row)" />
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="150" align="center" fixed="right" class-name="op-column">
            <template #default="{ row }">
              <el-button v-hasPermi="['system:user:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
              <el-divider direction="vertical" />
              <el-dropdown trigger="click" @command="(cmd) => handleAction(cmd, row)">
                <span class="op-more">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="resetPwd" icon="Key" v-hasPermi="['system:user:resetPwd']">重置密码</el-dropdown-item>
                    <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['system:user:remove']">删除用户</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
      </el-col>
    </el-row>

    <!-- 新增/修改用户 -->
    <el-dialog v-model="open" :title="title" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptOptions"
                :props="{ label: 'label', children: 'children' }"
                node-key="id"
                check-strictly
                style="width: 100%"
                placeholder="请选择归属部门"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" maxlength="11" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" maxlength="50" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col v-if="!form.userId" :span="12">
            <el-form-item label="登录账号" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col v-if="!form.userId" :span="12">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择">
                <el-option label="男" value="0" />
                <el-option label="女" value="1" />
                <el-option label="未知" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addUser, changeUserStatus, delUser, getUser, listUser, resetUserPwd, updateUser } from '@/api/system/user'
import { deptTreeSelect } from '@/api/system/dept'
import { optionselect } from '@/api/system/role'

const loading = ref(false)
const open = ref(false)
const title = ref('')
const userList = ref([])
const total = ref(0)
const ids = ref([])
const deptOptions = ref([])
const roleOptions = ref([])
const deptName = ref('')
const deptTreeRef = ref(null)
const formRef = ref(null)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined
  },
  rules: {
    userName: [
      { required: true, message: '登录账号不能为空', trigger: 'blur' },
      { min: 2, max: 20, message: '长度必须在 2 到 20 个字符之间', trigger: 'blur' }
    ],
    nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
    password: [{ required: true, message: '登录密码不能为空', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
    phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
  }
})

const { form, queryParams, rules } = data

/** 部门树过滤 */
watch(deptName, (val) => {
  deptTreeRef.value?.filter(val)
})

function filterNode(value, nodeData) {
  if (!value) return true
  return nodeData.label.indexOf(value) !== -1
}

function handleNodeClick(node) {
  queryParams.deptId = node.id
  getList()
}

/** 查询用户列表 */
function getList() {
  loading.value = true
  listUser(queryParams)
    .then((res) => {
      userList.value = res.data.rows || []
      total.value = res.data.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

/** 查询部门树 */
function getDeptTree() {
  deptTreeSelect().then((res) => {
    deptOptions.value = res.data || []
  })
}

/** 查询角色下拉 */
function getRoleOptions() {
  optionselect().then((res) => {
    roleOptions.value = res.data || []
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.userName = undefined
  queryParams.phonenumber = undefined
  queryParams.status = undefined
  queryParams.deptId = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.userId)
}

function reset() {
  Object.assign(form, {
    userId: undefined,
    deptId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: '0',
    status: '0',
    remark: undefined,
    roleIds: []
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加用户'
}

function handleUpdate(row) {
  reset()
  const userId = row.userId || ids.value[0]
  getUser(userId).then((res) => {
    const result = res.data
    Object.assign(form, {
      ...result.user,
      roleIds: result.roleIds || [],
      password: undefined
    })
    open.value = true
    title.value = '修改用户'
  })
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  ElMessageBox.confirm(`确认要${text}"${row.userName}"用户吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => changeUserStatus(row.userId, row.status))
    .then(() => {
      ElMessage.success(text + '成功')
    })
    .catch(() => {
      row.status = row.status === '0' ? '1' : '0'
    })
}

function handleResetPwd(row) {
  ElMessageBox.prompt(`请输入"${row.userName}"的新密码`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: '密码长度必须在 5 到 20 之间'
  })
    .then(({ value }) => resetUserPwd(row.userId, value))
    .then(() => {
      ElMessage.success('密码重置成功')
    })
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (form.userId) {
      updateUser(form).then(() => {
        ElMessage.success('修改成功')
        open.value = false
        getList()
      })
    } else {
      addUser(form).then(() => {
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
  const userIds = row?.userId || ids.value.join(',')
  ElMessageBox.confirm(`是否确认删除用户编号为"${userIds}"的数据项？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delUser(userIds))
    .then(() => {
      getList()
      ElMessage.success('删除成功')
    })
}

function handleAction(command, row) {
  if (command === 'resetPwd') {
    handleResetPwd(row)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

onMounted(() => {
  getList()
  getDeptTree()
  getRoleOptions()
})
</script>
