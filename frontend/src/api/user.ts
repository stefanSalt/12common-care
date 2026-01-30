import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'
import type { UserDto } from '../stores/user'

export type CreateUserRequest = {
  username: string
  password: string
  nickname?: string
  status?: number
}

export type UpdateUserRequest = {
  nickname?: string
  status?: number
}

export async function listUsers(
  current = 1,
  size = 10,
  roleCode?: string,
  excludeRoleCode?: string,
) {
  const res = await request.get<ApiResult<PageResult<UserDto>>>('/users', {
    params: { current, size, roleCode, excludeRoleCode },
  })
  return res.data.data
}

export async function createUser(payload: CreateUserRequest) {
  const res = await request.post<ApiResult<UserDto>>('/users', payload)
  return res.data.data
}

export async function updateUser(id: string, payload: UpdateUserRequest) {
  const res = await request.put<ApiResult<UserDto>>(`/users/${id}`, payload)
  return res.data.data
}

export async function deleteUser(id: string) {
  const res = await request.delete<ApiResult<null>>(`/users/${id}`)
  return res.data.data
}

export async function setUserRoles(userId: string, roleIds: string[]) {
  const res = await request.put<ApiResult<null>>(`/users/${userId}/roles`, { roleIds })
  return res.data.data
}
