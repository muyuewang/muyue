<template>
  <div v-show="!hidden" class="pagination-container">
    <el-pagination
      :background="background"
      :current-page="page"
      :page-size="limit"
      :layout="layout"
      :page-sizes="pageSizes"
      :total="total"
      @update:current-page="handleCurrentChange"
      @update:page-size="handleSizeChange"
    />
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, required: true },
  page: { type: Number, required: true },
  limit: { type: Number, required: true },
  pageSizes: { type: Array, default: () => [10, 20, 30, 50, 100] },
  layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
  background: { type: Boolean, default: true },
  hidden: { type: Boolean, default: false }
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

function handleCurrentChange(val) {
  emit('update:page', val)
  emit('pagination')
}

function handleSizeChange(val) {
  emit('update:limit', val)
  emit('pagination')
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
