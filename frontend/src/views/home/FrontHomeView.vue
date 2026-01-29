<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { BannerDto } from '../../api/banner'
import { listPublicBanners } from '../../api/banner'
import type { NotificationDto } from '../../api/notification'
import { listNotifications } from '../../api/notification'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loadingBanners = ref(false)
const loadingAnnouncements = ref(false)

const banners = ref<BannerDto[]>([])
const announcements = ref<NotificationDto[]>([])

const isLoggedIn = computed(() => !!userStore.token)

function bannerImageUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function openLink(linkUrl?: string) {
  const url = (linkUrl || '').trim()
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function loadBanners() {
  loadingBanners.value = true
  try {
    const data = await listPublicBanners(1, 10)
    banners.value = data.records ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '轮播图加载失败')
  } finally {
    loadingBanners.value = false
  }
}

async function loadAnnouncements() {
  if (!isLoggedIn.value) {
    announcements.value = []
    return
  }
  loadingAnnouncements.value = true
  try {
    const data = await listNotifications(1, 20)
    const items = data.records ?? []
    announcements.value = items.filter((n) => n.type === 'ANNOUNCEMENT')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '公告加载失败')
  } finally {
    loadingAnnouncements.value = false
  }
}

async function load() {
  await Promise.all([loadBanners(), loadAnnouncements()])
}

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px">
          <div style="font-weight: 700">系统首页</div>
          <el-button size="small" :loading="loadingBanners || loadingAnnouncements" @click="load">刷新</el-button>
        </div>
      </template>

      <el-skeleton :loading="loadingBanners" animated>
        <el-carousel v-if="banners.length > 0" height="240px" trigger="click">
          <el-carousel-item v-for="b in banners" :key="b.id">
            <div
              style="
                width: 100%;
                height: 240px;
                overflow: hidden;
                border-radius: 10px;
                cursor: pointer;
                position: relative;
                background: #0f172a;
              "
              @click="openLink(b.linkUrl)"
            >
              <img
                :src="bannerImageUrl(b.imageFileId)"
                :alt="b.title || 'banner'"
                style="width: 100%; height: 100%; object-fit: cover; opacity: 0.98"
              />
              <div
                v-if="b.title"
                style="
                  position: absolute;
                  left: 12px;
                  bottom: 12px;
                  padding: 6px 10px;
                  border-radius: 10px;
                  color: #fff;
                  font-weight: 700;
                  background: rgba(15, 23, 42, 0.55);
                  backdrop-filter: blur(8px);
                "
              >
                {{ b.title }}
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        <el-empty v-else description="暂无轮播图" />
      </el-skeleton>
    </el-card>

    <el-card v-loading="loadingAnnouncements">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="font-weight: 700">系统公告</div>
          <div style="color: var(--el-text-color-secondary); font-size: 12px">仅登录可见</div>
        </div>
      </template>

      <div v-if="!isLoggedIn" style="color: var(--el-text-color-secondary)">登录后可查看系统公告</div>
      <div v-else-if="announcements.length === 0" style="color: var(--el-text-color-secondary)">暂无公告</div>
      <div v-else style="display: flex; flex-direction: column; gap: 10px">
        <el-alert
          v-for="n in announcements"
          :key="n.id"
          type="info"
          :title="n.title"
          :closable="false"
          show-icon
        >
          <template #default>
            <div style="white-space: pre-wrap; line-height: 1.5">{{ n.content }}</div>
            <div style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">
              {{ n.createdAt }}
            </div>
          </template>
        </el-alert>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div style="font-weight: 700">最新爱心众筹</div>
      </template>
      <div style="color: var(--el-text-color-secondary)">待开发：将展示最新众筹项目列表与捐款进度</div>
    </el-card>
  </div>
</template>

