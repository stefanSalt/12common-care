<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { listNotifications, type NotificationDto } from '../../api/notification'
import { getActivitySignupRatio, getIncomeTrend } from '../../api/stats'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const DAYS = 7

const canViewStats = computed(() => userStore.permissions.includes('stats:view'))

const loadingAnnouncements = ref(false)
const announcements = ref<NotificationDto[]>([])

const statsLoading = ref(false)
const incomeChartEl = ref<HTMLDivElement | null>(null)
const ratioChartEl = ref<HTMLDivElement | null>(null)
let incomeChart: echarts.ECharts | null = null
let ratioChart: echarts.ECharts | null = null

function onChartResize() {
  incomeChart?.resize()
  ratioChart?.resize()
}

async function loadAnnouncements() {
  loadingAnnouncements.value = true
  try {
    const data = await listNotifications(1, 10)
    const items = data.records ?? []
    announcements.value = items.filter((n) => n.type === 'ANNOUNCEMENT')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '公告加载失败')
  } finally {
    loadingAnnouncements.value = false
  }
}

async function loadStats() {
  if (!canViewStats.value) return
  statsLoading.value = true
  try {
    const [incomeRows, ratioRows] = await Promise.all([getIncomeTrend(DAYS), getActivitySignupRatio(DAYS)])

    // Income trend line.
    if (incomeChartEl.value) {
      if (!incomeChart) incomeChart = echarts.init(incomeChartEl.value)
      incomeChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 48, right: 24, top: 24, bottom: 40 },
        xAxis: { type: 'category', data: (incomeRows ?? []).map((r) => r.date) },
        yAxis: { type: 'value' },
        series: [
          {
            type: 'line',
            smooth: true,
            data: (incomeRows ?? []).map((r) => Number(r.amount ?? 0)),
            areaStyle: { opacity: 0.12 },
          },
        ],
      })
    }

    // Activity signup ratio pie.
    if (ratioChartEl.value) {
      if (!ratioChart) ratioChart = echarts.init(ratioChartEl.value)
      ratioChart.setOption({
        tooltip: { trigger: 'item' },
        legend: { top: 'bottom' },
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: (ratioRows ?? []).map((r) => ({
              name: r.activityTitle || r.activityId,
              value: Number(r.signupCount ?? 0),
            })),
            label: { formatter: '{b}: {c}' },
          },
        ],
      })
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '统计加载失败')
  } finally {
    statsLoading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadAnnouncements(), loadStats()])
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
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap">
        <div style="font-weight: 800">欢迎，{{ userStore.user?.username }}</div>
        <el-button size="small" :loading="loadingAnnouncements || statsLoading" @click="loadAll">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        权限数量：{{ userStore.permissions.length }}
      </div>
    </el-card>

    <el-card v-loading="loadingAnnouncements">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap">
          <div style="font-weight: 700">最新公告</div>
          <div style="font-size: 12px; color: var(--el-text-color-secondary)">ANNOUNCEMENT</div>
        </div>
      </template>

      <div v-if="announcements.length === 0" style="color: var(--el-text-color-secondary)">暂无公告</div>
      <div v-else style="display: flex; flex-direction: column; gap: 10px">
        <el-alert v-for="n in announcements" :key="n.id" type="info" :title="n.title" :closable="false" show-icon>
          <template #default>
            <div style="white-space: pre-wrap; line-height: 1.5">{{ n.content }}</div>
            <div style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">{{ n.createdAt }}</div>
          </template>
        </el-alert>
      </div>
    </el-card>

    <el-card v-if="canViewStats">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap">
          <div style="font-weight: 700">数据统计概览（近 {{ DAYS }} 天）</div>
        </div>
      </template>

      <el-row :gutter="12">
        <el-col :xs="24" :lg="14">
          <el-card shadow="never">
            <div style="font-weight: 600">近 {{ DAYS }} 天收入趋势</div>
            <div ref="incomeChartEl" v-loading="statsLoading" style="height: 360px; margin-top: 12px" />
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="10">
          <el-card shadow="never">
            <div style="font-weight: 600">近 {{ DAYS }} 天活动报名量占比</div>
            <div ref="ratioChartEl" v-loading="statsLoading" style="height: 360px; margin-top: 12px" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>
