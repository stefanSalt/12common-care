import { defineStore } from 'pinia'
import { storage } from '../utils/storage'

export interface RoleDto {
  id: number
  code: string
  name: string
}

export interface UserDto {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  status?: number
  roles: RoleDto[]
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: storage.getString('token') ?? '',
    refreshToken: storage.getString('refreshToken') ?? '',
    user: storage.getJson<UserDto>('user'),
  }),
  getters: {
    permissions: (state): string[] => state.user?.permissions ?? [],
  },
  actions: {
    setTokens(token: string, refreshToken: string) {
      this.token = token
      this.refreshToken = refreshToken
      storage.setString('token', token)
      storage.setString('refreshToken', refreshToken)
    },
    setUser(user: UserDto) {
      this.user = user
      storage.setJson('user', user)
    },
    clear() {
      this.token = ''
      this.refreshToken = ''
      this.user = null
      storage.remove('token')
      storage.remove('refreshToken')
      storage.remove('user')
    },
  },
})

