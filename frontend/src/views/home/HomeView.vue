<script setup lang="ts">
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
</script>

<template>
  <el-card>
    <div style="display: flex; flex-direction: column; gap: 8px">
      <div>欢迎，{{ userStore.user?.username }}</div>
      <div>权限数量：{{ userStore.permissions.length }}</div>
      <el-button v-permission="'user:list'" type="success">示例：有权限时可见按钮</el-button>
    </div>
  </el-card>
</template>
