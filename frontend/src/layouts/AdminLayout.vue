<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const route = useRoute()

type MenuItem = { label: string; index: string; permission?: string }

const menuItems = computed<MenuItem[]>(() => [
  { label: '首页', index: '/admin' },
  { label: '用户', index: '/admin/users', permission: 'user:list' },
  { label: '角色', index: '/admin/roles', permission: 'role:list' },
  { label: '权限', index: '/admin/permissions', permission: 'permission:list' },
  { label: '文件', index: '/admin/files' },
  { label: '通知', index: '/admin/notifications' },
])

const visibleMenuItems = computed(() => {
  const permissions = userStore.permissions
  return menuItems.value.filter((item) => !item.permission || permissions.includes(item.permission))
})

function logout() {
  // Per requirements: logout is client-side only.
  userStore.clear()
  window.location.href = '/login'
}
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-header style="display: flex; justify-content: space-between; align-items: center">
      <div>admin-base</div>
      <div style="display: flex; gap: 12px; align-items: center">
        <span>{{ userStore.user?.username }}</span>
        <el-button type="primary" plain size="small" @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="200px" style="border-right: 1px solid var(--el-border-color)">
        <el-menu :default-active="route.path" router style="border-right: none">
          <el-menu-item v-for="item in visibleMenuItems" :key="item.index" :index="item.index">
            {{ item.label }}
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
      <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
