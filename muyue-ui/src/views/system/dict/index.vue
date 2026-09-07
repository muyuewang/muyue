<template>
  <div class="dict-layout">
    <!-- 字典类型 -->
    <el-card class="type-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>字典类型</span>
        </div>
      </template>
      <el-form :model="typeQuery" ref="typeQueryRef" :inline="true">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeQuery.dictName" placeholder="字典名称" clearable @keyup.enter="getTypeList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="getTypeList">搜索</el-button>
          <el-button :icon="Refresh" @click="resetTypeQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-row :gutter="10" class="table-toolbar">
        <el-col :span="1.5">
          <el-button type="primary" plain :icon="Plus" v-hasPermi="['system:dict:add']" @click="handleTypeAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :icon="Delete" :disabled="!typeSelected.length" v-hasPermi="['system:dict:remove']" @click="handleTypeDelete">删除</el-button>
        </el-col>
      </el-row>
      <el-table :data="typeList" v-loading="typeLoading" border height="calc(100vh - 280px)" @selection-change="handleTypeSelect" @row-click="handleRowClick" class="page-container">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column label="字典名称" prop="dictName" min-width="120" show-overflow-tooltip />
        <el-table-column label="字典类型" prop="dictType" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="170" align="center" fixed="right" class-name="op-column">
          <template #default="{ row, $index }">
            <el-button link type="primary" :icon="Edit" v-hasPermi="['system:dict:edit']" @click.stop="handleTypeUpdate(row)">修改</el-button>
            <el-divider direction="vertical" />
            <el-dropdown :ref="el => setMoreRef(el, $index)" trigger="click" @command="(cmd) => handleTypeAction(cmd, row, $index)">
              <el-button link type="primary" class="op-more-trigger">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="data" icon="DataLine">字典数据</el-dropdown-item>
                  <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['system:dict:remove']">删除类型</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="typeTotal > 0" :total="typeTotal" v-model:page="typeQuery.pageNum" v-model:limit="typeQuery.pageSize" @pagination="getTypeList" />
    </el-card>

    <!-- 字典数据抽屉 -->
    <el-drawer v-model="drawer" :title="drawerTitle" size="62%" :with-header="true">
      <el-form :model="dataQuery" ref="dataQueryRef" :inline="true">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataQuery.dictLabel" placeholder="字典标签" clearable @keyup.enter="getDataList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="getDataList">搜索</el-button>
          <el-button :icon="Refresh" @click="resetDataQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-row :gutter="10" class="table-toolbar">
        <el-col :span="1.5">
          <el-button type="primary" plain :icon="Plus" v-hasPermi="['system:dict:add']" @click="handleDataAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :icon="Delete" :disabled="!dataSelected.length" v-hasPermi="['system:dict:remove']" @click="handleDataDelete">删除</el-button>
        </el-col>
      </el-row>
      <el-table :data="dataList" v-loading="dataLoading" border @selection-change="handleDataSelect" class="page-container">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column label="字典编码" prop="dictCode" width="90" align="center" />
        <el-table-column label="字典标签" prop="dictLabel" min-width="100" show-overflow-tooltip />
        <el-table-column label="字典键值" prop="dictValue" min-width="100" show-overflow-tooltip />
        <el-table-column label="回显样式" prop="listClass" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.listClass || 'primary')">{{ row.listClass || 'primary' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="dictSort" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right" class-name="op-column">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" v-hasPermi="['system:dict:edit']" @click="handleDataUpdate(row)">修改</el-button>
            <el-button link type="danger" :icon="Delete" v-hasPermi="['system:dict:remove']" @click="handleDataDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="dataTotal > 0" :total="dataTotal" v-model:page="dataQuery.pageNum" v-model:limit="dataQuery.pageSize" @pagination="getDataList" />

      <el-dialog :title="dataTitle" v-model="dataOpen" width="500px" append-to-body>
        <el-form :model="dataForm" :rules="dataRules" ref="dataFormRef" label-width="90px">
          <el-form-item label="字典类型">
            <el-input v-model="dataForm.dictType" :disabled="true" />
          </el-form-item>
          <el-form-item label="数据标签" prop="dictLabel">
            <el-input v-model="dataForm.dictLabel" placeholder="请输入数据标签" />
          </el-form-item>
          <el-form-item label="数据键值" prop="dictValue">
            <el-input v-model="dataForm.dictValue" placeholder="请输入数据键值" />
          </el-form-item>
          <el-form-item label="显示排序" prop="dictSort">
            <el-input-number v-model="dataForm.dictSort" controls-position="right" :min="0" />
          </el-form-item>
          <el-form-item label="回显样式" prop="listClass">
            <el-select v-model="dataForm.listClass" placeholder="请选择" clearable>
              <el-option v-for="item in styleOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="dataForm.status">
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dataOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitDataForm">确 定</el-button>
        </template>
      </el-dialog>
    </el-drawer>

    <!-- 类型新增/修改弹窗 -->
    <el-dialog :title="typeTitle" v-model="typeOpen" width="500px" append-to-body>
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型（如 sys_user_sex）" :disabled="!!typeForm.dictId" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitTypeForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, DataLine } from '@element-plus/icons-vue'
import {
  listType,
  getType,
  addType,
  updateType,
  delType,
  listData,
  addData,
  updateData,
  delData
} from '@/api/system/dict'

