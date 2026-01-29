<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listMyActivityFavorites, unfavoriteActivity, type ActivityFavoriteDto } from '../../api/activity'

const router = useRouter()

const loading = ref(false)
const rows = ref<ActivityFavoriteDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listMyActivityFavorites(page.current, page.size)
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

function goDetail(r: ActivityFavoriteDto) {
  router.push(`/activities/${r.activityId}`)
}

async function removeFavorite(r: ActivityFavoriteDto) {
  try {
    await unfavoriteActivity(r.activityId)
    ElMessage.success('已取消收藏')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

const emptyText = computed(() => (loading.value ? '' : '暂无收藏记录'))
</script>

<template>
  <el-card>
    <div style="font-weight: 800">我的收藏</div>
  </el-card>

  <el-card style="margin-top: 12px">
    <el-table :data="rows" v-loading="loading" :empty-text="emptyText" style="width: 100%">
      <el-table-column prop="activityTitle" label="活动" min-width="240" />
      <el-table-column prop="createdAt" label="收藏时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goDetail(row)">详情</el-button>
          <el-button size="small" type="danger" @click="removeFavorite(row)">取消收藏</el-button>
        </template>
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
</template>

