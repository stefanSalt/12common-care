import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'

export type CommentDto = {
  id: string
  targetType: string
  targetId: string
  userId: string
  content: string
  createdAt?: string
  username?: string
  userNickname?: string
  storyTitle?: string
}

export type CreateCommentRequest = {
  targetType: 'STORY'
  targetId: string
  content: string
}

export async function listPublicComments(targetType: string, targetId: string, current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<CommentDto>>>('/comments/public', {
    params: { targetType, targetId, current, size },
  })
  return res.data.data
}

export async function createComment(payload: CreateCommentRequest) {
  const res = await request.post<ApiResult<CommentDto>>('/comments', payload)
  return res.data.data
}

export async function listMyComments(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<CommentDto>>>('/comments/my', { params: { current, size } })
  return res.data.data
}

export async function deleteMyComment(id: string) {
  const res = await request.delete<ApiResult<void>>(`/comments/${id}`)
  return res.data.data
}

export async function listAllComments(
  current = 1,
  size = 10,
  opts?: { targetType?: string; targetId?: string; userId?: string },
) {
  const res = await request.get<ApiResult<PageResult<CommentDto>>>('/comments', {
    params: { current, size, ...(opts ?? {}) },
  })
  return res.data.data
}

export async function deleteCommentAdmin(id: string) {
  const res = await request.delete<ApiResult<void>>(`/comments/${id}/manage`)
  return res.data.data
}

