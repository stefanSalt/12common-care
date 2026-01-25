<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function onSubmit() {
  loading.value = true
  try {
    const data = await login(form.username, form.password)
    userStore.setTokens(data.token, data.refreshToken)
    userStore.setUser(data.user)

    const isAdmin = (data.user.roles ?? []).some((r) => r.code === 'admin')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''

    // Default: admin -> /admin ; others -> /
    if (isAdmin) {
      await router.replace(redirect.startsWith('/admin') ? redirect : '/admin')
    } else {
      await router.replace('/')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px">
    <el-card style="width: 360px">
      <template #header>
        <div>登录</div>
      </template>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>
