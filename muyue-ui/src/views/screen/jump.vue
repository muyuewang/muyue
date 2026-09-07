<template>
  <div class="screen-jump" />
</template>

<script setup>
// 数据大屏一级菜单：点击后新标签页打开全屏大屏，并返回原页面
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  const { href } = router.resolve('/bigscreen')
  window.open(href, '_blank')
  router.back()
})
</script>