/* ---------- 字典类型 ---------- */
const typeLoading = ref(false)
const typeList = ref([])
const typeTotal = ref(0)
const typeSelected = ref([])
const typeQuery = reactive({ pageNum: 1, pageSize: 10, dictName: '', dictType: '' })
const typeOpen = ref(false)
const typeTitle = ref('')
const typeQueryRef = ref()
const typeFormRef = ref()
const typeForm = reactive({ dictId: undefined, dictName: '', dictType: '', status: '0', remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }],
  dictType: [{ required: true, message: '字典类型不能为空', trigger: 'blur' }]
}

function getTypeList() {
  typeLoading.value = true
  listType(typeQuery).then((res) => {
    typeList.value = res.rows
    typeTotal.value = res.total
    typeLoading.value = false
  })
}
function resetTypeQuery() {
  typeQueryRef.value.resetFields()
  getTypeList()
}
function handleTypeSelect(rows) {
  typeSelected.value = rows.map((r) => r.dictId)
}
function handleRowClick(row) {
  openData(row)
}
function resetType() {
  Object.assign(typeForm, { dictId: undefined, dictName: '', dictType: '', status: '0', remark: '' })
}
function handleTypeAdd() {
  resetType()
  typeOpen.value = true
  typeTitle.value = '新增字典类型'
}
function handleTypeUpdate(row) {
  resetType()
  getType(row.dictId).then((res) => {
    Object.assign(typeForm, res.data)
    typeOpen.value = true
    typeTitle.value = '修改字典类型'
  })
}
function submitTypeForm() {
  typeFormRef.value.validate((valid) => {
    if (!valid) return
    const action = typeForm.dictId ? updateType(typeForm) : addType(typeForm)
    action.then(() => {
      ElMessage.success('保存成功')
      typeOpen.value = false
      getTypeList()
    })
  })
}
function handleTypeDelete(row) {
  const ids = row.dictId ? [row.dictId] : typeSelected.value
  ElMessageBox.confirm('是否确认删除字典类型？', '提示', { type: 'warning' })
    .then(() => delType(ids.join(',')))
    .then(() => {
      ElMessage.success('删除成功')
      getTypeList()
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

function handleTypeAction(command, row, index) {
  closeMore(index)
  if (command === 'data') {
    openData(row)
  } else if (command === 'delete') {
    handleTypeDelete(row)
  }
}

/* ---------- 字典数据 ---------- */
const drawer = ref(false)
const drawerTitle = ref('')
const dataLoading = ref(false)
const dataList = ref([])
const dataTotal = ref(0)
const dataSelected = ref([])
const currentType = ref('')
const dataQuery = reactive({ pageNum: 1, pageSize: 10, dictType: '', dictLabel: '' })
const dataOpen = ref(false)
const dataTitle = ref('')
const dataQueryRef = ref()
const dataFormRef = ref()
const dataForm = reactive({ dictCode: undefined, dictSort: 0, dictLabel: '', dictValue: '', dictType: '', listClass: 'primary', status: '0' })
const dataRules = {
  dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
  dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }]
}
const styleOptions = ['primary', 'success', 'info', 'warning', 'danger', '']

function openData(row) {
  currentType.value = row.dictType
  dataQuery.dictType = row.dictType
  dataQuery.dictLabel = ''
  dataQuery.pageNum = 1
  drawerTitle.value = '字典数据 - ' + row.dictName + '（' + row.dictType + '）'
  drawer.value = true
  getDataList()
}
function getDataList() {
  dataLoading.value = true
  listData(dataQuery).then((res) => {
    dataList.value = res.rows
    dataTotal.value = res.total
    dataLoading.value = false
  })
}
function resetDataQuery() {
  dataQueryRef.value.resetFields()
  getDataList()
}
function handleDataSelect(rows) {
  dataSelected.value = rows.map((r) => r.dictCode)
}
function resetData() {
  Object.assign(dataForm, { dictCode: undefined, dictSort: 0, dictLabel: '', dictValue: '', dictType: currentType.value, listClass: 'primary', status: '0' })
}
function handleDataAdd() {
  resetData()
  dataForm.dictType = currentType.value
  dataOpen.value = true
  dataTitle.value = '新增字典数据'
}
function handleDataUpdate(row) {
  resetData()
  Object.assign(dataForm, row)
  dataOpen.value = true
  dataTitle.value = '修改字典数据'
}
function submitDataForm() {
  dataFormRef.value.validate((valid) => {
    if (!valid) return
    const action = dataForm.dictCode ? updateData(dataForm) : addData(dataForm)
    action.then(() => {
      ElMessage.success('保存成功')
      dataOpen.value = false
      getDataList()
    })
  })
}
function handleDataDelete(row) {
  const ids = row.dictCode ? [row.dictCode] : dataSelected.value
  ElMessageBox.confirm('是否确认删除字典数据？', '提示', { type: 'warning' })
    .then(() => delData(ids.join(',')))
    .then(() => {
      ElMessage.success('删除成功')
      getDataList()
    })
}

onMounted(getTypeList)
</script>

<style scoped>
.dict-layout {
  padding: 18px;
}
.type-card {
  border-radius: 12px;
}
.card-header {
  font-weight: 600;
}
</style>
