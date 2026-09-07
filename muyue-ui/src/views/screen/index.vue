<template>
  <div class="screen-viewport">
    <div class="screen" :style="screenStyle">
      <!-- 头部 -->
      <header class="screen-header">
        <div class="header-side left">
          <span class="back-btn" @click="goBack"><el-icon><Back /></el-icon> 返回系统</span>
        </div>
        <h1 class="header-title">沐月数据大屏</h1>
        <div class="header-side right">
          <span class="clock">{{ clock }}</span>
        </div>
      </header>

      <!-- KPI -->
      <div class="kpi-row">
        <div v-for="k in kpis" :key="k.label" class="kpi-card">
          <div class="kpi-value">{{ k.value }}</div>
          <div class="kpi-label">{{ k.label }}</div>
        </div>
      </div>

      <!-- 主体三栏 -->
      <div class="screen-body">
        <div class="col left-col">
          <div class="panel">
            <div class="panel-title">分公司人员分布 TOP10</div>
            <div ref="barRef" class="chart chart-bar"></div>
          </div>
          <div class="panel">
            <div class="panel-title">支付方式占比</div>
            <div ref="pieRef" class="chart chart-pie"></div>
          </div>
        </div>

        <div class="col center-col">
          <div class="panel map-panel">
            <div class="panel-title">全国分公司人员分布</div>
            <div ref="mapRef" class="chart chart-map"></div>
          </div>
        </div>

        <div class="col right-col">
          <div class="panel">
            <div class="panel-title">近12个月订单趋势</div>
            <div ref="lineRef" class="chart chart-line"></div>
          </div>
          <div class="panel order-panel">
            <div class="panel-title">最新订单</div>
            <div class="order-list">
              <div v-for="o in recentOrders" :key="o.order_no" class="order-item">
                <span class="order-no">{{ o.order_no }}</span>
                <span class="order-user">{{ o.user_name }}</span>
                <span class="order-amount">￥{{ formatAmount(o.total_amount) }}</span>
                <span class="order-status" :class="'st' + o.status">{{ statusMap[o.status] }}</span>
              </div>
              <div v-if="!recentOrders.length" class="order-empty">暂无数据</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import chinaJson from '@/assets/china.json'
import { branchCoords } from './coords'
import { getScreenStats } from '@/api/screen'

const router = useRouter()

const DESIGN_W = 1920
const DESIGN_H = 1080
const scale = ref(1)
const screenStyle = computed(() => ({
  transform: `translate(-50%, -50%) scale(${scale.value})`,
  transformOrigin: 'center center'
}))

const clock = ref('')
const kpis = ref([])
const recentOrders = ref([])
const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
const payMap = { 0: '支付宝', 1: '微信', 2: '货到付款' }

const barRef = ref()
const pieRef = ref()
const lineRef = ref()
const mapRef = ref()
let chartBar, chartPie, chartLine, chartMap
let clockTimer = null
let refreshTimer = null

const DARK_TEXT = '#9fb3d1'
const LINE_COLORS = ['#4f9dff', '#ffd166']

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fitScreen() {
  scale.value = Math.min(window.innerWidth / DESIGN_W, window.innerHeight / DESIGN_H)
}

