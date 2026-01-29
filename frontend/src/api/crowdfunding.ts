import request, { type ApiResult } from '../utils/request'
import type { LongLike, PageResult } from './types'

export type CrowdfundingProjectDto = {
  id: string
  title: string
  coverFileId: string
  targetAmount: number
  raisedAmount: number
  startTime?: string
  endTime: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  enabled: number
  createdBy?: string
  createdAt?: string
}

export type CrowdfundingProjectDetailDto = CrowdfundingProjectDto & {
  content: string
}

export type CrowdfundingDonationDto = {
  id: string
  projectId: string
  amount: number
  donorName: string
  createdAt?: string
}

export type CrowdfundingPublicDetailDto = {
  project: CrowdfundingProjectDetailDto
  latestDonations: CrowdfundingDonationDto[]
}

export type CreateCrowdfundingDonationRequest = {
  amount: number
  anonymous?: boolean
  remark?: string
}

export type CrowdfundingDonationRecordDto = {
  id: string
  projectId: string
  userId: string
  amount: number
  isAnonymous: number
  remark?: string
  createdAt?: string
  projectTitle?: string
  projectCoverFileId?: string
  username?: string
}

export type ManageCrowdfundingProjectRequest = {
  title?: string
  coverFileId?: LongLike
  content?: string
  targetAmount?: number
  endTime?: string
  enabled?: number
}

export async function listPublicCrowdfundingProjects(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<CrowdfundingProjectDto>>>('/crowdfunding/public', {
    params: { current, size },
  })
  return res.data.data
}

export async function getPublicCrowdfundingDetail(id: string) {
  const res = await request.get<ApiResult<CrowdfundingPublicDetailDto>>(`/crowdfunding/${id}/public`)
  return res.data.data
}

export async function donateToCrowdfunding(id: string, payload: CreateCrowdfundingDonationRequest) {
  const res = await request.post<ApiResult<CrowdfundingDonationDto>>(`/crowdfunding/${id}/donations`, payload)
  return res.data.data
}

export async function listMyCrowdfundingDonations(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<CrowdfundingDonationRecordDto>>>('/crowdfunding/my/donations', {
    params: { current, size },
  })
  return res.data.data
}

export async function listAllCrowdfundingProjects(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<CrowdfundingProjectDto>>>('/crowdfunding', {
    params: { current, size },
  })
  return res.data.data
}

export async function getCrowdfundingProjectDetail(id: string) {
  const res = await request.get<ApiResult<CrowdfundingProjectDetailDto>>(`/crowdfunding/${id}`)
  return res.data.data
}

export async function reviewCrowdfundingProject(id: string, action: 'APPROVE' | 'REJECT') {
  const res = await request.put<ApiResult<null>>(`/crowdfunding/${id}/review`, { action })
  return res.data.data
}

export async function manageUpdateCrowdfundingProject(id: string, payload: ManageCrowdfundingProjectRequest) {
  const res = await request.put<ApiResult<CrowdfundingProjectDetailDto>>(`/crowdfunding/${id}/manage`, payload)
  return res.data.data
}

export async function deleteCrowdfundingProject(id: string) {
  const res = await request.delete<ApiResult<null>>(`/crowdfunding/${id}`)
  return res.data.data
}

export async function listAllCrowdfundingDonations(current = 1, size = 10, projectId?: string) {
  const res = await request.get<ApiResult<PageResult<CrowdfundingDonationRecordDto>>>('/crowdfunding/donations', {
    params: { current, size, projectId },
  })
  return res.data.data
}
