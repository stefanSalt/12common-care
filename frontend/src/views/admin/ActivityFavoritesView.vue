<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivityFavorites, type ActivityFavoriteDto } from '../../api/activity'

const loading = ref(false)
const rows = ref<ActivityFavoriteDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0, activityId: '' })

async function load() {
  loading.value = true
  try {
    const data = await listActivityFavorites(page.current, page.size, page.activityId.trim() || undefined)
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

const emptyText = computed(() => (loading.value ? '' : '暂无收藏记录'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-input v-model="page.activityId" placeholder="按活动ID筛选（可选）" style="width: 260px" @keyup.enter="load" />
        <el-button @click="load">查询</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">展示所有活动的收藏记录，可按活动ID筛选。</div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%" :empty-text="emptyText">
        <el-table-column prop="id" label="编号" width="160" />
        <el-table-column prop="activityId" label="活动ID" width="160" />
        <el-table-column prop="activityTitle" label="活动标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="160" />
        <el-table-column prop="username" label="用户名" width="140" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="收藏时间" width="180" />
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