function initCharts() {
  echarts.registerMap('china', chinaJson)
  chartBar = echarts.init(barRef.value)
  chartPie = echarts.init(pieRef.value)
  chartLine = echarts.init(lineRef.value)
  chartMap = echarts.init(mapRef.value)

  chartMap.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10, 25, 50, 0.9)',
      borderColor: '#2a4a7a',
      textStyle: { color: '#dbe7ff', fontSize: 12 },
      formatter: (p) => `${p.name}<br/>人员数量：${p.value[2] ?? '-'} 人`
    },
    geo: {
      map: 'china',
      roam: false,
      zoom: 1.15,
      top: 40,
      itemStyle: {
        areaColor: '#12274a',
        borderColor: '#2f5a9e',
        borderWidth: 1
      },
      emphasis: {
        itemStyle: { areaColor: '#1c3d73' },
        label: { color: '#dbe7ff' }
      },
      label: { show: false }
    },
    series: [
      {
        name: '分公司人员',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        zlevel: 2,
        rippleEffect: { brushType: 'stroke', scale: 3 },
        symbolSize: (val) => 6 + Math.min(val[2] / 4, 16),
        itemStyle: { color: '#4fd1ff', shadowBlur: 10, shadowColor: '#4fd1ff' },
        label: {
          show: true,
          position: 'right',
          color: '#cfe3ff',
          fontSize: 11,
          formatter: (p) => p.name.replace('分公司', '')
        },
        data: []
      }
    ]
  })

  chartBar.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 8, right: 30, top: 8, bottom: 0, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(79,157,255,0.12)' } }, axisLabel: { color: DARK_TEXT } },
    yAxis: {
      type: 'category',
      axisLabel: { color: DARK_TEXT, fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(79,157,255,0.3)' } },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      barWidth: 10,
      itemStyle: {
        borderRadius: [0, 5, 5, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#1c6dff' },
          { offset: 1, color: '#4fd1ff' }
        ])
      },
      data: []
    }]
  })

  chartPie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 单（{d}%）' },
    color: ['#4f9dff', '#4fd1ff', '#ffd166', '#ff7a9e'],
    legend: {
      bottom: 0,
      textStyle: { color: DARK_TEXT, fontSize: 11 },
      itemWidth: 12,
      itemHeight: 8
    },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      itemStyle: { borderColor: '#0a1428', borderWidth: 2 },
      label: { color: DARK_TEXT, fontSize: 11, formatter: '{b}\n{d}%' },
      labelLine: { lineStyle: { color: 'rgba(159,179,209,0.4)' } },
      data: []
    }]
  })

  chartLine.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: DARK_TEXT, fontSize: 11 },
      itemWidth: 14,
      itemHeight: 8
    },
    color: LINE_COLORS,
    grid: { left: 8, right: 12, top: 34, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLabel: { color: DARK_TEXT, fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(79,157,255,0.3)' } }
    },
    yAxis: [
      { type: 'value', name: '订单量', nameTextStyle: { color: DARK_TEXT }, axisLabel: { color: DARK_TEXT }, splitLine: { lineStyle: { color: 'rgba(79,157,255,0.12)' } } },
      { type: 'value', name: '销售额(万)', nameTextStyle: { color: DARK_TEXT }, axisLabel: { color: DARK_TEXT }, splitLine: { show: false } }
    ],
    series: [
      {
        name: '订单量',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79,157,255,0.35)' },
            { offset: 1, color: 'rgba(79,157,255,0)' }
          ])
        },
        data: []
      },
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        yAxisIndex: 1,
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,209,102,0.3)' },
            { offset: 1, color: 'rgba(255,209,102,0)' }
          ])
        },
        data: []
      }
    ]
  })
}

async function loadStats() {
  const res = await getScreenStats()
  const data = res.data

  kpis.value = [
    { label: '员工总数', value: data.summary.userCount },
    { label: '分公司数量', value: data.summary.branchCount },
    { label: '订单总数', value: data.summary.orderCount },
    { label: '累计销售额', value: '￥' + formatAmount(data.summary.totalAmount) }
  ]

  recentOrders.value = (data.recentOrders || []).map((o) => ({
    ...o,
    status: String(o.status)
  }))

  // 折线图：近 12 个月
  const trend = (data.trend || []).slice(-12)
  chartLine.setOption({
    xAxis: { data: trend.map((t) => t.ym) },
    series: [
      { data: trend.map((t) => Number(t.cnt)) },
      { data: trend.map((t) => Number((Number(t.amt) / 10000).toFixed(2))) }
    ]
  })

  // 饼图：支付方式
  chartPie.setOption({
    series: [{
      data: (data.payDist || []).map((p) => ({
        name: payMap[String(p.pay_type)] || p.pay_type,
        value: Number(p.cnt)
      }))
    }]
  })

  // 条形图：分公司人数 TOP10
  const branchTop = (data.branchUsers || []).slice(0, 10).reverse()
  chartBar.setOption({
    yAxis: { data: branchTop.map((b) => b.name) },
    series: [{ data: branchTop.map((b) => Number(b.cnt)) }]
  })

  // 地图：分公司人员散点
  const scatter = (data.branchUsers || [])
    .filter((b) => branchCoords[b.name])
    .map((b) => ({
      name: b.name,
      value: [...branchCoords[b.name], Number(b.cnt)]
    }))
  chartMap.setOption({ series: [{ data: scatter }] })
}

