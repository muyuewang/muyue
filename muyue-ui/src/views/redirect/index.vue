<template>
  <div />
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const { params, query } = route
const path = params.path ? '/' + params.path : '/'

router.replace({ path, query })
</script>
