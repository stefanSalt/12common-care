<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteCommentAdmin, listAllComments, type CommentDto } from '../../api/comment'

const loading = ref(false)
const saving = ref(false)
const rows = ref<CommentDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listAllComments(page.current, page.size)
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

async function remove(row: CommentDto) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
    saving.value = true
    await deleteCommentAdmin(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}

function displayName(row: CommentDto) {
  return row.userNickname || row.username || row.userId
}

const emptyText = computed(() => (loading.value ? '' : '暂无评论'))
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
        <el-button @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading || saving" :empty-text="emptyText" style="width: 100%">
        <el-table-column prop="id" label="编号" width="200" />
        <el-table-column prop="storyTitle" label="文章" min-width="220" />
        <el-table-column prop="content" label="内容" min-width="260" />
        <el-table-column label="用户" width="180">
          <template #default="{ row }">{{ displayName(row) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'comment:manage'" size="small" type="danger" @click="remove(row)">删除</el-button>
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

