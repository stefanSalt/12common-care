<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { listMessages, replyMessage, type MessageDto } from '../../api/message'

const loading = ref(false)
const saving = ref(false)

const rows = ref<MessageDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function formatDateTime(value?: string | null) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

async function load() {
  loading.value = true
  try {
    const data = await listMessages(page.current, page.size)
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

const replyDialogOpen = ref(false)
const replyFormRef = ref<FormInstance>()
const replying = ref<MessageDto | null>(null)
const replyForm = reactive({ replyContent: '' })

const rules: FormRules = {
  replyContent: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!value || value.trim() === '') return callback(new Error('请输入回复内容'))
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function openReply(row: MessageDto) {
  replying.value = row
  replyForm.replyContent = (row.replyContent ?? '').toString()
  replyDialogOpen.value = true
}

async function submitReply() {
  if (!replying.value || !replyFormRef.value) return

  const valid = await new Promise<boolean>((resolve) => {
    replyFormRef.value!.validate((ok) => resolve(ok))
  })
  if (!valid) return

  saving.value = true
  try {
    await replyMessage(replying.value.id, { replyContent: replyForm.replyContent.trim() })
    ElMessage.success('已回复')
    replyDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '回复失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-button @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="编号" width="200" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="140" show-overflow-tooltip />
        <el-table-column prop="contactEmail" label="联系邮箱" width="220" show-overflow-tooltip />
        <el-table-column prop="content" label="留言内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已回复' : '未回复' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="repliedAt" label="回复时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.repliedAt) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'message:reply'" size="small" type="primary" plain @click="openReply(row)">
              {{ row.status === 1 ? '再次回复' : '回复' }}
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

    <el-dialog v-model="replyDialogOpen" title="回复留言（覆盖式保存）" width="620px">
      <div v-if="replying" style="display: grid; grid-template-columns: 110px 1fr; gap: 8px 12px; margin-bottom: 12px">
        <div style="color: var(--el-text-color-secondary)">标题</div>
        <div>{{ replying.title }}</div>
        <div style="color: var(--el-text-color-secondary)">用户</div>
        <div>{{ replying.username || '—' }}</div>
        <div style="color: var(--el-text-color-secondary)">联系邮箱</div>
        <div>{{ replying.contactEmail }}</div>
        <div style="color: var(--el-text-color-secondary)">留言内容</div>
        <div style="white-space: pre-wrap">{{ replying.content }}</div>
      </div>

      <el-form ref="replyFormRef" :model="replyForm" :rules="rules" label-position="top">
        <el-form-item label="回复内容" prop="replyContent">
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReply">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

