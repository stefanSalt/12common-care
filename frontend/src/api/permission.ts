import request, { type ApiResult } from '../utils/request'

export type PermissionDto = {
  id: string
  code: string
  name: string
  description?: string
}

export type CreatePermissionRequest = {
  code: string
  name: string
  description?: string
}

export type UpdatePermissionRequest = {
  name: string
  description?: string
}

export async function listPermissions() {
  const res = await request.get<ApiResult<PermissionDto[]>>('/permissions')
  return res.data.data
}

export async function createPermission(payload: CreatePermissionRequest) {
  const res = await request.post<ApiResult<PermissionDto>>('/permissions', payload)
  return res.data.data
}

export async function updatePermission(id: string, payload: UpdatePermissionRequest) {
  const res = await request.put<ApiResult<PermissionDto>>(`/permissions/${id}`, payload)
  return res.data.data
}

export async function deletePermission(id: string) {
  const res = await request.delete<ApiResult<null>>(`/permissions/${id}`)
  return res.data.data
}

