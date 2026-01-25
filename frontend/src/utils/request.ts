import axios, { AxiosError, type AxiosInstance } from 'axios'
import { useUserStore } from '../stores/user'

type ApiResult<T> = { code: number; message: string; data: T }

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

let refreshPromise: Promise<void> | null = null

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (token) {
    config.headers = config.headers ?? {}
    if (!config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResult<unknown> | undefined
    if (data && typeof data.code === 'number' && data.code !== 0) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  async (error: AxiosError) => {
    const userStore = useUserStore()
    const status = error.response?.status
    const config = error.config as any

    const isRefreshCall = typeof config?.url === 'string' && config.url.includes('/auth/refresh')

    if (status === 401 && !config?._retry && userStore.refreshToken && !isRefreshCall) {
      config._retry = true

      if (!refreshPromise) {
        refreshPromise = (async () => {
          try {
            const res = await request.post<ApiResult<{ token: string; refreshToken: string }>>('/auth/refresh', {
              refreshToken: userStore.refreshToken,
            })
            const { token, refreshToken } = res.data.data
            userStore.setTokens(token, refreshToken)
          } catch {
            userStore.clear()
            window.location.href = '/login'
          } finally {
            refreshPromise = null
          }
        })()
      }

      await refreshPromise

      // Retry with the (possibly) new token.
      const token = userStore.token
      if (token) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
      }
      return request(config)
    }

    return Promise.reject(error)
  },
)

export type { ApiResult }
export default request
