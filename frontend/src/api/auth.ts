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

export async function me() {
  const res = await request.get<ApiResult<UserDto>>('/auth/me')
  return res.data.data
}

