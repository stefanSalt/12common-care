<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps } from 'element-plus'
import type { FileInfoDto, FileVisibility } from '../../api/file'
import { deleteFile, downloadFileBlob, listMyFiles } from '../../api/file'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const rows = ref<FileInfoDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listMyFiles(page.current, page.size)
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

const visibility = ref<FileVisibility>('PRIVATE')

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) {
    headers.Authorization = `Bearer ${userStore.token}`
  }
  return headers
})

const uploadData = computed(() => ({ visibility: visibility.value }))

const onUploadSuccess: UploadProps['onSuccess'] = async (response: any) => {
  if (response && response.code === 0) {
    ElMessage.success('上传成功')
    await load()
    return
  }
  ElMessage.error(response?.message ?? '上传失败')
}

const onUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

async function onDownload(row: FileInfoDto) {
  saving.value = true
  try {
    const blob = await downloadFileBlob(row.id)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.originalName || 'download'
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '下载失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: FileInfoDto) {
  try {
    await ElMessageBox.confirm(`确认删除文件 ${row.originalName}？`, '提示', { type: 'warning' })
    saving.value = true
    await deleteFile(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-select v-model="visibility" style="width: 140px">
          <el-option value="PRIVATE" label="PRIVATE" />
          <el-option value="PUBLIC" label="PUBLIC" />
        </el-select>

        <el-upload
          action="/api/files/upload"
          :headers="uploadHeaders"
          :data="uploadData"
          :show-file-list="false"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
        >
          <el-button type="primary">上传文件</el-button>
        </el-upload>

        <el-button @click="load">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        上传默认 PRIVATE；PUBLIC 文件可匿名下载（后端放行 /api/files/{id}/download）。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="originalName" label="文件名" />
        <el-table-column prop="size" label="大小(bytes)" width="140" />
        <el-table-column prop="visibility" label="可见性" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :loading="saving" @click="onDownload(row)">下载</el-button>
            <el-button size="small" type="danger" :loading="saving" @click="onDelete(row)">删除</el-button>
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
  </div>
</template>
