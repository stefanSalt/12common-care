<script setup lang="ts">
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
</script>

<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column">
    <header style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px">
      <div>frontend (placeholder)</div>
      <div v-if="userStore.user" style="display: flex; gap: 12px; align-items: center">
        <span>{{ userStore.user.username }}</span>
      </div>
    </header>
    <main style="flex: 1; padding: 16px">
      <router-view />
    </main>
  </div>
</template>

