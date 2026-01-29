<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listPublicActivities, type ActivityDto } from '../../api/activity'

const router = useRouter()

const loading = ref(false)
const rows = ref<ActivityDto[]>([])
const page = reactive({ current: 1, size: 12, total: 0 })

function coverUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

async function load() {
  loading.value = true
  try {
    const data = await listPublicActivities(page.current, page.size)
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

function goDetail(id: string) {
  router.push(`/activities/${id}`)
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="font-weight: 700; font-size: 16px">公益活动</div>
      <div style="margin-top: 6px; color: var(--el-text-color-secondary); font-size: 12px">
        浏览活动详情后，可报名 / 签到 / 捐赠 / 收藏（需登录）。
      </div>
    </el-card>

    <el-card v-loading="loading">
      <el-row :gutter="12">
        <el-col v-for="item in rows" :key="item.id" :xs="24" :sm="12" :md="8">
          <div
            style="
              border: 1px solid rgba(15, 23, 42, 0.08);
              border-radius: 12px;
              overflow: hidden;
              background: #fff;
              cursor: pointer;
              margin-bottom: 12px;
            "
            @click="goDetail(item.id)"
          >
            <img :src="coverUrl(item.coverFileId)" alt="cover" style="width: 100%; height: 150px; object-fit: cover" />
            <div style="padding: 10px 12px">
              <div style="font-weight: 700; color: #111827; line-height: 1.2">{{ item.title }}</div>
              <div style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">
                {{ item.startTime }} ~ {{ item.endTime }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div style="display: flex; justify-content: flex-end; margin-top: 8px">
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

