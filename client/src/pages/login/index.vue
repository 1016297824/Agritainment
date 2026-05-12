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
            <text class="logo-emoji">🏡</text>
          </view>
        </view>
        <text class="app-name">农家乐</text>
        <text class="app-slogan">田园生活 · 从这里开始</text>
      </view>

      <view class="login-card">
        <text class="card-title">欢迎回来</text>
        <text class="card-subtitle">使用手机号验证码登录您的账户</text>

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
              placeholder="请输入手机号"
              placeholder-class="input-placeholder"
            />
          </view>

          <view class="input-wrapper code-row">
            <view class="input-icon">
              <text class="icon-text">💬</text>
            </view>
            <input
              class="form-input code-input"
              v-model="code"
              type="number"
              maxlength="6"
              placeholder="请输入验证码"
              placeholder-class="input-placeholder"
            />
            <view
              class="sms-btn"
              :class="{ 'sms-btn--counting': countdown > 0 }"
              @tap="sendCode"
            >
              <text class="sms-btn-text">{{ codeText }}</text>
            </view>
          </view>

          <view class="agreement-row">
            <view
              class="checkbox"
              :class="{ 'checkbox--checked': agreed }"
              @tap="toggleAgree"
            >
              <text v-if="agreed" class="check-mark">✓</text>
            </view>
            <text class="agreement-text">
              已阅读并同意
              <text class="agreement-link">《用户协议》</text>
              和
              <text class="agreement-link">《隐私政策》</text>
            </text>
          </view>

          <view class="submit-btn" @tap="handleLogin">
            <text class="submit-btn-text">登录 / 注册</text>
          </view>

          <view class="divider">
            <view class="divider-line" />
            <text class="divider-text">其他登录方式</text>
            <view class="divider-line" />
          </view>

          <view class="social-login">
            <view class="social-item" @tap="handleWechatLogin">
              <text class="social-icon">💚</text>
              <text class="social-label">微信一键登录</text>
            </view>
          </view>

          <view class="admin-link" @tap="goAdminLogin">
            <text class="admin-link-text">管理员登录 →</text>
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
      code: "",
      codeText: "获取验证码",
      countdown: 0,
      timer: null,
      agreed: false
    };
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    async sendCode() {
      if (this.countdown > 0) return;
      if (!this.phone || this.phone.length !== 11) {
        uni.showToast({ title: "请输入11位手机号", icon: "none" });
        return;
      }
      try {
        await authApi.sendSmsCode(this.phone);
        uni.showToast({ title: "验证码已发送", icon: "success" });
        this.countdown = 60;
        this.codeText = `${this.countdown}s`;
        this.timer = setInterval(() => {
          this.countdown--;
          this.codeText =
            this.countdown > 0 ? `${this.countdown}s` : "获取验证码";
          if (this.countdown <= 0) clearInterval(this.timer);
        }, 1000);
      } catch (e) {
        uni.showToast({ title: "发送失败，请重试", icon: "none" });
      }
    },
    toggleAgree() {
      this.agreed = !this.agreed;
    },
    async handleLogin() {
      if (!this.phone || !this.code) {
        uni.showToast({ title: "请输入手机号和验证码", icon: "none" });
        return;
      }
      if (!this.agreed) {
        uni.showToast({ title: "请先阅读并同意用户协议", icon: "none" });
        return;
      }
      try {
        const store = useAuthStore();
        await store.login(this.phone, this.code);
      } catch (e) {
        try {
          const store = useAuthStore();
          await store.register(this.phone, this.code);
          store.navigateToRoleHome();
        } catch (e2) {
          uni.showToast({ title: "登录/注册失败，请重试", icon: "none" });
        }
      }
    },
    handleWechatLogin() {
      uni.showToast({ title: "微信登录功能开发中", icon: "none" });
    },
    goAdminLogin() {
      uni.navigateTo({ url: "/pages/admin/login/index" });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ecfdf5 0%, #f0fdf4 40%, #dcfce7 100%);
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
  background: #22c55e;
  top: -120rpx;
  right: -160rpx;
}

.circle-2 {
  width: 360rpx;
  height: 360rpx;
  background: #86efac;
  bottom: -80rpx;
  left: -120rpx;
}

.leaf {
  position: absolute;
  width: 140rpx;
  height: 140rpx;
  border-radius: 0 100% 0 100%;
  opacity: 0.08;
  background: #15803d;
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
  background: linear-gradient(135deg, #15803d, #22c55e);
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
  color: #14532d;
  letter-spacing: 8rpx;
  margin-bottom: 12rpx;
}

.app-slogan {
  font-size: 26rpx;
  color: #4ade80;
  font-weight: 500;
  letter-spacing: 4rpx;
}

.login-card {
  background: #ffffff;
  border-radius: 40rpx;
  padding: 48rpx 40rpx 40rpx;
  box-shadow:
    0 8rpx 40rpx rgba(0, 0, 0, 0.06),
    0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #14532d;
  display: block;
  margin-bottom: 8rpx;
}

.card-subtitle {
  font-size: 26rpx;
  color: #9ca3af;
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
  background: #f9fafb;
  border-radius: 24rpx;
  padding: 0 24rpx;
  border: 2rpx solid #f3f4f6;
  transition: border-color 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #22c55e;
  background: #ffffff;
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
  color: #14532d;
  background: transparent;
}

.input-placeholder {
  color: #d1d5db;
  font-size: 28rpx;
}

.code-row {
  padding-right: 12rpx;
}

.code-input {
  flex: 1;
}

.sms-btn {
  width: 180rpx;
  height: 68rpx;
  background: linear-gradient(135deg, #15803d, #22c55e);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: opacity 0.2s ease;
}

.sms-btn--counting {
  background: #e5e7eb;
}

.sms-btn--counting .sms-btn-text {
  color: #9ca3af;
}

.sms-btn-text {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 8rpx 4rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  min-width: 36rpx;
  border: 3rpx solid #d1d5db;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4rpx;
  transition: all 0.2s ease;
}

.checkbox--checked {
  background: #15803d;
  border-color: #15803d;
}

.check-mark {
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
}

.agreement-text {
  font-size: 24rpx;
  color: #9ca3af;
  line-height: 1.6;
}

.agreement-link {
  color: #15803d;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #ca8a04, #eab308);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(202, 138, 4, 0.3);
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(202, 138, 4, 0.25);
}

.submit-btn-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
}

.divider {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 8rpx;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: #f3f4f6;
}

.divider-text {
  font-size: 24rpx;
  color: #d1d5db;
  flex-shrink: 0;
}

.social-login {
  display: flex;
  justify-content: center;
}

.social-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx 48rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 24rpx;
  transition:
    border-color 0.2s ease,
    background 0.2s ease;
}

.social-item:active {
  border-color: #22c55e;
  background: #f0fdf4;
}

.social-icon {
  font-size: 36rpx;
  line-height: 1;
}

.social-label {
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.admin-link {
  text-align: center;
  margin-top: 8rpx;
  padding: 12rpx 0;
}

.admin-link-text {
  font-size: 26rpx;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.admin-link-text:active {
  color: #15803d;
}
</style>
