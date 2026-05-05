<template>
  <view class="page">
    <view class="login-card">
      <text class="title">管理后台</text>
      <text class="subtitle">管理员登录</text>

      <view class="form">
        <view class="input-group">
          <input class="input" v-model="phone" type="number" maxlength="11" placeholder="管理员手机号" />
        </view>
        <view class="input-group">
          <input class="input" v-model="password" password placeholder="密码" />
        </view>
        <view class="login-btn" @tap="handleLogin">
          <text class="login-btn-text">登录</text>
        </view>
      </view>

      <view class="back-link" @tap="goBack">
        <text class="back-text">返回用户登录</text>
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
      password: ''
    }
  },
  methods: {
    async handleLogin() {
      if (!this.phone || !this.password) {
        uni.showToast({ title: '请输入手机号和密码', icon: 'none' })
        return
      }
      try {
        const store = useAuthStore()
        await store.adminLogin(this.phone, this.password)
      } catch (e) {
        uni.showToast({ title: e.message || '登录失败', icon: 'none' })
      }
    },
    goBack() {
      uni.navigateBack({ delta: 1 })
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
.login-btn { width: 100%; height: 88rpx; background: #15803D; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; margin-top: 16rpx; }
.login-btn-text { color: #fff; font-size: 30rpx; font-weight: 600; }
.back-link { text-align: center; margin-top: 32rpx; }
.back-text { font-size: 26rpx; color: #6B7280; }
</style>
