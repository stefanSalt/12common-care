import request, { type ApiResult } from '../utils/request'

export type ActivitySignupRatioDto = {
  activityId: string
  activityTitle?: string
  signupCount: number
}

export async function getActivitySignupRatio(days = 7) {
  const res = await request.get<ApiResult<ActivitySignupRatioDto[]>>('/stats/activity-signup-ratio', { params: { days } })
  return res.data.data
}

