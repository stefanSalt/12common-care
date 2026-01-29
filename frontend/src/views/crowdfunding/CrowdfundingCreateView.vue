<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { UploadProps } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createCrowdfundingProject,
  getMyCrowdfundingProjectDetail,
  updateCrowdfundingProject,
  type CrowdfundingProjectDetailDto,
} from '../../api/crowdfunding'
import RichTextEditor from '../../components/RichTextEditor.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) headers.Authorization = `Bearer ${userStore.token}`
  return headers
})

const projectId = computed(() => (typeof route.params.id === 'string' ? route.params.id : ''))
const isEdit = computed(() => !!projectId.value)

const loading = ref(false)
const saving = ref(false)
const readonly = ref(false)
const statusText = ref('')

const form = reactive({
  title: '',
  coverFileId: '' as any,
  targetAmount: 0,
  endTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
  content: '<p></p>',
})

function pad2(n: number) {
  return String(n).padStart(2, '0')
}

function toLocalDateTimeString(d: Date) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(
    d.getSeconds(),
  )}`
}

function coverUrl(fileId?: string | number | null) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function fill(detail: CrowdfundingProjectDetailDto) {
  form.title = detail.title ?? ''
  form.coverFileId = detail.coverFileId ?? ''
  form.targetAmount = Number(detail.targetAmount ?? 0) || 0
  form.endTime = detail.endTime ? new Date(detail.endTime) : new Date()
  form.content = detail.content ?? '<p></p>'

  readonly.value = detail.status === 'APPROVED'
  statusText.value = detail.status === 'APPROVED' ? '已通过' : detail.status === 'REJECTED' ? '已驳回' : '待审核'
}

async function load() {
  if (!isEdit.value) return

  loading.value = true
  try {
    const detail = await getMyCrowdfundingProjectDetail(projectId.value)
    fill(detail)
    if (readonly.value) {
      ElMessage.warning('项目已通过审核，禁止修改')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
    router.push('/my/crowdfunding-projects')
  } finally {
    loading.value = false
  }
}

onMounted(load)

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
  if (saving.value) return
  if (isEdit.value && readonly.value) return ElMessage.warning('项目已通过审核，禁止修改')
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.coverFileId) return ElMessage.warning('请先上传封面图（PUBLIC）')
  if (!Number.isFinite(form.targetAmount) || form.targetAmount <= 0) return ElMessage.warning('目标金额必须大于 0')
  if (!form.endTime) return ElMessage.warning('请选择截止时间')

  const payload = {
    title: form.title.trim(),
    coverFileId: form.coverFileId,
    content: form.content || '<p></p>',
    targetAmount: form.targetAmount,
    endTime: toLocalDateTimeString(form.endTime),
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateCrowdfundingProject(projectId.value, payload)
      ElMessage.success('保存成功')
    } else {
      await createCrowdfundingProject(payload)
      ElMessage.success('已提交审核')
    }
    router.push('/my/crowdfunding-projects')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/my/crowdfunding-projects')
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap">
        <div>
          <div style="font-weight: 800">{{ isEdit ? '编辑众筹项目' : '发起爱心众筹' }}</div>
          <div v-if="isEdit" style="margin-top: 6px; color: var(--el-text-color-secondary); font-size: 12px">
            当前状态：{{ statusText || '-' }}
          </div>
        </div>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" :loading="saving" @click="submit">
            {{ isEdit ? '保存修改' : '提交审核' }}
          </el-button>
        </div>
      </div>

      <div style="margin-top: 10px; color: var(--el-text-color-secondary); font-size: 12px">
        封面图与富文本图片将作为 PUBLIC 文件对外展示；提交后需管理员审核通过才会出现在前台列表。
      </div>
    </el-card>

    <el-card v-loading="loading">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" :disabled="readonly" autocomplete="off" />
        </el-form-item>

        <el-form-item label="封面图（必须，PUBLIC）">
          <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
            <el-upload
              v-if="!readonly"
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
                style="width: 180px; height: 100px; object-fit: cover; border-radius: 10px; border: 1px solid #e5e7eb"
              />
              <div style="font-size: 12px; color: var(--el-text-color-secondary)">fileId: {{ form.coverFileId }}</div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="目标金额">
          <el-input-number v-model="form.targetAmount" :min="0" :max="999999999" :disabled="readonly" />
        </el-form-item>

        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            style="width: 100%"
            :disabled="readonly"
            placeholder="选择截止时间"
          />
        </el-form-item>

        <el-form-item label="内容（富文本）">
          <RichTextEditor v-if="!readonly" v-model="form.content" height="360px" />
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
    </el-card>
  </div>
</template>

