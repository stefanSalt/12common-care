<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import type { UploadProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  createActivity,
  deleteActivity,
  getActivityDetail,
  listAllActivities,
  updateActivity,
  type ActivityDto,
} from '../../api/activity'
import { getActivitySignupRatio } from '../../api/stats'
import RichTextEditor from '../../components/RichTextEditor.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) headers.Authorization = `Bearer ${userStore.token}`
  return headers
})

function fileUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function pad2(n: number) {
  return String(n).padStart(2, '0')
}

function toLocalDateTimeString(d: Date) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(
    d.getSeconds(),
  )}`
}

const activeTab = ref<'activities' | 'stats'>('activities')

// -------- Activities tab --------
const activitiesLoading = ref(false)
const activitiesSaving = ref(false)
const activityRows = ref<ActivityDto[]>([])
const activityPage = reactive({ current: 1, size: 10, total: 0 })

async function loadActivities() {
  activitiesLoading.value = true
  try {
    const data = await listAllActivities(activityPage.current, activityPage.size)
    activityRows.value = data.records ?? []
    activityPage.total = Number(data.total ?? 0)
    activityPage.current = Number(data.current ?? activityPage.current)
    activityPage.size = Number(data.size ?? activityPage.size)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    activitiesLoading.value = false
  }
}

onMounted(loadActivities)

const dialogOpen = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref('')

const form = reactive({
  title: '',
  coverFileId: '',
  address: '',
  timeRange: [] as unknown as [Date, Date],
  signupEnabled: 1 as 0 | 1,
  donateEnabled: 1 as 0 | 1,
  maxParticipants: 50,
  donationTarget: 0,
  enabled: 1 as 0 | 1,
  content: '',
})

function resetForm() {
  form.title = ''
  form.coverFileId = ''
  form.address = ''
  form.timeRange = [new Date(), new Date()]
  form.signupEnabled = 1
  form.donateEnabled = 1
  form.maxParticipants = 50
  form.donationTarget = 0
  form.enabled = 1
  form.content = '<p></p>'
}

function openCreate() {
  dialogMode.value = 'create'
  editingId.value = ''
  resetForm()
  dialogOpen.value = true
}

async function openEdit(row: ActivityDto) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  try {
    const detail = await getActivityDetail(row.id)
    form.title = detail.title ?? ''
    form.coverFileId = detail.coverFileId ?? ''
    form.address = detail.address ?? ''
    form.signupEnabled = (detail.signupEnabled ?? 1) as 0 | 1
    form.donateEnabled = (detail.donateEnabled ?? 1) as 0 | 1
    form.maxParticipants = Number(detail.maxParticipants ?? 0) || 0
    form.donationTarget = Number(detail.donationTarget ?? 0) || 0
    form.enabled = (detail.enabled ?? 1) as 0 | 1
    form.content = detail.content ?? '<p></p>'
    form.timeRange = [new Date(detail.startTime), new Date(detail.endTime)]
    dialogOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载详情失败')
  }
}

const onCoverUploadSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response && response.code === 0 && response.data) {
    form.coverFileId = response.data.id
    ElMessage.success('封面已上传')
    return
  }
  ElMessage.error(response?.message ?? '上传失败')
}

const onCoverUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

async function submitActivity() {
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.coverFileId) return ElMessage.warning('请先上传封面图（PUBLIC）')
  if (!form.timeRange?.[0] || !form.timeRange?.[1]) return ElMessage.warning('请选择活动时间')
  if (form.signupEnabled === 1 && (!Number.isFinite(form.maxParticipants) || form.maxParticipants <= 0)) {
    return ElMessage.warning('报名人数上限必须大于 0')
  }

  const payload = {
    title: form.title.trim(),
    coverFileId: form.coverFileId,
    address: form.address.trim() || undefined,
    startTime: toLocalDateTimeString(form.timeRange[0]),
    endTime: toLocalDateTimeString(form.timeRange[1]),
    signupEnabled: form.signupEnabled,
    donateEnabled: form.donateEnabled,
    maxParticipants: form.signupEnabled === 1 ? form.maxParticipants : undefined,
    donationTarget: form.donateEnabled === 1 ? form.donationTarget : undefined,
    enabled: form.enabled,
    content: form.content || '<p></p>',
  }

  activitiesSaving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createActivity(payload)
    } else {
      await updateActivity(editingId.value, payload)
    }
    ElMessage.success('保存成功')
    dialogOpen.value = false
    await loadActivities()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    activitiesSaving.value = false
  }
}

async function removeActivity(row: ActivityDto) {
  try {
    await ElMessageBox.confirm(`确认删除活动「${row.title || row.id}」？`, '提示', { type: 'warning' })
    activitiesSaving.value = true
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    await loadActivities()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    activitiesSaving.value = false
  }
}

// -------- Stats tab --------
const statsLoading = ref(false)
const chartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
const onChartResize = () => chart?.resize()

async function loadStats() {
  statsLoading.value = true
  try {
    const rows = await getActivitySignupRatio(7)
    const seriesData = (rows ?? []).map((r) => ({
      name: r.activityTitle || r.activityId,
      value: Number(r.signupCount ?? 0),
    }))

    if (!chartEl.value) return
    if (!chart) {
      chart = echarts.init(chartEl.value)
      window.addEventListener('resize', onChartResize)
    }

    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { top: 'bottom' },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: seriesData,
          label: { formatter: '{b}: {c}' },
        },
      ],
    })
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    statsLoading.value = false
  }
}

watch(
  () => activeTab.value,
  (tab) => {
    if (tab === 'stats') loadStats()
  },
)

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  window.removeEventListener('resize', onChartResize)
})
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-button v-permission="'activity:manage'" type="primary" @click="openCreate">新增活动</el-button>
        <el-button @click="loadActivities">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        活动封面与富文本图片都会作为 PUBLIC 文件对外展示（未登录也可访问）。
      </div>
    </el-card>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="活动管理" name="activities">
          <el-table :data="activityRows" v-loading="activitiesLoading" style="width: 100%">
            <el-table-column prop="id" label="编号" width="200" />
            <el-table-column label="封面" width="140">
              <template #default="{ row }">
                <img
                  :src="fileUrl(row.coverFileId)"
                  alt="cover"
                  style="width: 120px; height: 64px; object-fit: cover; border-radius: 8px; border: 1px solid #e5e7eb"
                />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="startTime" label="开始时间" width="170" />
            <el-table-column prop="endTime" label="结束时间" width="170" />
            <el-table-column label="报名" width="90">
              <template #default="{ row }">
                <el-tag :type="row.signupEnabled === 1 ? 'success' : 'info'">{{ row.signupEnabled === 1 ? '开启' : '关闭' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="捐赠" width="90">
              <template #default="{ row }">
                <el-tag :type="row.donateEnabled === 1 ? 'success' : 'info'">{{ row.donateEnabled === 1 ? '开启' : '关闭' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="maxParticipants" label="上限" width="90" />
            <el-table-column prop="donatedAmount" label="已捐" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-permission="'activity:manage'" size="small" @click="openEdit(row)">编辑</el-button>
                <el-button v-permission="'activity:manage'" size="small" type="danger" @click="removeActivity(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="display: flex; justify-content: flex-end; margin-top: 12px">
            <el-pagination
              layout="total, prev, pager, next, sizes"
              :total="activityPage.total"
              :page-size="activityPage.size"
              :current-page="activityPage.current"
              @current-change="
                (p: number) => {
                  activityPage.current = p
                  loadActivities()
                }
              "
              @size-change="
                (s: number) => {
                  activityPage.size = s
                  activityPage.current = 1
                  loadActivities()
                }
              "
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-permission="'stats:view'" label="统计" name="stats">
          <el-card>
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
              <div style="font-weight: 600">近 7 天活动报名量占比</div>
              <el-button :loading="statsLoading" @click="loadStats">刷新</el-button>
            </div>
            <div ref="chartEl" v-loading="statsLoading" style="height: 360px; margin-top: 12px" />
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogOpen" :title="dialogMode === 'create' ? '新增活动' : '编辑活动'" width="900px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" autocomplete="off" />
        </el-form-item>

        <el-form-item label="封面图（必须，PUBLIC）">
          <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
            <el-upload
              action="/api/files/upload?visibility=PUBLIC"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onCoverUploadSuccess"
              :on-error="onCoverUploadError"
            >
              <el-button type="primary" plain>上传封面</el-button>
            </el-upload>

            <div v-if="form.coverFileId" style="display: flex; align-items: center; gap: 10px">
              <img
                :src="fileUrl(form.coverFileId)"
                alt="preview"
                style="width: 160px; height: 80px; object-fit: cover; border-radius: 10px; border: 1px solid #e5e7eb"
              />
              <div style="font-size: 12px; color: var(--el-text-color-secondary)">fileId: {{ form.coverFileId }}</div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="地址（可选）">
          <el-input v-model="form.address" autocomplete="off" />
        </el-form-item>

        <el-form-item label="活动时间">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px">
          <el-form-item label="报名开关">
            <el-switch v-model="form.signupEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="捐赠开关">
            <el-switch v-model="form.donateEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px">
          <el-form-item label="报名人数上限" :required="form.signupEnabled === 1">
            <el-input-number v-model="form.maxParticipants" :min="1" :max="999999" :disabled="form.signupEnabled === 0" />
          </el-form-item>
          <el-form-item label="捐赠目标金额（用于进度，可选）">
            <el-input-number
              v-model="form.donationTarget"
              :min="0"
              :max="999999999"
              :precision="2"
              :step="10"
              :disabled="form.donateEnabled === 0"
            />
          </el-form-item>
        </div>

        <el-form-item label="状态">
          <el-select v-model="form.enabled" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>

        <el-form-item label="详情（富文本，支持插图）">
          <RichTextEditor v-model="form.content" height="320px" />
          <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
            插图上传会走 PUBLIC 文件，插入的图片 URL 为 /api/files/&lt;id&gt;/download。
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="activitiesSaving" @click="submitActivity">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
