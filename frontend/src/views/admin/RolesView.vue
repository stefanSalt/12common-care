<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { RoleDto } from '../../stores/user'
import { createRole, deleteRole, getRoleDetail, listRoles, setRolePermissions, updateRole } from '../../api/role'
import type { PermissionDto } from '../../api/permission'
import { listPermissions } from '../../api/permission'

const loading = ref(false)
const saving = ref(false)
const rows = ref<RoleDto[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = await listRoles()
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
  code: '',
  name: '',
  description: '',
})

function openCreate() {
  editMode.value = 'create'
  editingId.value = ''
  form.code = ''
  form.name = ''
  form.description = ''
  editDialogOpen.value = true
}

async function openEdit(row: RoleDto) {
  editMode.value = 'edit'
  editingId.value = row.id
  saving.value = true
  try {
    const detail = await getRoleDetail(row.id)
    form.code = detail.code
    form.name = detail.name
    form.description = detail.description ?? ''
    editDialogOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载角色详情失败')
  } finally {
    saving.value = false
  }
}

async function submitRole() {
  if (editMode.value === 'create') {
    if (!form.code.trim()) return ElMessage.warning('请输入角色编码')
    if (!form.name.trim()) return ElMessage.warning('请输入角色名称')
  } else {
    if (!form.name.trim()) return ElMessage.warning('请输入角色名称')
  }

  saving.value = true
  try {
    if (editMode.value === 'create') {
      await createRole({
        code: form.code.trim(),
        name: form.name.trim(),
        description: form.description || undefined,
      })
    } else {
      await updateRole(editingId.value, { name: form.name.trim(), description: form.description || undefined })
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

async function removeRole(row: RoleDto) {
  try {
    await ElMessageBox.confirm(`确认删除角色 ${row.name}？`, '提示', { type: 'warning' })
    saving.value = true
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}

const assignDialogOpen = ref(false)
const assignRoleId = ref('')
const selectedPermissionIds = ref<string[]>([])
const permissionOptions = ref<PermissionDto[]>([])
const permissionLoading = ref(false)

async function ensurePermissionsLoaded() {
  if (permissionOptions.value.length > 0) return
  permissionLoading.value = true
  try {
    permissionOptions.value = await listPermissions()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载权限列表失败')
  } finally {
    permissionLoading.value = false
  }
}

async function openAssignPermissions(row: RoleDto) {
  assignRoleId.value = row.id
  assignDialogOpen.value = true

  await ensurePermissionsLoaded()

  saving.value = true
  try {
    const detail = await getRoleDetail(row.id)
    selectedPermissionIds.value = (detail.permissions ?? []).map((p) => p.id)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载角色权限失败')
  } finally {
    saving.value = false
  }
}

async function submitPermissions() {
  saving.value = true
  try {
    await setRolePermissions(assignRoleId.value, selectedPermissionIds.value)
    ElMessage.success('保存成功')
    assignDialogOpen.value = false
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
        <el-button v-permission="'role:create'" type="primary" @click="openCreate">新增角色</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="code" label="编码" width="180" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'role:update'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'role:update'" size="small" @click="openAssignPermissions(row)">分配权限</el-button>
            <el-button v-permission="'role:delete'" size="small" type="danger" @click="removeRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogOpen" :title="editMode === 'create' ? '新增角色' : '编辑角色'" width="480px">
      <el-form label-position="top">
        <el-form-item label="角色编码">
          <el-input v-model="form.code" :disabled="editMode === 'edit'" autocomplete="off" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogOpen" title="分配权限（覆盖式保存）" width="560px">
      <el-form label-position="top">
        <el-form-item label="权限">
          <el-select
            v-model="selectedPermissionIds"
            multiple
            filterable
            style="width: 100%"
            :loading="permissionLoading"
          >
            <el-option
              v-for="p in permissionOptions"
              :key="p.id"
              :label="`${p.name} (${p.code})`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
