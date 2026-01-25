<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { RoleDto, UserDto } from '../../stores/user'
import { createUser, deleteUser, listUsers, setUserRoles, updateUser } from '../../api/user'
import { listRoles } from '../../api/role'

const loading = ref(false)
const saving = ref(false)

const rows = ref<UserDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listUsers(page.current, page.size)
    rows.value = data.records ?? []
    page.total = Number(data.total ?? 0)
    page.current = Number(data.current ?? page.current)
    page.size = Number(data.size ?? page.size)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

const editDialogOpen = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editingId = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  status: 1,
})

function openCreate() {
  editMode.value = 'create'
  editingId.value = ''
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.status = 1
  editDialogOpen.value = true
}

function openEdit(row: UserDto) {
  editMode.value = 'edit'
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.nickname = row.nickname ?? ''
  form.status = row.status ?? 1
  editDialogOpen.value = true
}

async function submitUser() {
  if (editMode.value === 'create') {
    if (!form.username.trim()) return ElMessage.warning('请输入用户名')
    if (!form.password.trim()) return ElMessage.warning('请输入密码')
  }

  saving.value = true
  try {
    if (editMode.value === 'create') {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        nickname: form.nickname || undefined,
        status: form.status,
      })
    } else {
      await updateUser(editingId.value, {
        nickname: form.nickname || undefined,
        status: form.status,
      })
    }
    ElMessage.success('保存成功')
    editDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeUser(row: UserDto) {
  try {
    await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '提示', { type: 'warning' })
    saving.value = true
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}

const assignDialogOpen = ref(false)
const assignUserId = ref('')
const selectedRoleIds = ref<string[]>([])
const roleOptions = ref<RoleDto[]>([])
const roleLoading = ref(false)

async function ensureRolesLoaded() {
  if (roleOptions.value.length > 0) return
  roleLoading.value = true
  try {
    roleOptions.value = await listRoles()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载角色列表失败')
  } finally {
    roleLoading.value = false
  }
}

async function openAssignRoles(row: UserDto) {
  assignUserId.value = row.id
  selectedRoleIds.value = (row.roles ?? []).map((r) => r.id)
  assignDialogOpen.value = true
  await ensureRolesLoaded()
}

async function submitRoles() {
  saving.value = true
  try {
    await setUserRoles(assignUserId.value, selectedRoleIds.value)
    ElMessage.success('保存成功')
    assignDialogOpen.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-button v-permission="'user:create'" type="primary" @click="openCreate">新增用户</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="username" label="用户名" width="160" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'">{{ row.status === 0 ? '禁用' : '启用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色">
          <template #default="{ row }">
            <div style="display: flex; gap: 6px; flex-wrap: wrap">
              <el-tag v-for="r in row.roles || []" :key="r.id" size="small">{{ r.name }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'user:update'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'user:update'" size="small" @click="openAssignRoles(row)">分配角色</el-button>
            <el-button v-permission="'user:delete'" size="small" type="danger" @click="removeUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 12px">
        <el-pagination
          layout="total, prev, pager, next, sizes"
          :total="page.total"
          :page-size="page.size"
          :current-page="page.current"
          @current-change="
            (p: number) => {
              page.current = p
              load()
            }
          "
          @size-change="
            (s: number) => {
              page.size = s
              page.current = 1
              load()
            }
          "
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialogOpen" :title="editMode === 'create' ? '新增用户' : '编辑用户'" width="420px">
      <el-form label-position="top">
        <el-form-item v-if="editMode === 'create'" label="用户名">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="editMode === 'create'" label="密码">
          <el-input v-model="form.password" type="password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" autocomplete="off" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUser">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogOpen" title="分配角色（覆盖式保存）" width="480px">
      <el-form label-position="top">
        <el-form-item label="角色">
          <el-select v-model="selectedRoleIds" multiple filterable style="width: 100%" :loading="roleLoading">
            <el-option v-for="r in roleOptions" :key="r.id" :label="`${r.name} (${r.code})`" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
