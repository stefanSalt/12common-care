<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PermissionDto } from '../../api/permission'
import { createPermission, deletePermission, listPermissions, updatePermission } from '../../api/permission'

const loading = ref(false)
const saving = ref(false)
const rows = ref<PermissionDto[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = await listPermissions()
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

function openEdit(row: PermissionDto) {
  editMode.value = 'edit'
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.description = row.description ?? ''
  editDialogOpen.value = true
}

async function submitPermission() {
  if (editMode.value === 'create') {
    if (!form.code.trim()) return ElMessage.warning('请输入权限标识')
    if (!form.name.trim()) return ElMessage.warning('请输入权限名称')
  } else {
    if (!form.name.trim()) return ElMessage.warning('请输入权限名称')
  }

  saving.value = true
  try {
    if (editMode.value === 'create') {
      await createPermission({
        code: form.code.trim(),
        name: form.name.trim(),
        description: form.description || undefined,
      })
    } else {
      await updatePermission(editingId.value, { name: form.name.trim(), description: form.description || undefined })
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

async function removePermission(row: PermissionDto) {
  try {
    await ElMessageBox.confirm(`确认删除权限 ${row.name}？`, '提示', { type: 'warning' })
    saving.value = true
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message ?? '删除失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 12px">
    <el-card>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-button v-permission="'permission:create'" type="primary" @click="openCreate">新增权限</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="code" label="标识" width="220" />
        <el-table-column prop="name" label="名称" width="220" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'permission:update'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'permission:delete'" size="small" type="danger" @click="removePermission(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogOpen" :title="editMode === 'create' ? '新增权限' : '编辑权限'" width="520px">
      <el-form label-position="top">
        <el-form-item label="权限标识">
          <el-input v-model="form.code" :disabled="editMode === 'edit'" autocomplete="off" />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
