<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { cancelActivitySignup, checkInActivity, listMyActivitySignups, type ActivitySignupDto } from '../../api/activity'

const router = useRouter()

const loading = ref(false)
const rows = ref<ActivitySignupDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listMyActivitySignups(page.current, page.size)
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

function toMs(s?: string) {
  return s ? new Date(s).getTime() : 0
}

const nowMs = () => Date.now()

function canCancel(r: ActivitySignupDto) {
  return r.status === 'SIGNED' && nowMs() < toMs(r.activityStartTime)
}

function canCheckIn(r: ActivitySignupDto) {
  return r.status === 'SIGNED' && nowMs() >= toMs(r.activityStartTime) && nowMs() <= toMs(r.activityEndTime)
}

function goDetail(r: ActivitySignupDto) {
  router.push(`/activities/${r.activityId}`)
}

async function doCancel(r: ActivitySignupDto) {
  try {
    await cancelActivitySignup(r.activityId)
    ElMessage.success('已取消报名')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '取消失败')
  }
}

async function doCheckIn(r: ActivitySignupDto) {
  try {
    await checkInActivity(r.activityId)
    ElMessage.success('签到成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '签到失败')
  }
}

const emptyText = computed(() => (loading.value ? '' : '暂无报名记录'))
</script>

<template>
  <el-card>
    <div style="font-weight: 800">我的报名</div>
  </el-card>

  <el-card style="margin-top: 12px">
    <el-table :data="rows" v-loading="loading" :empty-text="emptyText" style="width: 100%">
      <el-table-column prop="activityTitle" label="活动" min-width="220" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="signedAt" label="报名时间" width="170" />
      <el-table-column prop="activityStartTime" label="开始时间" width="170" />
      <el-table-column prop="activityEndTime" label="结束时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goDetail(row)">详情</el-button>
          <el-button v-if="canCancel(row)" size="small" @click="doCancel(row)">取消</el-button>
          <el-button v-if="canCheckIn(row)" size="small" type="success" @click="doCheckIn(row)">签到</el-button>
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

