<template>
  <view class="page">
    <view class="bg-decoration">
      <view class="circle circle-1" />
      <view class="circle circle-2" />
      <view class="leaf leaf-1" />
      <view class="leaf leaf-2" />
    </view>

    <view class="content">
      <view class="brand-area">
        <view class="logo-wrapper">
          <view class="logo-icon">
            <text class="logo-emoji">⚙️</text>
          </view>
        </view>
        <text class="app-name">管理后台</text>
        <text class="app-slogan">管理员身份验证</text>
      </view>

      <view class="login-card">
        <text class="card-title">管理员登录</text>
        <text class="card-subtitle">请输入管理员账户信息</text>

        <view class="form">
          <view class="input-wrapper">
            <view class="input-icon">
              <text class="icon-text">📱</text>
            </view>
            <input
              class="form-input"
              v-model="phone"
              type="number"
              maxlength="11"
              placeholder="管理员手机号"
              placeholder-class="input-placeholder"
            />
          </view>

          <view class="input-wrapper">
            <view class="input-icon">
              <text class="icon-text">🔒</text>
            </view>
            <input
              class="form-input"
              v-model="password"
              password
              placeholder="请输入密码"
              placeholder-class="input-placeholder"
            />
          </view>

          <view class="submit-btn" @tap="handleLogin">
            <text class="submit-btn-text">登 录</text>
          </view>

          <view class="back-link" @tap="goBack">
            <text class="back-text">← 返回用户登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { authApi } from "@/api";
import { useAuthStore } from "@/stores/auth";

export default {
  data() {
    return {
      phone: "",
      password: ""
    };
  },
  methods: {
    async handleLogin() {
      if (!this.phone || !this.password) {
        uni.showToast({ title: "请输入手机号和密码", icon: "none" });
        return;
      }
      try {
        const store = useAuthStore();
        await store.adminLogin(this.phone, this.password);
      } catch (e) {
        uni.showToast({
          title: e.message || "登录失败",
          icon: "none"
        });
      }
    },
    goBack() {
      uni.navigateBack({ delta: 1 });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ECFDF5 0%, #F0FDF4 40%, #DCFCE7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
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
  opacity: 0.12;
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
  opacity: 0.08;
  background: #15803D;
  transform: rotate(45deg);
}

.leaf-1 {
  top: 180rpx;
  left: 60rpx;
}

.leaf-2 {
  bottom: 260rpx;
  right: 40rpx;
  transform: rotate(225deg);
}

.content {
  width: 100%;
  padding: 0 48rpx;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.brand-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80rpx;
  padding-bottom: 48rpx;
}

.logo-wrapper {
  margin-bottom: 24rpx;
}

.logo-icon {
  width: 140rpx;
  height: 140rpx;
  background: linear-gradient(135deg, #15803D, #22C55E);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(21, 128, 61, 0.25);
}

.logo-emoji {
  font-size: 64rpx;
  line-height: 1;
}

.app-name {
  font-size: 52rpx;
  font-weight: 800;
  color: #14532D;
  letter-spacing: 8rpx;
  margin-bottom: 12rpx;
}

.app-slogan {
  font-size: 26rpx;
  color: #4ADE80;
  font-weight: 500;
  letter-spacing: 4rpx;
}

.login-card {
  background: #FFFFFF;
  border-radius: 40rpx;
  padding: 48rpx 40rpx 40rpx;
  box-shadow:
    0 8rpx 40rpx rgba(0, 0, 0, 0.06),
    0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #14532D;
  display: block;
  margin-bottom: 8rpx;
}

.card-subtitle {
  font-size: 26rpx;
  color: #9CA3AF;
  display: block;
  margin-bottom: 40rpx;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  height: 96rpx;
  background: #F9FAFB;
  border-radius: 24rpx;
  padding: 0 24rpx;
  border: 2rpx solid #F3F4F6;
  transition: border-color 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #22C55E;
  background: #FFFFFF;
  box-shadow: 0 0 0 6rpx rgba(34, 197, 94, 0.08);
}

.input-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.icon-text {
  font-size: 32rpx;
  line-height: 1;
}

.form-input {
  flex: 1;
  height: 96rpx;
  font-size: 28rpx;
  color: #14532D;
  background: transparent;
}

.input-placeholder {
  color: #D1D5DB;
  font-size: 28rpx;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #15803D, #22C55E);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(21, 128, 61, 0.3);
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
  margin-top: 8rpx;
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(21, 128, 61, 0.25);
}

.submit-btn-text {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
}

.back-link {
  text-align: center;
  margin-top: 8rpx;
  padding: 12rpx 0;
}

.back-text {
  font-size: 26rpx;
  color: #9CA3AF;
  transition: color 0.2s ease;
}

.back-text:active {
  color: #15803D;
}
</style>