import request, { type ApiResult } from '../utils/request'
import type { PermissionDto } from './permission'
import type { RoleDto } from '../stores/user'

export type RoleDetailDto = {
  id: string
  code: string
  name: string
  description?: string
  permissions: PermissionDto[]
}

export type CreateRoleRequest = {
  code: string
  name: string
  description?: string
}

export type UpdateRoleRequest = {
  name: string
  description?: string
}

export async function listRoles() {
  const res = await request.get<ApiResult<RoleDto[]>>('/roles')
  return res.data.data
}

export async function getRoleDetail(id: string) {
  const res = await request.get<ApiResult<RoleDetailDto>>(`/roles/${id}`)
  return res.data.data
}

export async function createRole(payload: CreateRoleRequest) {
  const res = await request.post<ApiResult<RoleDto>>('/roles', payload)
  return res.data.data
}

export async function updateRole(id: string, payload: UpdateRoleRequest) {
  const res = await request.put<ApiResult<RoleDto>>(`/roles/${id}`, payload)
  return res.data.data
}

export async function deleteRole(id: string) {
  const res = await request.delete<ApiResult<null>>(`/roles/${id}`)
  return res.data.data
}

export async function setRolePermissions(roleId: string, permissionIds: string[]) {
  const res = await request.put<ApiResult<null>>(`/roles/${roleId}/permissions`, { permissionIds })
  return res.data.data
}

