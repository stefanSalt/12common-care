<script setup lang="ts">
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

function logout() {
  // Per requirements: logout is client-side only.
  userStore.clear()
  window.location.href = '/login'
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column">
    <header style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px">
      <div>admin-base</div>
      <div style="display: flex; gap: 12px; align-items: center">
        <span>{{ userStore.user?.username }}</span>
        <el-button type="primary" plain size="small" @click="logout">退出登录</el-button>
      </div>
    </header>
    <main style="flex: 1; padding: 16px">
      <router-view />
    </main>
  </div>
</template>

