<template>
  <view class="page">
    <view class="bg-decoration">
      <view class="circle circle-1" />
      <view class="circle circle-2" />
      <view class="leaf leaf-1" />
      <view class="leaf leaf-2" />
    </view>

    <view v-if="isLoggedIn" class="content">
      <view class="profile-header">
        <view class="avatar-wrapper">
          <image
            class="avatar"
            :src="userInfo?.avatarUrl || '/static/default-avatar.png'"
            mode="aspectFill"
          />
        </view>
        <text class="nickname">{{ userInfo?.nickname || '用户' }}</text>
        <view class="identity-row">
          <text class="identity-code">ID: {{ userInfo?.identityCode }}</text>
          <view v-if="userInfo?.isMember" class="member-badge">
            <text class="member-text">会员</text>
          </view>
        </view>
      </view>

      <view class="stats-row">
        <view class="stat-item" @tap="goReservations">
          <text class="stat-num">{{ stats.reservations }}</text>
          <text class="stat-label">预约</text>
        </view>
        <view class="stat-item" @tap="goCoupons">
          <text class="stat-num">{{ stats.coupons }}</text>
          <text class="stat-label">卡券</text>
        </view>
        <view class="stat-item" @tap="goJournals">
          <text class="stat-num">{{ stats.journals }}</text>
          <text class="stat-label">动态</text>
        </view>
      </view>

      <view v-if="userInfo?.role === 'staff' || userInfo?.role === 'admin'" class="menu-section">
        <view class="section-header">
          <text class="section-title">⚡ 工作台</text>
        </view>
        <view class="section-card">
          <view class="menu-item" @tap="goTo('/pages/staff/scan/index')">
            <view class="menu-left">
              <view class="menu-icon menu-icon-scan">
                <text class="menu-icon-text">📷</text>
              </view>
              <text class="menu-label">扫码核销</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goTo('/pages/staff/reservations/index')">
            <view class="menu-left">
              <view class="menu-icon menu-icon-booking">
                <text class="menu-icon-text">📋</text>
              </view>
              <text class="menu-label">预约管理</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goTo('/pages/staff/orders/index')">
            <view class="menu-left">
              <view class="menu-icon menu-icon-order">
                <text class="menu-icon-text">📦</text>
              </view>
              <text class="menu-label">订单管理</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view v-if="userInfo?.role === 'admin'" class="menu-item" @tap="goTo('/pages/admin/dashboard/index')">
            <view class="menu-left">
              <view class="menu-icon menu-icon-admin">
                <text class="menu-icon-text">⚙️</text>
              </view>
              <text class="menu-label">管理后台</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="menu-section">
        <view class="section-header">
          <text class="section-title">📋 我的服务</text>
        </view>
        <view class="section-card">
          <view class="menu-item" @tap="goCoupons">
            <view class="menu-left">
              <view class="menu-icon menu-icon-coupon">
                <text class="menu-icon-text">🎫</text>
              </view>
              <text class="menu-label">我的卡包</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goReservations">
            <view class="menu-left">
              <view class="menu-icon menu-icon-reserve">
                <text class="menu-icon-text">📅</text>
              </view>
              <text class="menu-label">我的预约</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goJournals">
            <view class="menu-left">
              <view class="menu-icon menu-icon-journal">
                <text class="menu-icon-text">📝</text>
              </view>
              <text class="menu-label">朋友圈</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view v-if="!userInfo?.isMember" class="menu-item" @tap="goMembership">
            <view class="menu-left">
              <view class="menu-icon menu-icon-member">
                <text class="menu-icon-text">👑</text>
              </view>
              <view class="menu-label-row">
                <text class="menu-label">开通会员</text>
                <text class="menu-label-tip">享专属权益</text>
              </view>
            </view>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="logout-btn" @tap="logout">
        <text class="logout-btn-text">退出登录</text>
      </view>
    </view>

    <view v-else class="login-prompt">
      <view class="prompt-icon">👤</view>
      <text class="prompt-title">欢迎来到农家乐</text>
      <text class="prompt-desc">登录后可查看个人中心和享受更多服务</text>
      <view class="go-login-btn" @tap="goLogin">
        <text class="go-login-btn-text">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from "@/stores/auth";
import { membershipApi } from "@/api";

