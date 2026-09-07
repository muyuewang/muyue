<template>
  <div class="page-container">
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="系统模块">
        <el-input v-model="queryParams.title" placeholder="请输入系统模块" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人员">
        <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="queryParams.businessType" placeholder="请选择" clearable style="width: 130px">
          <el-option v-for="item in businessOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 110px">
          <el-option label="正常" :value="0" />
          <el-option label="异常" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 340px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-toolbar">
      <el-button v-hasPermi="['monitor:operlog:remove']" type="danger" icon="Delete" :disabled="ids.length === 0" @click="handleDelete()">删除</el-button>
      <el-button v-hasPermi="['monitor:operlog:remove']" type="danger" icon="DeleteFilled" @click="handleClean">清空</el-button>
    </div>

    <el-table v-loading="loading" :data="logList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="日志编号" prop="operId" width="100" />
      <el-table-column label="系统模块" prop="title" min-width="110" show-overflow-tooltip />
      <el-table-column label="操作类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="businessTagType(row.businessType)">{{ businessLabel(row.businessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="请求方式" prop="requestMethod" width="90" align="center" />
      <el-table-column label="操作人员" prop="operName" width="110" />
      <el-table-column label="部门" prop="deptName" width="120" show-overflow-tooltip />
      <el-table-column label="操作地址" prop="operIp" width="130" />
      <el-table-column label="操作状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '异常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" prop="operTime" width="170" />
      <el-table-column label="耗时" width="90" align="center">
        <template #default="{ row }">{{ row.costTime }} ms</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleView(row)">详细</el-button>
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

    <!-- 日志详情 -->
    <el-dialog v-model="open" title="操作详情" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志编号">{{ detail.operId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ businessLabel(detail.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作地址">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作方法" :span="2">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="log-content">{{ detail.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div class="log-content">{{ detail.jsonResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detail.status === 0 ? 'success' : 'danger'">{{ detail.status === 0 ? '正常' : '异常' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消耗时间">{{ detail.costTime }} ms</el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误信息" :span="2">
          <div class="log-content">{{ detail.errorMsg }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.operTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanOperlog, delOperlog, listOperlog } from '@/api/monitor/operlog'
import { addDateRange } from '@/utils/index'

const loading = ref(false)
const open = ref(false)
const logList = ref([])
const total = ref(0)
const ids = ref([])
const dateRange = ref([])
const detail = ref({})

const businessOptions = [
  { label: '其它', value: 0 },
  { label: '新增', value: 1 },
  { label: '修改', value: 2 },
  { label: '删除', value: 3 },
  { label: '授权', value: 4 },
  { label: '导出', value: 5 },
  { label: '导入', value: 6 },
  { label: '强退', value: 7 },
  { label: '生成代码', value: 8 },
  { label: '清空数据', value: 9 }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  operName: undefined,
  businessType: undefined,
  status: undefined
})

function businessLabel(value) {
  const item = businessOptions.find((option) => option.value === value)
  return item ? item.label : '其它'
}

function businessTagType(value) {
  if (value === 1) return 'success'
  if (value === 2) return 'primary'
  if (value === 3) return 'danger'
  return 'info'
}

function getList() {
  loading.value = true
  listOperlog(addDateRange(queryParams, dateRange.value))
    .then((res) => {
      logList.value = res.rows || []
      total.value = res.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.title = undefined
  queryParams.operName = undefined
  queryParams.businessType = undefined
  queryParams.status = undefined
  dateRange.value = []
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.operId)
}

function handleView(row) {
  detail.value = row
  open.value = true
}

function handleDelete() {
  ElMessageBox.confirm(`是否确认删除日志编号为"${ids.value.join(',')}"的数据项？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delOperlog(ids.value.join(',')))
    .then(() => {
      getList()
      ElMessage.success('删除成功')
    })
}

function handleClean() {
  ElMessageBox.confirm('是否确认清空所有操作日志数据项？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => cleanOperlog())
    .then(() => {
      getList()
      ElMessage.success('清空成功')
    })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.log-content {
  max-height: 160px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #606266;
}
</style>
