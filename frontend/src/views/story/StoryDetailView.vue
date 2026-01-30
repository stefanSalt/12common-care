<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPublicStoryDetail, type StoryDetailDto } from '../../api/story'
import { createComment, listPublicComments, type CommentDto } from '../../api/comment'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => String(route.params.id || ''))

const loading = ref(false)
const detail = ref<StoryDetailDto | null>(null)

const commentLoading = ref(false)
const commentRows = ref<CommentDto[]>([])
const commentPage = reactive({ current: 1, size: 10, total: 0 })

const commentForm = reactive({ content: '' })
const posting = ref(false)

function coverUrl(fileId?: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function requireLogin() {
  if (userStore.token) return true
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

async function loadStory() {
  if (!id.value) return
  loading.value = true
  try {
    detail.value = await getPublicStoryDetail(id.value)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  if (!id.value) return
  commentLoading.value = true
  try {
    const data = await listPublicComments('STORY', id.value, commentPage.current, commentPage.size)
    commentRows.value = data.records ?? []
    commentPage.total = Number(data.total ?? 0)
    commentPage.current = Number(data.current ?? commentPage.current)
    commentPage.size = Number(data.size ?? commentPage.size)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    commentLoading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadStory(), loadComments()])
}

watch(
  () => id.value,
  () => {
    commentPage.current = 1
    loadAll()
  },
)

onMounted(loadAll)

const commentEmptyText = computed(() => (commentLoading.value ? '' : '暂无评论'))

function displayName(row: CommentDto) {
  return row.userNickname || row.username || '用户'
}

async function submitComment() {
  if (!requireLogin()) return
  const content = commentForm.content.trim()
  if (!content) return ElMessage.warning('请输入评论内容')

  posting.value = true
  try {
    await createComment({ targetType: 'STORY', targetId: id.value, content })
    ElMessage.success('评论成功')
    commentForm.content = ''
    commentPage.current = 1
    await loadComments()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '评论失败')
  } finally {
    posting.value = false
  }
}
</script>

<template>
  <el-card v-loading="loading">
    <template v-if="detail">
      <div style="display: flex; gap: 14px; flex-wrap: wrap">
        <img
          :src="coverUrl(detail.coverFileId)"
          alt="cover"
          style="width: 280px; height: 160px; object-fit: cover; border-radius: 12px; border: 1px solid #e5e7eb"
        />
        <div style="flex: 1; min-width: 240px">
          <div style="font-size: 18px; font-weight: 800; color: #111827">{{ detail.title }}</div>
          <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">发布时间：{{ detail.createdAt }}</div>
        </div>
      </div>

      <el-divider />

      <div class="content" v-html="detail.content" />
    </template>
  </el-card>

  <el-card style="margin-top: 12px">
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
      <div style="font-weight: 800">评论</div>
      <el-button :loading="commentLoading" @click="loadComments">刷新</el-button>
    </div>

    <div style="margin-top: 12px">
      <el-empty v-if="!commentRows.length && !commentLoading" :description="commentEmptyText" />

      <div v-else v-loading="commentLoading" style="display: flex; flex-direction: column; gap: 10px">
        <div
          v-for="c in commentRows"
          :key="c.id"
          style="border: 1px solid rgba(15, 23, 42, 0.08); border-radius: 12px; padding: 10px 12px; background: #fff"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
            <div style="font-weight: 700; color: #111827">{{ displayName(c) }}</div>
            <div style="font-size: 12px; color: var(--el-text-color-secondary)">{{ c.createdAt }}</div>
          </div>
          <div style="margin-top: 6px; white-space: pre-wrap; word-break: break-word">{{ c.content }}</div>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; margin-top: 12px">
        <el-pagination
          layout="total, prev, pager, next, sizes"
          :total="commentPage.total"
          :page-size="commentPage.size"
          :current-page="commentPage.current"
          @current-change="
            (p: number) => {
              commentPage.current = p
              loadComments()
            }
          "
          @size-change="
            (s: number) => {
              commentPage.size = s
              commentPage.current = 1
              loadComments()
            }
          "
        />
      </div>
    </div>

    <el-divider />

    <div style="display: flex; gap: 10px; align-items: flex-start; flex-wrap: wrap">
      <el-input
        v-model="commentForm.content"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        style="flex: 1; min-width: 240px"
      />
      <el-button type="primary" :loading="posting" @click="submitComment">发表评论</el-button>
    </div>
    <div v-if="!userStore.token" style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
      未登录将会跳转到登录页后再发表评论。
    </div>
  </el-card>
</template>

<style scoped>
.content :deep(img) {
  max-width: 100%;
}

.content :deep(p) {
  line-height: 1.8;
}
</style>

