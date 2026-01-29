<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { listMyCrowdfundingProjects, submitCrowdfundingProject, type CrowdfundingProjectDto } from '../../api/crowdfunding'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const rows = ref<CrowdfundingProjectDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function coverUrl(fileId?: string | number | null) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function statusTagType(status?: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function statusText(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审核'
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
    const data = await listMyCrowdfundingProjects(page.current, page.size)
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

function goCreate() {
  router.push('/crowdfunding/new')
}

function goEdit(row: CrowdfundingProjectDto) {
  router.push(`/crowdfunding/edit/${row.id}`)
}

function goPublicDetail(row: CrowdfundingProjectDto) {
  router.push(`/crowdfunding/${row.id}`)
}

async function submitReview(row: CrowdfundingProjectDto) {
  try {
    await ElMessageBox.confirm(`确认提交「${row.title || row.id}」审核？`, '提示', { type: 'warning' })
    saving.value = true
    await submitCrowdfundingProject(row.id)
    ElMessage.success('已提交')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '提交失败')
  } finally {
    saving.value = false
  }
}

const emptyText = computed(() => (loading.value ? '' : '暂无众筹项目'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-button type="primary" @click="goCreate">发起众筹</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        已通过的项目会出现在前台列表；若被管理员禁用，则前台不可见且禁止捐款。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%" :empty-text="emptyText">
        <el-table-column prop="id" label="编号" width="140" />
        <el-table-column label="封面" width="160">
          <template #default="{ row }">
            <img
              :src="coverUrl(row.coverFileId)"
              alt="cover"
              style="width: 140px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid #e5e7eb"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="筹款" min-width="200">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 6px">
              <div style="font-size: 12px; color: var(--el-text-color-secondary)">
                目标：{{ row.targetAmount }}｜已筹：{{ row.raisedAmount }}
              </div>
              <el-progress :percentage="percent(row)" :stroke-width="10" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="截止时间" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :disabled="row.status === 'APPROVED' || saving"
              :loading="saving"
              @click="goEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 'REJECTED' || saving"
              :loading="saving"
              @click="submitReview(row)"
            >
              提交审核
            </el-button>
            <el-button
              size="small"
              :disabled="row.status !== 'APPROVED' || row.enabled !== 1"
              @click="goPublicDetail(row)"
            >
              查看
            </el-button>
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
  </div>
</template>

