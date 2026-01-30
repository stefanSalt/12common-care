<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listNotifications, markRead, type NotificationDto } from '../../api/notification'

const loading = ref(false)
const savingId = ref('')

const rows = ref<NotificationDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function formatDateTime(value?: string | null) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

function typeLabel(type?: string) {
  const t = (type || '').toUpperCase()
  if (t === 'ANNOUNCEMENT') return '公告'
  if (t === 'BUSINESS') return '业务'
  if (t === 'SYSTEM') return '系统'
  return type || '其它'
}

function typeTag(type?: string) {
  const t = (type || '').toUpperCase()
  if (t === 'ANNOUNCEMENT') return 'info'
  if (t === 'BUSINESS') return 'success'
  if (t === 'SYSTEM') return 'warning'
  return 'info'
}

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

const detailOpen = ref(false)
const viewing = ref<NotificationDto | null>(null)

function openDetail(row: NotificationDto) {
  viewing.value = row
  detailOpen.value = true
}

async function onMarkRead(row: NotificationDto) {
  if (row.isRead === 1) return
  savingId.value = row.id
  try {
    await markRead(row.id)
    row.isRead = 1
    ElMessage.success('已标记已读')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  } finally {
    savingId.value = ''
  }
}

async function markCurrentPageRead() {
  const unread = rows.value.filter((r) => r.isRead !== 1)
  if (unread.length === 0) {
    ElMessage.info('本页暂无未读通知')
    return
  }
  for (const item of unread) {
    savingId.value = item.id
    try {
      await markRead(item.id)
      item.isRead = 1
    } catch (e: any) {
      ElMessage.error(e?.message ?? '标记失败')
      break
    } finally {
      savingId.value = ''
    }
  }
  ElMessage.success('已标记本页已读')
}

const emptyText = computed(() => (loading.value ? '' : '暂无通知'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-button :disabled="loading || savingId !== ''" @click="load">刷新</el-button>
        <el-button :disabled="loading || savingId !== ''" @click="markCurrentPageRead">标记本页已读</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        新通知会通过弹窗实时提示；这里可查看历史通知并标记已读。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%" :empty-text="emptyText">
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" effect="light">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'warning'" effect="light">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
            <el-button
              size="small"
              type="primary"
              plain
              :disabled="row.isRead === 1"
              :loading="savingId === row.id"
              @click="onMarkRead(row)"
            >
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

    <el-dialog v-model="detailOpen" title="通知详情" width="680px">
      <div v-if="viewing" style="display: flex; flex-direction: column; gap: 10px">
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px">
          <div style="font-size: 16px; font-weight: 700; color: #111827">{{ viewing.title }}</div>
          <el-tag :type="viewing.isRead === 1 ? 'success' : 'warning'" effect="light">
            {{ viewing.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </div>
        <div style="font-size: 12px; color: var(--el-text-color-secondary)">
          {{ formatDateTime(viewing.createdAt) }} ｜ {{ typeLabel(viewing.type) }}
        </div>
        <div style="white-space: pre-wrap; line-height: 1.6">{{ viewing.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关闭</el-button>
        <el-button
          v-if="viewing && viewing.isRead !== 1"
          type="primary"
          :loading="savingId === viewing.id"
          @click="onMarkRead(viewing)"
        >
          标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

