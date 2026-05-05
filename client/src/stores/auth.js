import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { getRoleHome } from '@/utils/tabBar'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isMember: (state) => state.userInfo?.is_member || false,
    role: (state) => state.userInfo?.role || 'customer',
    isStaff: (state) => state.userInfo?.role === 'staff',
    isAdmin: (state) => state.userInfo?.role === 'admin'
  },
  actions: {
    async login(phone, code) {
      const data = await authApi.login(phone, code)
      this.token = data.token
      this.userInfo = data.user
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data.user)
      this.navigateToRoleHome()
    },
    async adminLogin(phone, password) {
      const data = await authApi.adminLogin(phone, password)
      this.token = data.token
      this.userInfo = data.user
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data.user)
      this.navigateToRoleHome()
    },
    async register(phone, code) {
      const data = await authApi.register(phone, code)
      this.token = data.token
      this.userInfo = data.user
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data.user)
    },
    async fetchUserInfo() {
      const data = await authApi.getMe()
      this.userInfo = data
      uni.setStorageSync('userInfo', data)
    },
    navigateToRoleHome() {
      const home = getRoleHome(this.role)
      if (this.isAdmin) {
        uni.redirectTo({ url: home })
      } else {
        uni.switchTab({ url: home })
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.navigateTo({ url: '/pages/login/index' })
    }
  }
})
