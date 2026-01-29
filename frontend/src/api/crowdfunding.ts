import request, { type ApiResult } from '../utils/request'
import type { PageResult } from './types'

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