export default {
  data() {
    return {
      userInfo: null,
      stats: {
        reservations: 0,
        coupons: 0,
        journals: 0
      }
    };
  },
  computed: {
    isLoggedIn() {
      const store = useAuthStore();
      return store.isLoggedIn;
    }
  },
  onShow() {
    const store = useAuthStore();
    this.userInfo = store.userInfo;
  },
  methods: {
    goLogin() {
      uni.navigateTo({ url: "/pages/login/index" });
    },
    goTo(url) {
      uni.navigateTo({ url });
    },
    goCoupons() {
      uni.navigateTo({ url: "/pages/profile/coupons/index" });
    },
    goReservations() {
      uni.navigateTo({ url: "/pages/profile/reservations/index" });
    },
    goJournals() {
      uni.navigateTo({ url: "/pages/profile/journals/index" });
    },
    async goMembership() {
      try {
        await membershipApi.purchase();
        uni.showToast({ title: "会员开通成功", icon: "success" });
        const store = useAuthStore();
        await store.fetchUserInfo();
        this.userInfo = store.userInfo;
      } catch (e) {}
    },
    logout() {
      const store = useAuthStore();
      store.logout();
      this.userInfo = null;
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ECFDF5 0%, #F0FDF4 30%, #DCFCE7 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 500rpx;
  height: 500rpx;
  background: #22C55E;
  top: -120rpx;
  right: -160rpx;
}

.circle-2 {
  width: 360rpx;
  height: 360rpx;
  background: #86EFAC;
  bottom: -80rpx;
  left: -120rpx;
}

.leaf {
  position: absolute;
  width: 140rpx;
  height: 140rpx;
  border-radius: 0 100% 0 100%;
  opacity: 0.06;
  background: #15803D;
  transform: rotate(45deg);
}

.leaf-1 {
  top: 120rpx;
  right: 40rpx;
}

.leaf-2 {
  bottom: 180rpx;
  left: 30rpx;
  transform: rotate(225deg);
}

.content {
  position: relative;
  z-index: 1;
  padding: 32rpx;
  padding-bottom: 48rpx;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0 32rpx;
}

.avatar-wrapper {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  padding: 6rpx;
  background: linear-gradient(135deg, #15803D, #22C55E, #86EFAC);
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(21, 128, 61, 0.2);
}

.avatar {
  width: 148rpx;
  height: 148rpx;
  border-radius: 50%;
  background: #E5E7EB;
}

.nickname {
  font-size: 40rpx;
  font-weight: 700;
  color: #14532D;
  margin-bottom: 8rpx;
}

.identity-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.identity-code {
  font-size: 24rpx;
  color: #9CA3AF;
  letter-spacing: 2rpx;
}

.member-badge {
  background: linear-gradient(135deg, #CA8A04, #EAB308);
  padding: 4rpx 20rpx;
  border-radius: 20rpx;
}

.member-text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.stats-row {
  display: flex;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx 0;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  position: relative;
}

.stat-item::after {
  content: "";
  position: absolute;
  right: 0;
  top: 8rpx;
  bottom: 8rpx;
  width: 2rpx;
  background: #F3F4F6;
}

.stat-item:last-child::after {
  display: none;
}

.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #15803D;
}

.stat-label {
  font-size: 24rpx;
  color: #9CA3AF;
}

.menu-section {
  margin-bottom: 24rpx;
}

.section-header {
  padding: 0 8rpx 16rpx;
}

.section-title {
  font-size: 26rpx;
  color: #15803D;
  font-weight: 600;
}

.section-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
  transition: background 0.15s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #F9FAFB;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon-scan {
  background: #DBEAFE;
}

.menu-icon-booking {
  background: #E0E7FF;
}

.menu-icon-order {
  background: #FEF3C7;
}

.menu-icon-admin {
  background: #F3F4F6;
}

.menu-icon-coupon {
  background: #FCE7F3;
}

.menu-icon-reserve {
  background: #D1FAE5;
}

.menu-icon-journal {
  background: #FEF3C7;
}

.menu-icon-member {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
}

.menu-icon-text {
  font-size: 32rpx;
  line-height: 1;
}

.menu-label {
  font-size: 30rpx;
  color: #1F2937;
  font-weight: 500;
}

.menu-label-row {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.menu-label-tip {
  font-size: 22rpx;
  color: #CA8A04;
}

.menu-arrow {
  font-size: 32rpx;
  color: #D1D5DB;
}

.logout-btn {
  display: flex;
  justify-content: center;
  padding: 24rpx 0;
}

.logout-btn-text {
  font-size: 28rpx;
  color: #9CA3AF;
  padding: 16rpx 48rpx;
}

.logout-btn-text:active {
  color: #DC2626;
}

.login-prompt {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64rpx;
}

.prompt-icon {
  font-size: 96rpx;
  margin-bottom: 32rpx;
  opacity: 0.8;
}

.prompt-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #14532D;
  margin-bottom: 16rpx;
}

.prompt-desc {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-bottom: 48rpx;
  text-align: center;
  line-height: 1.6;
}

.go-login-btn {
  background: linear-gradient(135deg, #15803D, #22C55E);
  padding: 24rpx 96rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(21, 128, 61, 0.3);
}

.go-login-btn:active {
  transform: scale(0.98);
}

.go-login-btn-text {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
}
</style>