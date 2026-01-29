<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listPublicCrowdfundingProjects, type CrowdfundingProjectDto } from '../../api/crowdfunding'

const router = useRouter()

const loading = ref(false)
const rows = ref<CrowdfundingProjectDto[]>([])
const page = reactive({ current: 1, size: 12, total: 0 })

function coverUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function isEnded(endTime?: string) {
  if (!endTime) return false
  const ms = new Date(endTime).getTime()
  return Number.isFinite(ms) ? Date.now() > ms : false
}

function percent(item: CrowdfundingProjectDto) {
  const target = Number(item.targetAmount ?? 0)
  const raised = Number(item.raisedAmount ?? 0)
  if (!target || target <= 0) return 0
  return Math.min(100, Math.round((raised / target) * 100))
}

async function load() {
  loading.value = true
  try {
    const data = await listPublicCrowdfundingProjects(page.current, page.size)
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
  router.push(`/crowdfunding/${id}`)
}

const emptyText = computed(() => (loading.value ? '' : '暂无众筹项目'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="font-weight: 700; font-size: 16px">爱心众筹</div>
      <div style="margin-top: 6px; color: var(--el-text-color-secondary); font-size: 12px">
        查看项目详情后可进行捐款（需登录）。支持匿名捐款，捐款进度实时展示。
      </div>
    </el-card>

    <el-card v-loading="loading" :body-style="{ padding: '16px' }">
      <el-empty v-if="!rows.length && !loading" :description="emptyText" />

      <el-row v-else :gutter="12">
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
              <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px">
                <div style="font-weight: 800; color: #111827; line-height: 1.2; flex: 1">{{ item.title }}</div>
                <el-tag :type="isEnded(item.endTime) ? 'info' : 'success'" effect="light">
                  {{ isEnded(item.endTime) ? '已结束' : '进行中' }}
                </el-tag>
              </div>

              <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
                目标：{{ item.targetAmount }}｜已筹：{{ item.raisedAmount }}
              </div>
              <div style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">
                截止：{{ item.endTime }}
              </div>

              <div style="margin-top: 10px">
                <el-progress :percentage="percent(item)" :stroke-width="10" />
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

