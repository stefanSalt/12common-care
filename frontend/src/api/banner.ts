import request, { type ApiResult } from '../utils/request'
import type { LongLike, PageResult } from './types'

export type BannerDto = {
  id: string
  title?: string
  imageFileId: string
  linkUrl?: string
  sortNo: number
  enabled: number
  createdAt?: string
}

export type CreateBannerRequest = {
  title?: string
  imageFileId: LongLike
  linkUrl?: string
  sortNo?: number
  enabled?: number
}

export type UpdateBannerRequest = {
  title?: string
  imageFileId?: LongLike
  linkUrl?: string
  sortNo?: number
  enabled?: number
}

export async function listPublicBanners(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<BannerDto>>>('/banners/public', { params: { current, size } })
  return res.data.data
}

export async function listAllBanners(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<BannerDto>>>('/banners', { params: { current, size } })
  return res.data.data
}

export async function createBanner(payload: CreateBannerRequest) {
  const res = await request.post<ApiResult<BannerDto>>('/banners', payload)
  return res.data.data
}

export async function updateBanner(id: string, payload: UpdateBannerRequest) {
  const res = await request.put<ApiResult<BannerDto>>(`/banners/${id}`, payload)
  return res.data.data
}

export async function deleteBanner(id: string) {
  const res = await request.delete<ApiResult<null>>(`/banners/${id}`)
  return res.data.data
}

