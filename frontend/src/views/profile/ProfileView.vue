<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { UploadProps } from 'element-plus'
import { ElMessage } from 'element-plus'
import { changePassword, me, updateMe } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
})

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const roleNames = computed(() => (userStore.user?.roles ?? []).map((r) => r.name).join(' / '))
const avatarUrl = computed(() => {
  const id = userStore.user?.avatarFileId
  return id ? `/api/files/${id}/download` : ''
})

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) headers.Authorization = `Bearer ${userStore.token}`
  return headers
})

async function loadMe() {
  loading.value = true
  try {
    const user = await me()
    userStore.setUser(user)
    form.username = user.username
    form.nickname = user.nickname ?? ''
    form.email = user.email ?? ''
    form.phone = user.phone ?? ''
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadMe)

async function onSave() {
  saving.value = true
  try {
    const nickname = form.nickname.trim()
    const email = form.email.trim()
    const phone = form.phone.trim()
    const user = await updateMe({
      nickname: nickname || null,
      email: email || null,
      phone: phone || null,
    })
    userStore.setUser(user)
    ElMessage.success('已保存')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

const changingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function onChangePassword() {
  const oldPassword = passwordForm.oldPassword
  const newPassword = passwordForm.newPassword
  const confirmPassword = passwordForm.confirmPassword

  if (!oldPassword || !newPassword || !confirmPassword) {
    ElMessage.error('请填写完整的密码信息')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  changingPassword.value = true
  try {
    await changePassword({ oldPassword, newPassword })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码已修改')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '修改失败')
  } finally {
    changingPassword.value = false
  }
}

const onAvatarUploadSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response && response.code === 0 && response.data) {
    userStore.setUser(response.data)
    ElMessage.success('头像已更新')
    return
  }
  ElMessage.error(response?.message ?? '头像上传失败')
}

const onAvatarUploadError: UploadProps['onError'] = () => {
  ElMessage.error('头像上传失败')
}
</script>

<template>
  <div style="max-width: 900px; margin: 0 auto">
    <el-card v-loading="loading">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-avatar :size="72" :src="avatarUrl || undefined">{{ displayName?.slice(0, 1)?.toUpperCase() }}</el-avatar>
        <div style="display: flex; flex-direction: column; gap: 6px; min-width: 220px">
          <div style="font-size: 18px; font-weight: 700">{{ displayName }}</div>
          <div style="color: var(--el-text-color-secondary); font-size: 12px">{{ roleNames || ' ' }}</div>
        </div>

        <el-upload
          action="/api/auth/me/avatar"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="onAvatarUploadSuccess"
          :on-error="onAvatarUploadError"
        >
          <el-button type="primary" plain>上传头像</el-button>
        </el-upload>
      </div>

      <el-divider />

      <el-form label-position="top" style="max-width: 520px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="off" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" autocomplete="off" />
        </el-form-item>
      </el-form>

      <div style="display: flex; gap: 8px">
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        <el-button :disabled="loading" @click="loadMe">刷新</el-button>
      </div>

      <el-divider />

      <div style="font-weight: 600; margin-bottom: 12px">修改密码</div>
      <el-form label-position="top" style="max-width: 520px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" autocomplete="new-password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" autocomplete="new-password" show-password />
        </el-form-item>
      </el-form>
      <div>
        <el-button type="primary" :loading="changingPassword" @click="onChangePassword">修改密码</el-button>
      </div>
    </el-card>
  </div>
</template>
