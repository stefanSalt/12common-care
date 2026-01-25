import request, { type ApiResult } from '../utils/request'
import type { UserDto } from '../stores/user'

export type LoginResponseData = {
  token: string
  refreshToken: string
  user: UserDto
}

export async function login(username: string, password: string) {
  const res = await request.post<ApiResult<LoginResponseData>>('/auth/login', { username, password })
  return res.data.data
}

export type RegisterRequest = {
  username: string
  password: string
  nickname?: string | null
}

export async function register(payload: RegisterRequest) {
  const res = await request.post<ApiResult<LoginResponseData>>('/auth/register', payload)
  return res.data.data
}

export async function me() {
  const res = await request.get<ApiResult<UserDto>>('/auth/me')
  return res.data.data
}

export type UpdateMeRequest = {
  nickname?: string | null
  email?: string | null
  phone?: string | null
}

export async function updateMe(payload: UpdateMeRequest) {
  const res = await request.put<ApiResult<UserDto>>('/auth/me', payload)
  return res.data.data
}

export type ChangePasswordRequest = {
  oldPassword: string
  newPassword: string
}

export async function changePassword(payload: ChangePasswordRequest) {
  const res = await request.put<ApiResult<null>>('/auth/me/password', payload)
  return res.data.data
}
