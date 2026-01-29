import request, { type ApiResult } from '../utils/request'
import type { LongLike, PageResult } from './types'

export type ActivityDto = {
  id: string
  title: string
  coverFileId: string
  address?: string
  startTime: string
  endTime: string
  signupEnabled: number
  donateEnabled: number
  maxParticipants?: number
  donationTarget?: number
  donatedAmount?: number
  enabled: number
  createdAt?: string
}

export type ActivityDetailDto = ActivityDto & {
  content: string
}

export type CreateActivityRequest = {
  title: string
  coverFileId: LongLike
  content: string
  address?: string
  startTime: string
  endTime: string
  signupEnabled?: number
  donateEnabled?: number
  maxParticipants?: number
  donationTarget?: number
  enabled?: number
}

export type UpdateActivityRequest = Partial<CreateActivityRequest>

export type ActivitySignupDto = {
  id: string
  activityId: string
  userId: string
  status: 'SIGNED' | 'CANCELED' | 'CHECKED_IN'
  signedAt?: string
  canceledAt?: string
  checkedInAt?: string
  activityTitle?: string
  activityCoverFileId?: string
  activityStartTime?: string
  activityEndTime?: string
  username?: string
}

export type ActivityDonationDto = {
  id: string
  activityId: string
  userId: string
  amount: number
  remark?: string
  createdAt?: string
  activityTitle?: string
  username?: string
}

export type CreateActivityDonationRequest = {
  amount: number
  remark?: string
}

export type ActivityFavoriteDto = {
  id: string
  activityId: string
  userId: string
  createdAt?: string
  activityTitle?: string
  activityCoverFileId?: string
  username?: string
}

export type MyActivityStateDto = {
  signupStatus: 'NONE' | 'SIGNED' | 'CANCELED' | 'CHECKED_IN'
  favorited: boolean
}

export async function listPublicActivities(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<ActivityDto>>>('/activities/public', { params: { current, size } })
  return res.data.data
}

export async function getPublicActivityDetail(id: string) {
  const res = await request.get<ApiResult<ActivityDetailDto>>(`/activities/${id}/public`)
  return res.data.data
}

export async function listAllActivities(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<ActivityDto>>>('/activities', { params: { current, size } })
  return res.data.data
}

export async function getActivityDetail(id: string) {
  const res = await request.get<ApiResult<ActivityDetailDto>>(`/activities/${id}`)
  return res.data.data
}

export async function createActivity(payload: CreateActivityRequest) {
  const res = await request.post<ApiResult<ActivityDetailDto>>('/activities', payload)
  return res.data.data
}

export async function updateActivity(id: string, payload: UpdateActivityRequest) {
  const res = await request.put<ApiResult<ActivityDetailDto>>(`/activities/${id}`, payload)
  return res.data.data
}

export async function deleteActivity(id: string) {
  const res = await request.delete<ApiResult<null>>(`/activities/${id}`)
  return res.data.data
}

export async function getMyActivityState(id: string) {
  const res = await request.get<ApiResult<MyActivityStateDto>>(`/activities/${id}/my-state`)
  return res.data.data
}

export async function signupActivity(id: string) {
  const res = await request.post<ApiResult<ActivitySignupDto>>(`/activities/${id}/signup`)
  return res.data.data
}

export async function cancelActivitySignup(id: string) {
  const res = await request.put<ApiResult<ActivitySignupDto>>(`/activities/${id}/signup/cancel`)
  return res.data.data
}

export async function checkInActivity(id: string) {
  const res = await request.put<ApiResult<ActivitySignupDto>>(`/activities/${id}/check-in`)
  return res.data.data
}

export async function donateToActivity(id: string, payload: CreateActivityDonationRequest) {
  const res = await request.post<ApiResult<ActivityDonationDto>>(`/activities/${id}/donations`, payload)
  return res.data.data
}

export async function favoriteActivity(id: string) {
  const res = await request.post<ApiResult<ActivityFavoriteDto>>(`/activities/${id}/favorite`)
  return res.data.data
}

export async function unfavoriteActivity(id: string) {
  const res = await request.delete<ApiResult<null>>(`/activities/${id}/favorite`)
  return res.data.data
}

export async function listMyActivitySignups(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<ActivitySignupDto>>>('/activities/my/signups', {
    params: { current, size },
  })
  return res.data.data
}

export async function listMyActivityDonations(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<ActivityDonationDto>>>('/activities/my/donations', {
    params: { current, size },
  })
  return res.data.data
}

export async function listMyActivityFavorites(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<ActivityFavoriteDto>>>('/activities/my/favorites', {
    params: { current, size },
  })
  return res.data.data
}

export async function listActivitySignups(current = 1, size = 10, activityId?: string) {
  const res = await request.get<ApiResult<PageResult<ActivitySignupDto>>>('/activities/signups', {
    params: { current, size, activityId },
  })
  return res.data.data
}

export async function listActivityDonations(current = 1, size = 10, activityId?: string) {
  const res = await request.get<ApiResult<PageResult<ActivityDonationDto>>>('/activities/donations', {
    params: { current, size, activityId },
  })
  return res.data.data
}

export async function listActivityFavorites(current = 1, size = 10, activityId?: string) {
  const res = await request.get<ApiResult<PageResult<ActivityFavoriteDto>>>('/activities/favorites', {
    params: { current, size, activityId },
  })
  return res.data.data
}