function tick() {
  clock.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

function goBack() {
  window.close()
  router.push('/index')
}

onMounted(async () => {
  fitScreen()
  window.addEventListener('resize', fitScreen)
  tick()
  clockTimer = setInterval(tick, 1000)
  initCharts()
  await loadStats()
  refreshTimer = setInterval(loadStats, 60000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', fitScreen)
  clearInterval(clockTimer)
  clearInterval(refreshTimer)
})
</script>

<style scoped>
.screen-viewport {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #050b18;
  overflow: hidden;
  z-index: 2000;
}

.screen {
  position: absolute;
  left: 50%;
  top: 50%;
  display: flex;
  flex-direction: column;
  width: 1920px;
  height: 1080px;
  padding: 0 24px 24px;
  color: #dbe7ff;
  background:
    radial-gradient(ellipse at 50% -10%, rgba(38, 92, 180, 0.35) 0%, transparent 55%),
    radial-gradient(ellipse at 100% 100%, rgba(24, 60, 120, 0.25) 0%, transparent 45%),
    linear-gradient(160deg, #0a1428 0%, #0c1830 50%, #081020 100%);
}

/* 头部 */
.screen-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 84px;
  background: linear-gradient(180deg, rgba(31, 78, 158, 0.35) 0%, transparent 100%);
  border-bottom: 1px solid rgba(79, 157, 255, 0.25);
}

.header-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #6fb4ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(79, 157, 255, 0.45);
}

.header-side {
  display: flex;
  align-items: center;
  width: 260px;
  font-size: 14px;
  color: #9fb3d1;
}

.header-side.right {
  justify-content: flex-end;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #4fd1ff;
}

.clock {
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}

/* KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin: 18px 0;
}

.kpi-card {
  position: relative;
  padding: 18px 24px;
  background: linear-gradient(135deg, rgba(31, 78, 158, 0.28) 0%, rgba(20, 45, 90, 0.18) 100%);
  border: 1px solid rgba(79, 157, 255, 0.22);
  border-radius: 10px;
  overflow: hidden;
}

.kpi-card::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #4fd1ff, #1c6dff);
}

.kpi-value {
  font-size: 34px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: #ffffff;
  text-shadow: 0 0 18px rgba(79, 209, 255, 0.5);
}

.kpi-label {
  margin-top: 4px;
  font-size: 14px;
  color: #9fb3d1;
}

/* 主体三栏 */
.screen-body {
  display: flex;
  flex: 1;
  gap: 20px;
  min-height: 0;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.left-col,
.right-col {
  width: 470px;
}

.center-col {
  flex: 1;
}

.panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  padding: 16px;
  background: rgba(16, 36, 72, 0.35);
  border: 1px solid rgba(79, 157, 255, 0.18);
  border-radius: 10px;
}

.map-panel {
  flex: 1;
}

.panel-title {
  position: relative;
  margin-bottom: 10px;
  padding-left: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #e6efff;
}

.panel-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, #4fd1ff, #1c6dff);
}

.chart {
  flex: 1;
  min-height: 0;
}

/* 最新订单 */
.order-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  font-size: 12px;
  border-radius: 6px;
  background: rgba(31, 78, 158, 0.12);
}

.order-no {
  flex: 1.3;
  color: #cfe3ff;
  font-variant-numeric: tabular-nums;
}

.order-user {
  flex: 0.7;
  color: #9fb3d1;
}

.order-amount {
  flex: 1;
  text-align: right;
  color: #ffd166;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.order-status {
  flex: 0.6;
  text-align: center;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
}

.st0 { color: #ffd166; background: rgba(255, 209, 102, 0.12); }
.st1 { color: #4f9dff; background: rgba(79, 157, 255, 0.12); }
.st2 { color: #4fd1ff; background: rgba(79, 209, 255, 0.12); }
.st3 { color: #52e6a5; background: rgba(82, 230, 165, 0.12); }
.st4 { color: #8b95a7; background: rgba(139, 149, 167, 0.12); }

.order-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #5b6c8f;
  font-size: 13px;
}

/* 滚动条 */
.order-list::-webkit-scrollbar {
  width: 4px;
}

.order-list::-webkit-scrollbar-thumb {
  background: rgba(79, 157, 255, 0.3);
  border-radius: 4px;
}
</style>
