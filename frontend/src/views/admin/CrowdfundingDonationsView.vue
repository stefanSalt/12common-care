<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAllCrowdfundingDonations, type CrowdfundingDonationRecordDto } from '../../api/crowdfunding'

const loading = ref(false)
const rows = ref<CrowdfundingDonationRecordDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0, projectId: '' })

function coverUrl(fileId?: string | number | null) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function donorText(row: CrowdfundingDonationRecordDto) {
  if (Number(row.isAnonymous) === 1) return '匿名'
  return row.username || row.userId || '-'
}

async function load() {
  loading.value = true
  try {
    const data = await listAllCrowdfundingDonations(
      page.current,
      page.size,
      page.projectId.trim() ? page.projectId.trim() : undefined,
    )
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

const emptyText = computed(() => (loading.value ? '' : '暂无捐款记录'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-input
          v-model="page.projectId"
          placeholder="按项目ID筛选（可选）"
          style="width: 220px"
          @keyup.enter="load"
        />
        <el-button @click="load">查询</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        展示所有众筹项目的捐款记录（包含匿名）。可按项目ID筛选。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%" :empty-text="emptyText">
        <el-table-column prop="id" label="编号" width="140" />
        <el-table-column prop="projectId" label="项目ID" width="120" />
        <el-table-column label="项目" min-width="220">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 10px">
              <img
                v-if="row.projectCoverFileId"
                :src="coverUrl(row.projectCoverFileId)"
                alt="cover"
                style="width: 56px; height: 32px; object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb"
              />
              <div style="display: flex; flex-direction: column; gap: 2px">
                <div style="font-weight: 700; color: #111827">{{ row.projectTitle || '-' }}</div>
                <div style="font-size: 12px; color: var(--el-text-color-secondary)">projectId: {{ row.projectId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="捐款人" width="160">
          <template #default="{ row }">
            <div>{{ donorText(row) }}</div>
            <div v-if="Number(row.isAnonymous) === 1" style="font-size: 12px; color: var(--el-text-color-secondary)">匿名</div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column prop="createdAt" label="时间" width="180" />
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

