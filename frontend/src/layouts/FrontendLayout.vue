<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const PROJECT_NAME = '管理系统'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const avatarUrl = computed(() => {
  const id = userStore.user?.avatarFileId
  return id ? `/api/files/${id}/download` : ''
})

const activeNav = computed(() => {
  const p = route.path
  if (p.startsWith('/activities')) return '/activities'
  if (p.startsWith('/crowdfunding')) return '/crowdfunding'
  if (p.startsWith('/messages')) return '/messages'
  return '/'
})

const year = computed(() => new Date().getFullYear())

function initials(name: string) {
  const n = (name || '').trim()
  return n ? n.slice(0, 1).toUpperCase() : ' '
}

function goHome() {
  router.push('/')
}

function goMessages() {
  router.push('/messages')
}

function goMySignups() {
  router.push('/my/signups')
}

function goMyFavorites() {
  router.push('/my/favorites')
}

function goMyDonations() {
  router.push('/my/donations')
}

function goMyCrowdfundingDonations() {
  router.push('/my/crowdfunding-donations')
}

function goMyCrowdfundingProjects() {
  router.push('/my/crowdfunding-projects')
}

function goCrowdfundingNew() {
  router.push('/crowdfunding/new')
}

function goProfile() {
  router.push('/profile')
}

function goLogin() {
  router.push('/login')
}

function goRegister() {
  router.push('/register')
}

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<template>
  <div class="front-shell">
    <header class="front-header">
      <div class="front-header__inner">
        <div class="front-brand" @click="goHome">
          <div class="front-logo" aria-hidden="true">{{ initials(PROJECT_NAME) }}</div>
          <div class="front-brand__name">{{ PROJECT_NAME }}</div>
        </div>

        <el-menu
          class="front-nav"
          mode="horizontal"
          router
          :default-active="activeNav"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.92)"
          active-text-color="#ffffff"
          :ellipsis="false"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/activities">公益活动</el-menu-item>
          <el-menu-item index="/crowdfunding">爱心众筹</el-menu-item>
          <el-menu-item index="/messages">留言</el-menu-item>
        </el-menu>

        <div class="front-user">
          <template v-if="userStore.user">
            <el-dropdown trigger="click">
              <div class="front-user__trigger">
                <el-avatar :size="32" :src="avatarUrl || undefined">{{ initials(displayName) }}</el-avatar>
                <div class="front-user__name">{{ displayName }}</div>
                <el-icon :size="16" class="front-user__chevron"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goProfile">个人信息</el-dropdown-item>
                  <el-dropdown-item @click="goMySignups">我的报名</el-dropdown-item>
                  <el-dropdown-item @click="goMyFavorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="goMyDonations">我的捐赠</el-dropdown-item>
                  <el-dropdown-item @click="goMyCrowdfundingDonations">众筹捐款</el-dropdown-item>
                  <el-dropdown-item @click="goMyCrowdfundingProjects">我的众筹</el-dropdown-item>
                  <el-dropdown-item @click="goCrowdfundingNew">发起众筹</el-dropdown-item>
                  <el-dropdown-item @click="goMessages">我的留言</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button size="small" @click="goLogin">登录</el-button>
            <el-button size="small" class="front-cta" @click="goRegister">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="front-main">
      <div class="front-main__inner">
        <router-view />
      </div>
    </main>

    <footer class="front-footer">
      <div class="front-footer__inner">© {{ year }} {{ PROJECT_NAME }} 版权所有</div>
    </footer>
  </div>
</template>

<style scoped>
.front-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6f7;
}

.front-shell {
  --front-accent: #f97316;
  --front-accent-hover: #ea580c;
}

.front-header {
  height: 64px;
  background: var(--front-accent);
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.front-header__inner {
  height: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.front-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  min-width: 180px;
}

.front-logo {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: var(--front-accent);
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.front-brand__name {
  font-size: 16px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.98);
  letter-spacing: 0.2px;
}

.front-nav {
  flex: 1;
  justify-content: center;
  border-bottom: none;
}

.front-nav :deep(.el-menu) {
  border-bottom: none;
}

.front-nav :deep(.el-menu--horizontal) {
  border-bottom: none;
}

.front-nav :deep(.el-menu-item) {
  border-bottom: 2px solid transparent;
  margin: 0 6px;
  padding: 0 10px;
  border-radius: 10px;
}

.front-nav :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.12);
}

.front-nav :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.08);
  border-bottom-color: rgba(255, 255, 255, 0.95);
}

.front-user {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  min-width: 200px;
}

.front-user__trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  padding: 6px 8px;
  border-radius: 12px;
}

.front-user__trigger:hover {
  background: rgba(255, 255, 255, 0.12);
}

.front-user__name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(255, 255, 255, 0.98);
  font-weight: 700;
}

.front-user__chevron {
  color: rgba(255, 255, 255, 0.9);
}

.front-cta {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(255, 255, 255, 0.7);
  color: #111827;
}

.front-cta:hover {
  background: #ffffff;
  border-color: #ffffff;
  color: #111827;
}

.front-main {
  flex: 1;
  padding: 18px 16px;
}

.front-main__inner {
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  padding: 16px;
}

.front-footer {
  padding: 14px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
  background: var(--front-accent);
  color: rgba(255, 255, 255, 0.88);
}

.front-footer__inner {
  max-width: 1100px;
  margin: 0 auto;
  text-align: center;
  font-size: 12px;
}

@media (max-width: 768px) {
  .front-brand {
    min-width: 140px;
  }

  .front-user__name {
    display: none;
  }

  .front-nav {
    justify-content: flex-start;
  }
}
</style>
