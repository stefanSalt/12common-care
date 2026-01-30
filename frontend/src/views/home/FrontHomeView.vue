<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import type { BannerDto } from '../../api/banner'
import { listPublicBanners } from '../../api/banner'
import { listPublicCrowdfundingProjects, type CrowdfundingProjectDto } from '../../api/crowdfunding'
import { listPublicActivities, type ActivityDto } from '../../api/activity'
import { listPublicStories, type StoryDto } from '../../api/story'
import type { NotificationDto } from '../../api/notification'
import { listNotifications } from '../../api/notification'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loadingBanners = ref(false)
const loadingAnnouncements = ref(false)
const loadingCrowdfunding = ref(false)
const loadingActivities = ref(false)
const loadingStories = ref(false)

const banners = ref<BannerDto[]>([])
const announcements = ref<NotificationDto[]>([])
const latestCrowdfunding = ref<CrowdfundingProjectDto[]>([])
const latestActivities = ref<ActivityDto[]>([])
const latestStories = ref<StoryDto[]>([])

const isLoggedIn = computed(() => !!userStore.token)

function bannerImageUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function coverUrl(fileId: string) {
  return fileId ? `/api/files/${fileId}/download` : ''
}

function isEnded(endTime?: string) {
  if (!endTime) return false
  const ms = new Date(endTime).getTime()
  return Number.isFinite(ms) ? Date.now() > ms : false
}

function percent(item: CrowdfundingProjectDto) {
  const target = Number(item.targetAmount ?? 0)
  const raised = Number(item.raisedAmount ?? 0)
  if (!target || target <= 0) return 0
  return Math.min(100, Math.round((raised / target) * 100))
}

function activityPercent(item: ActivityDto) {
  const target = Number(item.donationTarget ?? 0)
  const raised = Number(item.donatedAmount ?? 0)
  if (!target || target <= 0) return 0
  return Math.min(100, Math.round((raised / target) * 100))
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

async function loadCrowdfunding() {
  loadingCrowdfunding.value = true
  try {
    const data = await listPublicCrowdfundingProjects(1, 6)
    latestCrowdfunding.value = data.records ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '众筹加载失败')
  } finally {
    loadingCrowdfunding.value = false
  }
}

async function loadActivities() {
  loadingActivities.value = true
  try {
    const data = await listPublicActivities(1, 6)
    latestActivities.value = data.records ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '公益活动加载失败')
  } finally {
    loadingActivities.value = false
  }
}

async function loadStories() {
  loadingStories.value = true
  try {
    const data = await listPublicStories(1, 6)
    latestStories.value = data.records ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '事迹加载失败')
  } finally {
    loadingStories.value = false
  }
}

async function load() {
  await Promise.all([loadBanners(), loadAnnouncements(), loadCrowdfunding(), loadActivities(), loadStories()])
}

onMounted(load)

function goCrowdfundingList() {
  router.push('/crowdfunding')
}

function goCrowdfundingDetail(id: string) {
  router.push(`/crowdfunding/${id}`)
}

function goStoryList() {
  router.push('/stories')
}

function goStoryDetail(id: string) {
  router.push(`/stories/${id}`)
}

function goActivitiesList() {
  router.push('/activities')
}

