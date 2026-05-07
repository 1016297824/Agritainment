import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { getRoleHome, isTabBarPage } from '@/utils/tabBar'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isMember: (state) => state.userInfo?.isMember || false,
    role: (state) => state.userInfo?.role || 'customer',
    isStaff: (state) => state.userInfo?.role === 'staff',
    isAdmin: (state) => state.userInfo?.role === 'admin',
    userId: (state) => state.userInfo?.id
  },
  actions: {
    async ensureUserInfo() {
      if (this.token && !this.userInfo) {
        await this.fetchUserInfo()
      }
    },
    async login(phone, code) {
      const data = await authApi.login(phone, code)
      this.token = data.token
      uni.setStorageSync('token', data.token)
      await this.fetchUserInfo()
      this.bindWxOpenid()
      this.navigateToRoleHome()
    },
    async adminLogin(phone, password) {
      const data = await authApi.adminLogin(phone, password)
      this.token = data.token
      uni.setStorageSync('token', data.token)
      await this.fetchUserInfo()
      this.navigateToRoleHome()
    },
    async register(phone, code) {
      const data = await authApi.register(phone, code)
      this.token = data.token
      uni.setStorageSync('token', data.token)
      await this.fetchUserInfo()
      this.bindWxOpenid()
    },
    async fetchUserInfo() {
      const data = await authApi.getMe()
      this.userInfo = data
      uni.setStorageSync('userInfo', data)
    },
    navigateToRoleHome() {
      const home = getRoleHome(this.role)
      if (isTabBarPage(home)) {
        uni.switchTab({ url: home })
      } else {
        uni.reLaunch({ url: home })
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.navigateTo({ url: '/pages/login/index' })
    },
    async bindWxOpenid() {
      try {
        const [err, loginRes] = await uni.login({ provider: 'weixin' })
        if (err || !loginRes.code) return
        const userId = this.userInfo?.id
        if (!userId) return
        await authApi.wxLogin(loginRes.code, userId)
      } catch (e) { }
    }
  }
})
