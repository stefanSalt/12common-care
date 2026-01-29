<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { UploadProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteCrowdfundingProject,
  getCrowdfundingProjectDetail,
  listAllCrowdfundingProjects,
  manageUpdateCrowdfundingProject,
  reviewCrowdfundingProject,
  type CrowdfundingProjectDetailDto,
  type CrowdfundingProjectDto,
} from '../../api/crowdfunding'
import RichTextEditor from '../../components/RichTextEditor.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) headers.Authorization = `Bearer ${userStore.token}`
  return headers
})

const loading = ref(false)
const saving = ref(false)
const rows = ref<CrowdfundingProjectDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function coverUrl(fileId?: string | number | null) {
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

function percent(item: CrowdfundingProjectDto) {
  const target = Number(item.targetAmount ?? 0)
  const raised = Number(item.raisedAmount ?? 0)
  if (!target || target <= 0) return 0
  return Math.min(100, Math.round((raised / target) * 100))
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

async function load() {
  loading.value = true
  try {
    const data = await listAllCrowdfundingProjects(page.current, page.size)
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

async function review(row: CrowdfundingProjectDto, action: 'APPROVE' | 'REJECT') {
  const label = action === 'APPROVE' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${label}「${row.title || row.id}」？`, '提示', { type: 'warning' })
    saving.value = true
    await reviewCrowdfundingProject(row.id, action)
    ElMessage.success('操作成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '操作失败')
  } finally {
    saving.value = false
  }
}

const dialogOpen = ref(false)
const dialogMode = ref<'detail' | 'edit'>('detail')
const dialogLoading = ref(false)
const editingId = ref('')

const form = reactive({
  title: '',
  coverFileId: '' as any,
  targetAmount: 0,
  endTime: new Date(),
  enabled: 1 as 0 | 1,
  content: '<p></p>',
})

function resetForm() {
  form.title = ''
  form.coverFileId = ''
  form.targetAmount = 0
  form.endTime = new Date()
  form.enabled = 1
  form.content = '<p></p>'
}

async function loadDetail(id: string) {
  dialogLoading.value = true
  try {
    const detail = await getCrowdfundingProjectDetail(id)
    fillForm(detail)
    editingId.value = detail.id
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载详情失败')
    dialogOpen.value = false
  } finally {
    dialogLoading.value = false
  }
}

function fillForm(detail: CrowdfundingProjectDetailDto) {
  form.title = detail.title ?? ''
  form.coverFileId = detail.coverFileId ?? ''
  form.targetAmount = Number(detail.targetAmount ?? 0) || 0
  form.endTime = detail.endTime ? new Date(detail.endTime) : new Date()
  form.enabled = (Number(detail.enabled ?? 1) === 1 ? 1 : 0) as 0 | 1
  form.content = detail.content ?? '<p></p>'
}

async function openDetail(row: CrowdfundingProjectDto) {
  dialogMode.value = 'detail'
  resetForm()
  dialogOpen.value = true
  await loadDetail(row.id)
}

async function openEdit(row: CrowdfundingProjectDto) {
  dialogMode.value = 'edit'
  resetForm()
  dialogOpen.value = true
  await loadDetail(row.id)
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

async function submit() {
  if (!editingId.value) return
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.coverFileId) return ElMessage.warning('请先上传封面图（PUBLIC）')
  if (!Number.isFinite(form.targetAmount) || form.targetAmount <= 0) return ElMessage.warning('目标金额必须大于 0')
  if (!form.endTime) return ElMessage.warning('请选择截止时间')

  const payload = {
    title: form.title.trim(),
    coverFileId: form.coverFileId,
    targetAmount: form.targetAmount,
    endTime: toLocalDateTimeString(form.endTime),
    enabled: form.enabled,
    content: form.content || '<p></p>',
  }

  saving.value = true
  try {
    await manageUpdateCrowdfundingProject(editingId.value, payload)
    ElMessage.success('保存成功')
    dialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row: CrowdfundingProjectDto) {
  const next = row.enabled === 1 ? 0 : 1
  const label = next === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${label}「${row.title || row.id}」？`, '提示', { type: 'warning' })
    saving.value = true
    await manageUpdateCrowdfundingProject(row.id, { enabled: next })
    ElMessage.success('操作成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '操作失败')
  } finally {
    saving.value = false
  }
}

async function remove(row: CrowdfundingProjectDto) {
  try {
    await ElMessageBox.confirm(`确认删除众筹项目「${row.title || row.id}」？`, '提示', { type: 'warning' })
    saving.value = true
    await deleteCrowdfundingProject(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
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
        <el-button @click="load">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        公众端仅展示“已通过(APPROVED)”的项目；此处为后台管理端全量列表（含待审核/已驳回）。
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
        <el-table-column prop="createdBy" label="发布人ID" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'crowdfunding:review'"
              size="small"
              type="success"
              :disabled="row.status !== 'PENDING' || saving"
              :loading="saving"
              @click="review(row, 'APPROVE')"
            >
              通过
            </el-button>
            <el-button
              v-permission="'crowdfunding:review'"
              size="small"
              type="danger"
              :disabled="row.status !== 'PENDING' || saving"
              :loading="saving"
              @click="review(row, 'REJECT')"
            >
              驳回
            </el-button>
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-permission="'crowdfunding:manage'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-permission="'crowdfunding:manage'"
              size="small"
              :type="row.enabled === 1 ? 'warning' : 'success'"
              :disabled="saving"
              :loading="saving"
              @click="toggleEnabled(row)"
            >
              {{ row.enabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-permission="'crowdfunding:manage'"
              size="small"
              type="danger"
              :disabled="saving"
              :loading="saving"
              @click="remove(row)"
            >
              删除
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

    <el-dialog
      v-model="dialogOpen"
      :title="dialogMode === 'detail' ? '众筹项目详情' : '编辑众筹项目'"
      width="760px"
      :close-on-click-modal="dialogMode === 'detail'"
    >
      <div v-loading="dialogLoading">
        <el-form label-position="top">
          <el-form-item label="标题">
            <el-input v-model="form.title" :disabled="dialogMode === 'detail'" autocomplete="off" />
          </el-form-item>

          <el-form-item label="封面图（PUBLIC）">
            <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
              <el-upload
                v-if="dialogMode === 'edit'"
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
                  :src="coverUrl(form.coverFileId)"
                  alt="cover"
                  style="width: 160px; height: 90px; object-fit: cover; border-radius: 10px; border: 1px solid #e5e7eb"
                />
                <div style="font-size: 12px; color: var(--el-text-color-secondary)">fileId: {{ form.coverFileId }}</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="目标金额">
            <el-input-number v-model="form.targetAmount" :min="0" :max="999999999" :disabled="dialogMode === 'detail'" />
          </el-form-item>

          <el-form-item label="截止时间">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              style="width: 100%"
              :disabled="dialogMode === 'detail'"
              placeholder="选择截止时间"
            />
          </el-form-item>

          <el-form-item label="启用状态">
            <el-select v-model="form.enabled" style="width: 100%" :disabled="dialogMode === 'detail'">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="禁用" />
            </el-select>
          </el-form-item>

          <el-form-item label="内容">
            <RichTextEditor v-if="dialogMode === 'edit'" v-model="form.content" height="320px" />
            <div
              v-else
              style="
                border: 1px solid rgba(15, 23, 42, 0.12);
                border-radius: 10px;
                padding: 12px;
                background: #fff;
                min-height: 120px;
              "
              v-html="form.content"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogOpen = false">关闭</el-button>
        <el-button v-if="dialogMode === 'edit'" type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