function goActivityDetail(id: string) {
  router.push(`/activities/${id}`)
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px">
          <div style="font-weight: 700">系统首页</div>
          <el-button
            size="small"
            :loading="loadingBanners || loadingAnnouncements || loadingCrowdfunding || loadingActivities || loadingStories"
            @click="load"
          >
            刷新
          </el-button>
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
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px">
          <div style="font-weight: 700">最新爱心众筹</div>
          <el-button size="small" @click="goCrowdfundingList">查看更多</el-button>
        </div>
      </template>

      <div v-loading="loadingCrowdfunding">
        <el-empty v-if="latestCrowdfunding.length === 0 && !loadingCrowdfunding" description="暂无众筹项目" />

        <el-row v-else :gutter="12">
          <el-col v-for="item in latestCrowdfunding" :key="item.id" :xs="24" :sm="12" :md="8">
            <div
              style="
                border: 1px solid rgba(15, 23, 42, 0.08);
                border-radius: 12px;
                overflow: hidden;
                background: #fff;
                cursor: pointer;
                margin-bottom: 12px;
              "
              @click="goCrowdfundingDetail(item.id)"
            >
              <img :src="coverUrl(item.coverFileId)" alt="cover" style="width: 100%; height: 140px; object-fit: cover" />
              <div style="padding: 10px 12px">
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px">
                  <div style="font-weight: 800; color: #111827; line-height: 1.2; flex: 1">{{ item.title }}</div>
                  <el-tag :type="isEnded(item.endTime) ? 'info' : 'success'" effect="light">
                    {{ isEnded(item.endTime) ? '已结束' : '进行中' }}
                  </el-tag>
                </div>

                <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
                  目标：{{ item.targetAmount }}｜已筹：{{ item.raisedAmount }}
                </div>
                <div style="margin-top: 6px; font-size: 12px; color: var(--el-text-color-secondary)">
                  截止：{{ item.endTime }}
                </div>

                <div style="margin-top: 10px">
                  <el-progress :percentage="percent(item)" :stroke-width="10" />
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px">
          <div style="font-weight: 700">最新公益活动</div>
          <el-button size="small" @click="goActivitiesList">查看更多</el-button>
        </div>
      </template>

      <div v-loading="loadingActivities">
        <el-empty v-if="latestActivities.length === 0 && !loadingActivities" description="暂无公益活动" />

        <el-row v-else :gutter="12">
          <el-col v-for="item in latestActivities" :key="item.id" :xs="24" :sm="12" :md="8">
            <div
              style="
                border: 1px solid rgba(15, 23, 42, 0.08);
                border-radius: 12px;
                overflow: hidden;
                background: #fff;
                cursor: pointer;
                margin-bottom: 12px;
              "
              @click="goActivityDetail(item.id)"
            >
              <img :src="coverUrl(item.coverFileId)" alt="cover" style="width: 100%; height: 140px; object-fit: cover" />
              <div style="padding: 10px 12px">
                <div style="font-weight: 800; color: #111827; line-height: 1.2">{{ item.title }}</div>
                <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">
                  时间：{{ item.startTime }} ~ {{ item.endTime }}
                </div>

                <div v-if="item.donateEnabled && item.donationTarget" style="margin-top: 10px">
                  <div style="font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 6px">
                    捐赠进度：{{ item.donatedAmount ?? 0 }} / {{ item.donationTarget }}
                  </div>
                  <el-progress :percentage="activityPercent(item)" :stroke-width="10" />
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 8px">
          <div style="font-weight: 700">最新爱心事迹</div>
          <el-button size="small" @click="goStoryList">查看更多</el-button>
        </div>
      </template>

      <div v-loading="loadingStories">
        <el-empty v-if="latestStories.length === 0 && !loadingStories" description="暂无文章" />

        <el-row v-else :gutter="12">
          <el-col v-for="item in latestStories" :key="item.id" :xs="24" :sm="12" :md="8">
            <div
              style="
                border: 1px solid rgba(15, 23, 42, 0.08);
                border-radius: 12px;
                overflow: hidden;
                background: #fff;
                cursor: pointer;
                margin-bottom: 12px;
              "
              @click="goStoryDetail(item.id)"
            >
              <img :src="coverUrl(item.coverFileId)" alt="cover" style="width: 100%; height: 140px; object-fit: cover" />
              <div style="padding: 10px 12px">
                <div style="font-weight: 800; color: #111827; line-height: 1.2">{{ item.title }}</div>
                <div style="margin-top: 8px; font-size: 12px; color: var(--el-text-color-secondary)">发布时间：{{ item.createdAt }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>
