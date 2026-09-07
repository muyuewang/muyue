<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="search-form">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="买家" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入买家名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="收货人" prop="receiver">
        <el-input v-model="queryParams.receiver" placeholder="请输入收货人" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable style="width: 120px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式" prop="payType">
        <el-select v-model="queryParams.payType" placeholder="支付方式" clearable style="width: 120px">
          <el-option v-for="item in payOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" v-hasPermi="['order:order:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" v-hasPermi="['order:order:remove']" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table :data="orderList" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="订单编号" width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleDetail(row)">{{ row.orderNo }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="买家" prop="userName" width="100" />
      <el-table-column label="订单总额" prop="totalAmount" width="110" align="right">
        <template #default="{ row }">￥{{ formatAmount(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'">{{ statusMap[row.status]?.label || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100" align="center">
        <template #default="{ row }">{{ payMap[row.payType] || row.payType }}</template>
      </el-table-column>
      <el-table-column label="收货人" prop="receiver" width="100" />
      <el-table-column label="联系电话" prop="phone" width="130" />
      <el-table-column label="收货地址" prop="address" min-width="150" show-overflow-tooltip />
      <el-table-column label="下单时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="170" align="center" fixed="right" class-name="op-column">
        <template #default="{ row, $index }">
          <el-button link type="primary" :icon="View" v-hasPermi="['order:order:query']" @click="handleDetail(row)">明细</el-button>
          <el-divider direction="vertical" />
          <el-dropdown :ref="el => setMoreRef(el, $index)" trigger="click" @command="(cmd) => handleAction(cmd, row, $index)">
            <el-button link type="primary" class="op-more-trigger">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit" icon="Edit" v-hasPermi="['order:order:edit']">修改订单</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" class="danger-item" v-hasPermi="['order:order:remove']">删除订单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 订单明细对话框 -->
    <el-dialog title="订单明细" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border v-if="detailOrder">
        <el-descriptions-item label="订单编号">{{ detailOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ detailOrder.userName }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusMap[detailOrder.status]?.type || 'info'">{{ statusMap[detailOrder.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ payMap[detailOrder.payType] }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailOrder.receiver }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailOrder.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detailOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailOrder?.items || []" border class="detail-table">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="商品名称" prop="productName" show-overflow-tooltip />
        <el-table-column label="单价" width="120" align="right">
          <template #default="{ row }">￥{{ formatAmount(row.price) }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="小计" width="130" align="right">
          <template #default="{ row }">￥{{ formatAmount(row.totalPrice) }}</template>
        </el-table-column>
      </el-table>
      <div class="detail-total">
        订单总额：<span class="total-amount">￥{{ formatAmount(detailOrder?.totalAmount) }}</span>
      </div>
    </el-dialog>

    <!-- 新增/修改订单 -->
    <el-dialog :title="title" v-model="open" width="820px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单编号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="买家" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入买家名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付方式" prop="payType">
              <el-select v-model="form.payType" style="width: 100%">
                <el-option v-for="item in payOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiver">
              <el-input v-model="form.receiver" placeholder="请输入收货人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="收货地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入收货地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">订单明细</el-divider>
        <el-table :data="form.items" border>
          <el-table-column label="商品名称" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.productName" placeholder="商品名称" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" controls-position="right" style="width: 110px" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" controls-position="right" style="width: 100px" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="110" align="right">
            <template #default="{ row }">￥{{ formatAmount((row.price || 0) * (row.quantity || 0)) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" :icon="Delete" @click="form.items.splice($index, 1)" />
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-total">
          <el-button type="primary" plain :icon="Plus" size="small" @click="addItem">添加商品</el-button>
          <span style="margin-left: 16px">合计：<span class="total-amount">￥{{ formatAmount(formTotal) }}</span></span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'Order' })
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, View } from '@element-plus/icons-vue'
import { listOrder, getOrder, addOrder, updateOrder, delOrder } from '@/api/order/order'

const statusOptions = [
  { value: '0', label: '待付款', type: 'warning' },
  { value: '1', label: '已付款', type: 'primary' },
  { value: '2', label: '已发货', type: 'primary' },
  { value: '3', label: '已完成', type: 'success' },
  { value: '4', label: '已取消', type: 'info' }
]
const payOptions = [
  { value: '0', label: '支付宝' },
  { value: '1', label: '微信' },
  { value: '2', label: '货到付款' }
]
const statusMap = Object.fromEntries(statusOptions.map((s) => [s.value, s]))
const payMap = Object.fromEntries(payOptions.map((s) => [s.value, s.label]))

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const open = ref(false)
const detailOpen = ref(false)
const detailOrder = ref(null)
const title = ref('')
const selectedIds = ref([])
const queryRef = ref()
const formRef = ref()

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderNo: '', userName: '', receiver: '', status: '', payType: '' })
const defaultForm = {
  orderId: undefined, orderNo: '', userName: '', status: '0', payType: '0',
  receiver: '', phone: '', address: '', remark: '',
  items: []
}
const form = reactive(JSON.parse(JSON.stringify(defaultForm)))
const rules = {
  userName: [{ required: true, message: '买家名称不能为空', trigger: 'blur' }]
}

const formTotal = computed(() =>
  (form.items || []).reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
)

function formatAmount(value) {
  const num = Number(value || 0)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  listOrder(queryParams).then((res) => {
    orderList.value = res.rows
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
  selectedIds.value = rows.map((r) => r.orderId)
}
function addItem() {
  form.items.push({ productName: '', price: 0, quantity: 1 })
}
function reset() {
  Object.assign(form, JSON.parse(JSON.stringify(defaultForm)))
}
function handleAdd() {
  reset()
  addItem()
  open.value = true
  title.value = '新增订单'
}
function handleUpdate(row) {
  reset()
  getOrder(row.orderId).then((res) => {
    Object.assign(form, res.data)
    if (!form.items || !form.items.length) addItem()
    open.value = true
    title.value = '修改订单'
  })
}
function handleDetail(row) {
  getOrder(row.orderId).then((res) => {
    detailOrder.value = res.data
    detailOpen.value = true
  })
}
function cancel() {
  open.value = false
}
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (!form.items.length || form.items.some((i) => !i.productName)) {
      ElMessage.warning('请完善订单明细（商品名称不能为空）')
      return
    }
    const action = form.orderId ? updateOrder(form) : addOrder(form)
    action.then(() => {
      ElMessage.success(form.orderId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}
function handleDelete(row) {
  const ids = row.orderId ? [row.orderId] : selectedIds.value
  ElMessageBox.confirm('是否确认删除订单编号为"' + ids.join(',') + '"的数据项？', '提示', {
    type: 'warning'
  }).then(() => delOrder(ids.join(','))).then(() => {
    ElMessage.success('删除成功')
    getList()
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
.detail-table {
  margin-top: 14px;
}
.detail-total {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 12px;
  font-size: 14px;
  color: var(--text-regular);
}
.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--muyue-danger);
}
</style>
