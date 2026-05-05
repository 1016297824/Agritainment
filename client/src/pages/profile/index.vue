<template>
  <view class="page">
    <view v-if="isLoggedIn" class="profile">
      <view class="profile-header">
        <image class="avatar" :src="userInfo?.avatar_url || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="user-info">
          <text class="nickname">{{ userInfo?.nickname || '用户' }}</text>
          <text class="identity-code">身份码: {{ userInfo?.identity_code }}</text>
          <view v-if="userInfo?.is_member" class="member-badge">
            <text class="member-text">会员</text>
          </view>
        </view>
      </view>

      <view class="menu-section" v-if="userInfo?.role === 'staff' || userInfo?.role === 'admin'">
        <view class="section-title">工作台</view>
        <view class="menu-item" @tap="goTo('/pages/staff/scan/index')">
          <text class="menu-label">扫码核销</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goTo('/pages/staff/reservations/index')">
          <text class="menu-label">预约管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goTo('/pages/staff/orders/index')">
          <text class="menu-label">订单管理</text>
          <text class="menu-arrow">›</text>
        </view>
        <view v-if="userInfo?.role === 'admin'" class="menu-item" @tap="goTo('/pages/admin/dashboard/index')">
          <text class="menu-label">管理后台</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @tap="goCoupons">
          <text class="menu-label">我的卡包</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goReservations">
          <text class="menu-label">我的预约</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goJournals">
          <text class="menu-label">游玩日志</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goMembership" v-if="!userInfo?.is_member">
          <text class="menu-label">开通会员</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="logout">
          <text class="menu-label logout-label">退出登录</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <view v-else class="login-prompt">
      <text class="prompt-text">登录后享受更多服务</text>
      <view class="login-btn" @tap="goLogin">
        <text class="login-btn-text">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '@/stores/auth'
import { membershipApi } from '@/api'

export default {
  data() {
    return {
      userInfo: null
    }
  },
  computed: {
    isLoggedIn() {
      const store = useAuthStore()
      return store.isLoggedIn
    }
  },
  onShow() {
    const store = useAuthStore()
    this.userInfo = store.userInfo
  },
  methods: {
    goLogin() { uni.navigateTo({ url: '/pages/login/index' }) },
    goTo(url) { uni.navigateTo({ url }) },
    goCoupons() { uni.navigateTo({ url: '/pages/profile/coupons/index' }) },
    goReservations() { uni.navigateTo({ url: '/pages/profile/reservations/index' }) },
    goJournals() { uni.navigateTo({ url: '/pages/profile/journals/index' }) },
    async goMembership() {
      try {
        await membershipApi.purchase()
        uni.showToast({ title: '会员开通成功', icon: 'success' })
        const store = useAuthStore()
        await store.fetchUserInfo()
        this.userInfo = store.userInfo
      } catch (e) {}
    },
    logout() {
      const store = useAuthStore()
      store.logout()
      this.userInfo = null
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); }
.profile { padding: 32rpx; }
.profile-header { display: flex; align-items: center; gap: 24rpx; padding: 32rpx; background: var(--color-surface); border-radius: var(--radius-lg); box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06); }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; background: #E5E7EB; }
.user-info { flex: 1; }
.nickname { font-size: 32rpx; font-weight: 600; color: var(--color-text); display: block; }
.identity-code { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 4rpx; }
.member-badge { display: inline-block; margin-top: 8rpx; background: #FEF3C7; padding: 4rpx 16rpx; border-radius: 8rpx; }
.member-text { font-size: 22rpx; color: #92400E; font-weight: 500; }
.menu-section { margin-top: 32rpx; background: var(--color-surface); border-radius: var(--radius-lg); overflow: hidden; }
.section-title { font-size: 26rpx; color: var(--color-muted); padding: 16rpx 32rpx 0; font-weight: 600; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 32rpx; border-bottom: 1rpx solid var(--color-border); }
.menu-item:last-child { border-bottom: none; }
.menu-label { font-size: 28rpx; color: var(--color-text); }
.logout-label { color: var(--color-error); }
.menu-arrow { font-size: 32rpx; color: var(--color-muted); }
.login-prompt { padding: 120rpx 32rpx; text-align: center; display: flex; flex-direction: column; align-items: center; }
.prompt-text { font-size: 28rpx; color: #6B7280; display: block; margin-bottom: 32rpx; }
.login-btn { background: #15803D; padding: 24rpx 64rpx; border-radius: 16rpx; }
.login-btn-text { color: #fff; font-size: 30rpx; font-weight: 600; }
</style>
