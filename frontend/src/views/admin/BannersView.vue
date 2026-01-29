<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { UploadProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { BannerDto } from '../../api/banner'
import { createBanner, deleteBanner, listAllBanners, updateBanner } from '../../api/banner'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) headers.Authorization = `Bearer ${userStore.token}`
  return headers
})

const loading = ref(false)
const saving = ref(false)
const rows = ref<BannerDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

function imageUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

async function load() {
  loading.value = true
  try {
    const data = await listAllBanners(page.current, page.size)
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

const dialogOpen = ref(false)
const mode = ref<'create' | 'edit'>('create')
const editingId = ref('')

const form = reactive({
  title: '',
  imageFileId: '',
  linkUrl: '',
  sortNo: 0,
  enabled: 1,
})

function openCreate() {
  mode.value = 'create'
  editingId.value = ''
  form.title = ''
  form.imageFileId = ''
  form.linkUrl = ''
  form.sortNo = 0
  form.enabled = 1
  dialogOpen.value = true
}

function openEdit(row: BannerDto) {
  mode.value = 'edit'
  editingId.value = row.id
  form.title = row.title ?? ''
  form.imageFileId = row.imageFileId ?? ''
  form.linkUrl = row.linkUrl ?? ''
  form.sortNo = Number(row.sortNo ?? 0)
  form.enabled = Number(row.enabled ?? 0)
  dialogOpen.value = true
}

const onImageUploadSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response && response.code === 0 && response.data) {
    form.imageFileId = response.data.id
    ElMessage.success('图片已上传')
    return
  }
  ElMessage.error(response?.message ?? '上传失败')
}

const onImageUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

async function submit() {
  if (!form.imageFileId) return ElMessage.warning('请先上传轮播图图片（PUBLIC）')

  const payload = {
    title: form.title.trim() || undefined,
    imageFileId: form.imageFileId,
    linkUrl: form.linkUrl.trim() || undefined,
    sortNo: Number.isFinite(form.sortNo) ? form.sortNo : 0,
    enabled: form.enabled,
  }

  saving.value = true
  try {
    if (mode.value === 'create') {
      await createBanner(payload)
    } else {
      await updateBanner(editingId.value, payload)
    }
    ElMessage.success('保存成功')
    dialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row: BannerDto) {
  try {
    await ElMessageBox.confirm(`确认删除轮播图「${row.title || row.id}」？`, '提示', { type: 'warning' })
    saving.value = true
    await deleteBanner(row.id)
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
        <el-button v-permission="'banner:manage'" type="primary" @click="openCreate">新增轮播图</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
      <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px">
        轮播图图片将作为 PUBLIC 文件对外展示（未登录也可访问）。
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="编号" width="200" />
        <el-table-column label="图片" width="160">
          <template #default="{ row }">
            <img
              :src="imageUrl(row.imageFileId)"
              alt="banner"
              style="width: 140px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid #e5e7eb"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="linkUrl" label="链接" min-width="200" />
        <el-table-column prop="sortNo" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'banner:manage'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'banner:manage'" size="small" type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogOpen" :title="mode === 'create' ? '新增轮播图' : '编辑轮播图'" width="560px">
      <el-form label-position="top">
        <el-form-item label="标题（可选）">
          <el-input v-model="form.title" autocomplete="off" />
        </el-form-item>

        <el-form-item label="图片（必须，PUBLIC）">
          <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
            <el-upload
              action="/api/files/upload?visibility=PUBLIC"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onImageUploadSuccess"
              :on-error="onImageUploadError"
            >
              <el-button type="primary" plain>上传图片</el-button>
            </el-upload>

            <div v-if="form.imageFileId" style="display: flex; align-items: center; gap: 10px">
              <img
                :src="imageUrl(form.imageFileId)"
                alt="preview"
                style="width: 160px; height: 80px; object-fit: cover; border-radius: 10px; border: 1px solid #e5e7eb"
              />
              <div style="font-size: 12px; color: var(--el-text-color-secondary)">fileId: {{ form.imageFileId }}</div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="点击跳转链接（可选）">
          <el-input v-model="form.linkUrl" placeholder="https://..." autocomplete="off" />
        </el-form-item>

        <el-form-item label="排序（越大越靠前）">
          <el-input-number v-model="form.sortNo" :min="-999999" :max="999999" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="form.enabled" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

