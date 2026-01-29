<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import {
  cancelActivitySignup,
  checkInActivity,
  donateToActivity,
  favoriteActivity,
  getMyActivityState,
  getPublicActivityDetail,
  signupActivity,
  unfavoriteActivity,
  type ActivityDetailDto,
  type MyActivityStateDto,
} from '../../api/activity'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => String(route.params.id || ''))

const loading = ref(false)
const detail = ref<ActivityDetailDto | null>(null)
const myState = ref<MyActivityStateDto | null>(null)

const donateDialogOpen = ref(false)
const donateForm = reactive({ amount: 10, remark: '' })
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
    detail.value = await getPublicActivityDetail(id.value)
    if (userStore.token) {
      myState.value = await getMyActivityState(id.value)
    } else {
      myState.value = null
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => [id.value, userStore.token],
  () => load(),
)

onMounted(load)

const startMs = computed(() => (detail.value?.startTime ? new Date(detail.value.startTime).getTime() : 0))
const endMs = computed(() => (detail.value?.endTime ? new Date(detail.value.endTime).getTime() : 0))
const nowMs = () => Date.now()

const canCancel = computed(() => myState.value?.signupStatus === 'SIGNED' && nowMs() < startMs.value)
const canCheckIn = computed(() => myState.value?.signupStatus === 'SIGNED' && nowMs() >= startMs.value && nowMs() <= endMs.value)

const donatedAmount = computed(() => Number(detail.value?.donatedAmount ?? 0))
const donationTarget = computed(() => Number(detail.value?.donationTarget ?? 0))
const donationPercent = computed(() => {
  if (!donationTarget.value || donationTarget.value <= 0) return 0
  return Math.min(100, Math.round((donatedAmount.value / donationTarget.value) * 100))
})

async function doSignup() {
  if (!requireLogin()) return
  try {
    const r = await signupActivity(id.value)
    myState.value = { signupStatus: r.status, favorited: myState.value?.favorited ?? false }
    ElMessage.success('报名成功')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '报名失败')
  }
}

async function doCancel() {
  if (!requireLogin()) return
  try {
    const r = await cancelActivitySignup(id.value)
    myState.value = { signupStatus: r.status, favorited: myState.value?.favorited ?? false }
    ElMessage.success('已取消报名')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '取消失败')
  }
}

async function doCheckIn() {
  if (!requireLogin()) return
  try {
    const r = await checkInActivity(id.value)
    myState.value = { signupStatus: r.status, favorited: myState.value?.favorited ?? false }
    ElMessage.success('签到成功')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '签到失败')
  }
}

async function doToggleFavorite() {
  if (!requireLogin()) return
  try {
    if (myState.value?.favorited) {
      await unfavoriteActivity(id.value)
      myState.value = { signupStatus: myState.value?.signupStatus ?? 'NONE', favorited: false }
      ElMessage.success('已取消收藏')
    } else {
      await favoriteActivity(id.value)
      myState.value = { signupStatus: myState.value?.signupStatus ?? 'NONE', favorited: true }
      ElMessage.success('已收藏')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

function openDonate() {
  if (!requireLogin()) return
  donateForm.amount = 10
  donateForm.remark = ''
  donateDialogOpen.value = true
}

async function submitDonate() {
  if (!requireLogin()) return
  if (!Number.isFinite(donateForm.amount) || donateForm.amount <= 0) return ElMessage.warning('金额必须大于 0')
  donating.value = true
  try {
    await donateToActivity(id.value, { amount: donateForm.amount, remark: donateForm.remark.trim() || undefined })
    ElMessage.success('捐赠成功')
    donateDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '捐赠失败')
  } finally {
    donating.value = false
  }
}
</script>

<template>
  <el-card v-loading="loading">
    <template v-if="detail">
      <div style="display: flex; gap: 14px; flex-wrap: wrap">
        <img
          :src="coverUrl(detail.coverFileId)"
          alt="cover"
          style="width: 280px; height: 160px; object-fit: cover; border-radius: 12px; border: 1px solid #e5e7eb"
        />
        <div style="flex: 1; min-width: 240px">
          <div style="font-size: 18px; font-weight: 800; color: #111827">{{ detail.title }}</div>
          <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
            时间：{{ detail.startTime }} ~ {{ detail.endTime }}
          </div>
          <div v-if="detail.address" style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">
            地址：{{ detail.address }}
          </div>

          <div style="margin-top: 10px; display: flex; gap: 8px; flex-wrap: wrap; align-items: center">
            <el-tag v-if="myState?.signupStatus && myState.signupStatus !== 'NONE'" type="info">报名：{{ myState.signupStatus }}</el-tag>
            <el-tag v-if="myState?.favorited" type="warning">已收藏</el-tag>
          </div>

          <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
            <el-button v-if="detail.signupEnabled === 1 && (!myState || myState.signupStatus === 'NONE' || myState.signupStatus === 'CANCELED')" type="primary" @click="doSignup">
              报名
            </el-button>
            <el-button v-if="detail.signupEnabled === 1 && canCancel" @click="doCancel">取消报名</el-button>
            <el-button v-if="detail.signupEnabled === 1 && canCheckIn" type="success" @click="doCheckIn">签到</el-button>

            <el-button v-if="detail.donateEnabled === 1" type="warning" @click="openDonate">捐赠</el-button>
            <el-button @click="doToggleFavorite">{{ myState?.favorited ? '取消收藏' : '收藏' }}</el-button>
          </div>

          <div v-if="detail.donateEnabled === 1" style="margin-top: 12px">
            <div style="font-size: 12px; color: var(--el-text-color-secondary)">
              捐赠进度：{{ donatedAmount }} / {{ donationTarget || '-' }}
            </div>
            <el-progress v-if="donationTarget > 0" :percentage="donationPercent" :stroke-width="10" />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="content" v-html="detail.content" />
    </template>
  </el-card>

  <el-dialog v-model="donateDialogOpen" title="捐赠" width="420px">
    <el-form label-position="top">
      <el-form-item label="金额">
        <el-input-number v-model="donateForm.amount" :min="1" :max="999999999" :precision="2" :step="10" />
      </el-form-item>
      <el-form-item label="备注（可选）">
        <el-input v-model="donateForm.remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="donateDialogOpen = false">取消</el-button>
      <el-button type="primary" :loading="donating" @click="submitDonate">确认捐赠</el-button>
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

