<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { NotificationDto } from '../../api/notification'
import { announce, listNotifications, markRead } from '../../api/notification'

const loading = ref(false)
const saving = ref(false)
const rows = ref<NotificationDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listNotifications(page.current, page.size)
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

async function onMarkRead(row: NotificationDto) {
  if (row.isRead === 1) return
  saving.value = true
  try {
    await markRead(row.id)
    ElMessage.success('已标记已读')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  } finally {
    saving.value = false
  }
}

const announceDialogOpen = ref(false)
const announceForm = reactive({ title: '', content: '' })

function openAnnounce() {
  announceForm.title = ''
  announceForm.content = ''
  announceDialogOpen.value = true
}

async function submitAnnounce() {
  if (!announceForm.title.trim()) return ElMessage.warning('请输入标题')
  if (!announceForm.content.trim()) return ElMessage.warning('请输入内容')

  saving.value = true
  try {
    await announce({ title: announceForm.title.trim(), content: announceForm.content })
    ElMessage.success('已发布')
    announceDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-button v-permission="'notification:announce'" type="primary" @click="openAnnounce">发布公告</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        WebSocket 已按 4B 策略：登录成功即建立连接，收到通知会弹窗提示（init + notification）。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="title" label="标题" width="240" />
        <el-table-column prop="type" label="类型" width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'warning'">{{ row.isRead === 1 ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="200" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.isRead === 1" :loading="saving" @click="onMarkRead(row)">
              标记已读
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

    <el-dialog v-model="announceDialogOpen" title="发布公告" width="520px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="announceForm.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="announceForm.content" type="textarea" :rows="5" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="announceDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAnnounce">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
