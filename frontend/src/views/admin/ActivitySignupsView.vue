<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivitySignups, type ActivitySignupDto } from '../../api/activity'

const loading = ref(false)
const rows = ref<ActivitySignupDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0, activityId: '' })

function statusLabel(status?: ActivitySignupDto['status']) {
  if (status === 'SIGNED') return '已报名'
  if (status === 'CANCELED') return '已取消'
  if (status === 'CHECKED_IN') return '已签到'
  return status || '-'
}

function statusType(status?: ActivitySignupDto['status']) {
  if (status === 'SIGNED') return 'success'
  if (status === 'CHECKED_IN') return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const data = await listActivitySignups(page.current, page.size, page.activityId.trim() || undefined)
    rows.value = data.records ?? []
    page.total = Number(data.total ?? 0)
    page.current = Number(data.current ?? page.current)
    page.size = Number(data.size ?? page.size)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

const emptyText = computed(() => (loading.value ? '' : '暂无报名记录'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-input v-model="page.activityId" placeholder="按活动ID筛选（可选）" style="width: 260px" @keyup.enter="load" />
        <el-button @click="load">查询</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">展示所有活动的报名记录，可按活动ID筛选。</div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%" :empty-text="emptyText">
        <el-table-column prop="id" label="编号" width="160" />
        <el-table-column prop="activityId" label="活动ID" width="160" />
        <el-table-column prop="activityTitle" label="活动标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="160" />
        <el-table-column prop="username" label="用户名" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signedAt" label="报名时间" width="180" />
        <el-table-column prop="canceledAt" label="取消时间" width="180">
          <template #default="{ row }">{{ row.canceledAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkedInAt" label="签到时间" width="180">
          <template #default="{ row }">{{ row.checkedInAt || '-' }}</template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 12px">
        <el-pagination
          layout="total, prev, pager, next, sizes"
          :total="page.total"
          :page-size="page.size"
          :current-page="page.current"
          @current-change="
            (p: number) => {
              page.current = p
              load()
            }
          "
          @size-change="
            (s: number) => {
              page.size = s
              page.current = 1
              load()
            }
          "
        />
      </div>
    </el-card>
  </div>
</template>

