<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const router = useRouter()

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const avatarUrl = computed(() => {
  const id = userStore.user?.avatarFileId
  return id ? `/api/files/${id}/download` : ''
})

function goProfile() {
  router.push('/profile')
}

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column">
    <header style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px">
      <div>前台（占位）</div>
      <div v-if="userStore.user" style="display: flex; gap: 12px; align-items: center">
        <el-avatar :size="28" :src="avatarUrl || undefined">{{ displayName?.slice(0, 1)?.toUpperCase() }}</el-avatar>
        <span>{{ displayName }}</span>
        <el-button size="small" @click="goProfile">个人信息</el-button>
        <el-button size="small" type="primary" plain @click="logout">退出</el-button>
      </div>
      <div v-else>
        <el-button size="small" type="primary" @click="router.push('/login')">登录</el-button>
      </div>
    </header>
    <main style="flex: 1; padding: 16px">
      <router-view />
    </main>
  </div>
</template>
