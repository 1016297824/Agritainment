<template>
  <view class="page">
    <view class="login-card">
      <text class="title">农家乐</text>
      <text class="subtitle">手机号验证登录</text>

      <view class="form">
        <view class="input-group">
          <input class="input" v-model="phone" type="number" maxlength="11" placeholder="请输入手机号" />
        </view>
        <view class="input-group code-group">
          <input class="input code-input" v-model="code" type="number" maxlength="6" placeholder="验证码" />
          <view class="code-btn" @tap="sendCode">
            <text class="code-btn-text">{{ codeText }}</text>
          </view>
        </view>
        <view class="login-btn" @tap="handleLogin">
          <text class="login-btn-text">登录 / 注册</text>
        </view>

        <view class="admin-toggle" @tap="showAdmin = !showAdmin">
          <text class="admin-toggle-text">{{ showAdmin ? '收起管理员登录' : '管理员登录' }}</text>
        </view>

        <view v-if="showAdmin" class="admin-form">
          <view class="input-group">
            <input class="input" v-model="adminPhone" type="number" maxlength="11" placeholder="管理员手机号" />
          </view>
          <view class="input-group">
            <input class="input" v-model="adminPassword" type="text" placeholder="密码" />
          </view>
          <view class="login-btn admin-login-btn" @tap="handleAdminLogin">
            <text class="login-btn-text">管理员登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

export default {
  data() {
    return {
      phone: '',
      code: '',
      codeText: '获取验证码',
      countdown: 0,
      showAdmin: false,
      adminPhone: '',
      adminPassword: ''
    }
  },
  methods: {
    async sendCode() {
      if (this.countdown > 0) return
      if (!this.phone || this.phone.length !== 11) {
        uni.showToast({ title: '请输入11位手机号', icon: 'none' })
        return
      }
      try {
        await authApi.sendSmsCode(this.phone)
        uni.showToast({ title: '验证码已发送', icon: 'success' })
        this.countdown = 60
        this.codeText = `${this.countdown}s`
        this.timer = setInterval(() => {
          this.countdown--
          this.codeText = this.countdown > 0 ? `${this.countdown}s` : '获取验证码'
          if (this.countdown <= 0) clearInterval(this.timer)
        }, 1000)
      } catch (e) {
        uni.showToast({ title: '发送失败，请重试', icon: 'none' })
      }
    },
    async handleLogin() {
      if (!this.phone || !this.code) {
        uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
        return
      }
      try {
        const store = useAuthStore()
        await store.login(this.phone, this.code)
        uni.switchTab({ url: '/pages/index/index' })
      } catch (e) {
        try {
          const store = useAuthStore()
          await store.register(this.phone, this.code)
          uni.switchTab({ url: '/pages/index/index' })
        } catch (e2) {}
      }
    },
    async handleAdminLogin() {
      if (!this.adminPhone || !this.adminPassword) {
        uni.showToast({ title: '请输入手机号和密码', icon: 'none' })
        return
      }
      try {
        const store = useAuthStore()
        await store.adminLogin(this.adminPhone, this.adminPassword)
      } catch (e) {
        uni.showToast({ title: e.message || '登录失败', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F0FDF4; display: flex; align-items: center; justify-content: center; }
.login-card { width: 600rpx; background: #FFFFFF; border-radius: 32rpx; padding: 64rpx 48rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08); }
.title { font-size: 48rpx; font-weight: 700; color: #15803D; display: block; text-align: center; }
.subtitle { font-size: 28rpx; color: #6B7280; display: block; text-align: center; margin-top: 8rpx; margin-bottom: 48rpx; }
.form { display: flex; flex-direction: column; gap: 24rpx; }
.input-group { width: 100%; }
.input { width: 100%; height: 88rpx; padding: 0 24rpx; border: 2rpx solid #D1D5DB; border-radius: 16rpx; font-size: 28rpx; background: #fff; box-sizing: border-box; }
.code-group { display: flex; gap: 16rpx; }
.code-input { flex: 1; }
.code-btn { width: 200rpx; height: 88rpx; background: #15803D; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; }
.code-btn-text { color: #fff; font-size: 26rpx; }
.login-btn { width: 100%; height: 88rpx; background: #CA8A04; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; margin-top: 16rpx; }
.login-btn-text { color: #fff; font-size: 30rpx; font-weight: 600; }
.admin-toggle { text-align: center; margin-top: 24rpx; }
.admin-toggle-text { font-size: 26rpx; color: #6B7280; }
.admin-form { margin-top: 16rpx; display: flex; flex-direction: column; gap: 24rpx; }
.admin-login-btn { background: #15803D; }
</style>
