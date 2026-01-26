import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'

export type MessageDto = {
  id: string
  userId: string
  username: string
  title: string
  content: string
  contactEmail: string
  status: number
  replyContent?: string | null
  createdAt: string
  repliedAt?: string | null
}

export type CreateMessageRequest = {
  title: string
  content: string
  contactEmail: string
}

export type ReplyMessageRequest = {
  replyContent: string
}

export async function createMessage(payload: CreateMessageRequest) {
  const res = await request.post<ApiResult<MessageDto>>('/messages', payload)
  return res.data.data
}

export async function listMyMessages(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<MessageDto>>>('/messages/my', { params: { current, size } })
  return res.data.data
}

export async function listMessages(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<MessageDto>>>('/messages', { params: { current, size } })
  return res.data.data
}

export async function replyMessage(id: string, payload: ReplyMessageRequest) {
  const res = await request.put<ApiResult<MessageDto>>(`/messages/${id}/reply`, payload)
  return res.data.data
}

