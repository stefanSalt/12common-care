<script setup lang="ts">
import { computed, ref, watch, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Avatar,
  ArrowDown,
  BellFilled,
  Calendar,
  ChatLineRound,
  Expand,
  Files,
  Fold,
  House,
  Key,
  Picture,
  Setting,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { storage } from '../utils/storage'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

type MenuItem = { label: string; index: string; icon: Component; permission?: string }

const menuItems = computed<MenuItem[]>(() => [
  { label: '首页', index: '/admin', icon: House },
  { label: '用户', index: '/admin/users', icon: User, permission: 'user:list' },
  { label: '角色', index: '/admin/roles', icon: Key, permission: 'role:list' },
  { label: '权限', index: '/admin/permissions', icon: Setting, permission: 'permission:list' },
  { label: '文件', index: '/admin/files', icon: Files },
  { label: '轮播图', index: '/admin/banners', icon: Picture, permission: 'banner:list' },
  { label: '公益活动', index: '/admin/activities', icon: Calendar, permission: 'activity:list' },
  { label: '通知', index: '/admin/notifications', icon: BellFilled },
  { label: '留言', index: '/admin/messages', icon: ChatLineRound, permission: 'message:list' },
])

const visibleMenuItems = computed(() => {
  const permissions = userStore.permissions
  return menuItems.value.filter((item) => !item.permission || permissions.includes(item.permission))
})

const COLLAPSE_KEY = 'admin_menu_collapsed'
const isCollapsed = ref(storage.getString(COLLAPSE_KEY) === '1')
watch(isCollapsed, (v) => storage.setString(COLLAPSE_KEY, v ? '1' : '0'))

const asideWidth = computed(() => (isCollapsed.value ? '64px' : '208px'))

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const roleNames = computed(() => (userStore.user?.roles ?? []).map((r) => r.name).join(' / '))
const avatarUrl = computed(() => {
  const id = userStore.user?.avatarFileId
  return id ? `/api/files/${id}/download` : ''
})

function initials(name: string) {
  const n = (name || '').trim()
  return n ? n.slice(0, 1).toUpperCase() : '?'
}

function goProfile() {
  router.push('/admin/profile')
}

function logout() {
  // Per requirements: logout is client-side only.
  userStore.clear()
  window.location.href = '/login'
}
</script>

<template>
  <el-container class="admin-shell">
    <el-header class="admin-header">
      <div class="admin-header__left">
        <el-button class="admin-icon-btn" text @click="isCollapsed = !isCollapsed">
          <el-icon :size="18">
            <component :is="isCollapsed ? Expand : Fold" />
          </el-icon>
        </el-button>
        <div class="admin-brand">
          <el-icon :size="18"><Avatar /></el-icon>
          <span class="admin-brand__text">管理后台</span>
        </div>
      </div>

      <div class="admin-header__right">
        <el-dropdown trigger="click">
          <div class="admin-user">
            <el-avatar :size="32" :src="avatarUrl || undefined">
              {{ initials(displayName) }}
            </el-avatar>
            <div class="admin-user__meta">
              <div class="admin-user__name">{{ displayName }}</div>
              <div class="admin-user__roles">{{ roleNames || ' ' }}</div>
            </div>
            <el-icon :size="16" class="admin-user__chevron"><ArrowDown /></el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goProfile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="admin-body">
      <el-aside class="admin-aside" :width="asideWidth">
        <el-menu
          class="admin-menu"
          :default-active="route.path"
          router
          :collapse="isCollapsed"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#cbd5e1"
          active-text-color="#ffffff"
        >
          <el-menu-item v-for="item in visibleMenuItems" :key="item.index" :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-shell {
  height: 100vh;
  overflow: hidden;
}

.admin-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  color: #e5e7eb;
  background: linear-gradient(90deg, #0b1220 0%, #0f172a 55%, #111827 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
}

.admin-header__left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 240px;
}

.admin-icon-btn {
  color: #e5e7eb;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  user-select: none;
}

.admin-brand__text {
  font-weight: 600;
  letter-spacing: 0.2px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.admin-user__meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.1;
  min-width: 120px;
}

.admin-user__name {
  font-size: 14px;
  font-weight: 600;
  color: #f3f4f6;
}

.admin-user__roles {
  font-size: 12px;
  color: rgba(203, 213, 225, 0.85);
}

.admin-user__chevron {
  color: rgba(203, 213, 225, 0.9);
}

.admin-body {
  height: calc(100vh - 56px);
  overflow: hidden;
}

.admin-aside {
  height: 100%;
  background: radial-gradient(1200px 600px at 0% 0%, rgba(59, 130, 246, 0.18) 0%, rgba(0, 0, 0, 0) 60%),
    linear-gradient(180deg, #0b1220 0%, #0f172a 65%, #0b1220 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.admin-menu {
  height: 100%;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.admin-menu::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.admin-menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin: 6px 10px;
}

.admin-menu.el-menu--collapse :deep(.el-menu-item) {
  margin: 6px 8px;
  padding: 0 !important;
  justify-content: center;
}

.admin-menu.el-menu--collapse :deep(.el-menu-item .el-icon) {
  margin-right: 0;
}

.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.10);
}

.admin-main {
  height: 100%;
  overflow: auto;
  background: #f5f6f7;
  padding: 16px;
}
</style>
