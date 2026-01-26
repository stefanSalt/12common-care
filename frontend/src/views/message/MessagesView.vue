<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createMessage, listMyMessages, type MessageDto } from '../../api/message'

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  title: '',
  contactEmail: '',
  content: '',
})

const requiredTrim = (message: string) => {
  return (_rule: any, value: string, callback: (error?: Error) => void) => {
    if (!value || value.trim() === '') return callback(new Error(message))
    callback()
  }
}

const emailTrim = () => {
  return (_rule: any, value: string, callback: (error?: Error) => void) => {
    const v = (value || '').trim()
    if (!v) return callback(new Error('请输入联系邮箱'))
    const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
    if (!ok) return callback(new Error('邮箱格式不正确'))
    callback()
  }
}

const rules: FormRules = {
  title: [{ validator: requiredTrim('请输入标题'), trigger: 'blur' }],
  contactEmail: [{ validator: emailTrim(), trigger: 'blur' }],
  content: [{ validator: requiredTrim('请输入留言内容'), trigger: 'blur' }],
}

const loading = ref(false)
const rows = ref<MessageDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function formatDateTime(value?: string | null) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

async function load() {
  loading.value = true
  try {
    const data = await listMyMessages(page.current, page.size)
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

async function onSubmit() {
  if (!formRef.value) return

  const valid = await new Promise<boolean>((resolve) => {
    formRef.value!.validate((ok) => resolve(ok))
  })
  if (!valid) return

  submitting.value = true
  try {
    await createMessage({
      title: form.title.trim(),
      contactEmail: form.contactEmail.trim(),
      content: form.content.trim(),
    })
    ElMessage.success('已提交')
    form.title = ''
    form.contactEmail = ''
    form.content = ''
    page.current = 1
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div style="max-width: 980px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <template #header>
        <div>提交留言</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="80" show-word-limit autocomplete="off" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" autocomplete="email" />
        </el-form-item>
        <el-form-item label="留言内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <div style="display: flex; gap: 8px">
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交</el-button>
          <el-button :disabled="loading" @click="load">刷新列表</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div>我的留言</div>
      </template>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已回复' : '未回复' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactEmail" label="联系邮箱" width="220" show-overflow-tooltip />
        <el-table-column prop="content" label="留言内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="回复内容" min-width="220">
          <template #default="{ row }">
            <div style="white-space: pre-wrap; color: var(--el-text-color-regular)">
              {{ row.replyContent || '—' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="repliedAt" label="回复时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.repliedAt) || '—' }}</template>
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

