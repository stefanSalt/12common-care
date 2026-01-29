<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import {
  donateToCrowdfunding,
  getPublicCrowdfundingDetail,
  type CrowdfundingDonationDto,
  type CrowdfundingPublicDetailDto,
} from '../../api/crowdfunding'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => String(route.params.id || ''))

const loading = ref(false)
const detail = ref<CrowdfundingPublicDetailDto | null>(null)

const donateDialogOpen = ref(false)
const donateForm = reactive({ amount: 10, anonymous: false, remark: '' })
const donating = ref(false)

function coverUrl(fileId?: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function requireLogin() {
  if (userStore.token) return true
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    detail.value = await getPublicCrowdfundingDetail(id.value)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => id.value,
  () => load(),
)

onMounted(load)

const project = computed(() => detail.value?.project)

const targetAmount = computed(() => Number(project.value?.targetAmount ?? 0))
const raisedAmount = computed(() => Number(project.value?.raisedAmount ?? 0))
const percent = computed(() => {
  if (!targetAmount.value || targetAmount.value <= 0) return 0
  return Math.min(100, Math.round((raisedAmount.value / targetAmount.value) * 100))
})

const ended = computed(() => {
  const endTime = project.value?.endTime
  if (!endTime) return false
  const ms = new Date(endTime).getTime()
  return Number.isFinite(ms) ? Date.now() > ms : false
})

function openDonate() {
  if (!requireLogin()) return
  donateForm.amount = 10
  donateForm.anonymous = false
  donateForm.remark = ''
  donateDialogOpen.value = true
}

async function submitDonate() {
  if (!requireLogin()) return
  if (ended.value) return ElMessage.warning('项目已结束，无法捐款')
  if (!Number.isFinite(donateForm.amount) || donateForm.amount <= 0) return ElMessage.warning('金额必须大于 0')
  donating.value = true
  try {
    await donateToCrowdfunding(id.value, {
      amount: donateForm.amount,
      anonymous: donateForm.anonymous,
      remark: donateForm.remark.trim() || undefined,
    })
    ElMessage.success('捐款成功')
    donateDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '捐款失败')
  } finally {
    donating.value = false
  }
}

function donationRowKey(row: CrowdfundingDonationDto) {
  return row.id
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card v-loading="loading">
      <template v-if="project">
        <div style="display: flex; gap: 14px; flex-wrap: wrap">
          <img
            :src="coverUrl(project.coverFileId)"
            alt="cover"
            style="width: 280px; height: 160px; object-fit: cover; border-radius: 12px; border: 1px solid #e5e7eb"
          />
          <div style="flex: 1; min-width: 240px">
            <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px">
              <div style="font-size: 18px; font-weight: 900; color: #111827">{{ project.title }}</div>
              <el-tag :type="ended ? 'info' : 'success'" effect="light">{{ ended ? '已结束' : '进行中' }}</el-tag>
            </div>

            <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
              截止：{{ project.endTime }}
            </div>

            <div style="margin-top: 10px; font-size: 12px; color: var(--el-text-color-secondary)">
              进度：{{ raisedAmount }} / {{ targetAmount }}
            </div>
            <el-progress v-if="targetAmount > 0" :percentage="percent" :stroke-width="10" style="margin-top: 8px" />

            <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
              <el-button type="warning" :disabled="ended" @click="openDonate">捐款</el-button>
              <el-button @click="router.push('/crowdfunding')">返回列表</el-button>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="content" v-html="project.content" />
      </template>
    </el-card>

    <el-card>
      <div style="font-weight: 800; margin-bottom: 10px">捐款记录（公开）</div>
      <el-table :data="detail?.latestDonations ?? []" :row-key="donationRowKey" style="width: 100%">
        <el-table-column prop="donorName" label="捐款人" width="180" />
        <el-table-column prop="amount" label="金额" width="140" />
        <el-table-column prop="createdAt" label="时间" width="200" />
      </el-table>
    </el-card>
  </div>

  <el-dialog v-model="donateDialogOpen" title="捐款" width="420px">
    <el-form label-position="top">
      <el-form-item label="金额">
        <el-input-number v-model="donateForm.amount" :min="1" :max="999999999" :precision="2" :step="10" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="donateForm.anonymous">匿名捐款</el-checkbox>
      </el-form-item>
      <el-form-item label="备注（可选）">
        <el-input v-model="donateForm.remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="donateDialogOpen = false">取消</el-button>
      <el-button type="primary" :loading="donating" @click="submitDonate">确认捐款</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.content :deep(img) {
  max-width: 100%;
}

.content :deep(p) {
  line-height: 1.8;
}
</style>

