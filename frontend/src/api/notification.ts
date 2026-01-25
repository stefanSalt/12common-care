import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'

export type NotificationDto = {
  id: string
  title: string
  content: string
  type: string
  isRead: number
  createdAt: string
}

export type AnnounceRequest = {
  title: string
  content: string
}

export async function listNotifications(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<NotificationDto>>>('/notifications', { params: { current, size } })
  return res.data.data
}

export async function markRead(id: string) {
  const res = await request.put<ApiResult<null>>(`/notifications/${id}/read`)
  return res.data.data
}

export async function announce(payload: AnnounceRequest) {
  const res = await request.post<ApiResult<null>>('/notifications/announce', payload)
  return res.data.data
}

