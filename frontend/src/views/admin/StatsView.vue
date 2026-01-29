<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getActivitySignupRatio, getIncomeTrend } from '../../api/stats'

const DAYS = 7

const incomeLoading = ref(false)
const ratioLoading = ref(false)

const incomeChartEl = ref<HTMLDivElement | null>(null)
const ratioChartEl = ref<HTMLDivElement | null>(null)

let incomeChart: echarts.ECharts | null = null
let ratioChart: echarts.ECharts | null = null

function onChartResize() {
  incomeChart?.resize()
  ratioChart?.resize()
}

async function loadIncomeTrend() {
  incomeLoading.value = true
  try {
    const rows = await getIncomeTrend(DAYS)
    const xData = (rows ?? []).map((r) => r.date)
    const yData = (rows ?? []).map((r) => Number(r.amount ?? 0))

    if (!incomeChartEl.value) return
    if (!incomeChart) {
      incomeChart = echarts.init(incomeChartEl.value)
    }

    incomeChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 24, top: 24, bottom: 40 },
      xAxis: { type: 'category', data: xData },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'line',
          smooth: true,
          data: yData,
          areaStyle: { opacity: 0.12 },
        },
      ],
    })
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    incomeLoading.value = false
  }
}

async function loadSignupRatio() {
  ratioLoading.value = true
  try {
    const rows = await getActivitySignupRatio(DAYS)
    const seriesData = (rows ?? []).map((r) => ({
      name: r.activityTitle || r.activityId,
      value: Number(r.signupCount ?? 0),
    }))

    if (!ratioChartEl.value) return
    if (!ratioChart) {
      ratioChart = echarts.init(ratioChartEl.value)
    }

    ratioChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: seriesData,
          label: { formatter: '{b}: {c}' },
        },
      ],
    })
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    ratioLoading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadIncomeTrend(), loadSignupRatio()])
}

onMounted(async () => {
  window.addEventListener('resize', onChartResize)
  await loadAll()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onChartResize)
  incomeChart?.dispose()
  incomeChart = null
  ratioChart?.dispose()
  ratioChart = null
})
</script>

<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
      <div style="font-weight: 600">数据统计（近 {{ DAYS }} 天）</div>
      <el-button :loading="incomeLoading || ratioLoading" @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
            <div style="font-weight: 600">近 {{ DAYS }} 天收入趋势</div>
            <el-button :loading="incomeLoading" @click="loadIncomeTrend">刷新</el-button>
          </div>
          <div ref="incomeChartEl" v-loading="incomeLoading" style="height: 360px; margin-top: 12px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
            <div style="font-weight: 600">近 {{ DAYS }} 天活动报名量占比</div>
            <el-button :loading="ratioLoading" @click="loadSignupRatio">刷新</el-button>
          </div>
          <div ref="ratioChartEl" v-loading="ratioLoading" style="height: 360px; margin-top: 12px" />
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

