<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteMyComment, listMyComments, type CommentDto } from '../../api/comment'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const rows = ref<CommentDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listMyComments(page.current, page.size)
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

function goStory(row: CommentDto) {
  if (!row.targetId) return
  router.push(`/stories/${row.targetId}`)
}

async function remove(row: CommentDto) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
    saving.value = true
    await deleteMyComment(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}

const emptyText = computed(() => (loading.value ? '' : '暂无评论'))
</script>

<template>
  <el-card>
    <div style="font-weight: 800">我的评论</div>
  </el-card>

  <el-card style="margin-top: 12px">
    <el-table :data="rows" v-loading="loading || saving" :empty-text="emptyText" style="width: 100%">
      <el-table-column prop="storyTitle" label="文章" min-width="220" />
      <el-table-column prop="content" label="评论内容" min-width="260" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goStory(row)">查看文章</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
</template>

