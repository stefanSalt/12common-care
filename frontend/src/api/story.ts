import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'

export type StoryDto = {
  id: string
  title: string
  coverFileId: string
  enabled: number
  createdAt?: string
}

export type StoryDetailDto = StoryDto & {
  content: string
}

export type CreateStoryRequest = {
  title: string
  coverFileId: string
  content: string
  enabled?: number
}

export type UpdateStoryRequest = Partial<CreateStoryRequest>

export async function listPublicStories(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<StoryDto>>>('/stories/public', { params: { current, size } })
  return res.data.data
}

export async function getPublicStoryDetail(id: string) {
  const res = await request.get<ApiResult<StoryDetailDto>>(`/stories/${id}/public`)
  return res.data.data
}

export async function listAllStories(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<StoryDto>>>('/stories', { params: { current, size } })
  return res.data.data
}

export async function getStoryDetail(id: string) {
  const res = await request.get<ApiResult<StoryDetailDto>>(`/stories/${id}`)
  return res.data.data
}

export async function createStory(payload: CreateStoryRequest) {
  const res = await request.post<ApiResult<StoryDetailDto>>('/stories', payload)
  return res.data.data
}

export async function updateStory(id: string, payload: UpdateStoryRequest) {
  const res = await request.put<ApiResult<StoryDetailDto>>(`/stories/${id}`, payload)
  return res.data.data
}

export async function deleteStory(id: string) {
  const res = await request.delete<ApiResult<void>>(`/stories/${id}`)
  return res.data.data
}

