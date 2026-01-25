<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

const requiredTrim = (message: string) => {
  return (_rule: any, value: string, callback: (error?: Error) => void) => {
    if (!value || value.trim() === '') {
      callback(new Error(message))
      return
    }
    callback()
  }
}

const rules: FormRules = {
  username: [{ validator: requiredTrim('请输入用户名'), trigger: 'blur' }],
  password: [{ validator: requiredTrim('请输入密码'), trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return

  const valid = await new Promise<boolean>((resolve) => {
    formRef.value!.validate((ok) => resolve(ok))
  })
  if (!valid) return

  loading.value = true
  try {
    const data = await register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || null,
    })
    userStore.setTokens(data.token, data.refreshToken)
    userStore.setUser(data.user)

    ElMessage.success('注册成功')

    const isAdmin = (data.user.roles ?? []).some((r) => r.code === 'admin')
    if (isAdmin) await router.replace('/admin')
    else await router.replace('/')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    style="min-height: 100vh; box-sizing: border-box; display: flex; align-items: center; justify-content: center; padding: 24px"
  >
    <el-card style="width: 360px">
      <template #header>
        <div>注册</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="new-password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" autocomplete="off" />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">注册</el-button>
      </el-form>
      <div style="margin-top: 12px; text-align: center">
        <el-link type="primary" @click="router.push('/login')">已有账号？去登录</el-link>
      </div>
    </el-card>
  </div>
</template>
