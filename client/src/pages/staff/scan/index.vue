<template>
  <view class="scan-page">
    <view class="scan-header">
      <text class="scan-title">扫码核销</text>
      <text class="scan-desc">扫描客户卡券二维码进行核销</text>
    </view>

    <view class="scan-area" @tap="startScan">
      <view class="scan-icon">📷</view>
      <text class="scan-text">点击扫码</text>
    </view>

    <view v-if="result" class="result-card">
      <view class="result-status" :class="result.valid ? 'success' : 'error'">
        {{ result.valid ? '✓ 核销成功' : '✗ 核销失败' }}
      </view>
      <view v-if="result.valid && result.coupon" class="result-info">
        <text class="result-name">{{ result.coupon.Product?.name || '卡券' }}</text>
        <text class="result-code">编号: {{ result.coupon.code }}</text>
      </view>
      <text class="result-msg">{{ resultMsg }}</text>
    </view>

    <view class="manual-input">
      <text class="manual-label">手动输入卡券编号</text>
      <view class="manual-row">
        <input v-model="couponCode" class="manual-input-field" placeholder="输入12位卡券编号" />
        <button class="manual-btn" @tap="verifyManual">核销</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { couponApi } from '@/api'

const couponCode = ref('')
const result = ref(null)
const resultMsg = ref('')

const startScan = () => {
  uni.scanCode({
    onlyFromCamera: false,
    success: async (res) => {
      const code = res.result.replace('COUPON_', '').replace('PLOT_', '')
      await verifyCoupon(code)
    },
    fail: () => {
      uni.showToast({ title: '扫码取消', icon: 'none' })
    }
  })
}

const verifyManual = async () => {
  if (!couponCode.value) {
    uni.showToast({ title: '请输入卡券编号', icon: 'none' })
    return
  }
  await verifyCoupon(couponCode.value)
}

const verifyCoupon = async (code) => {
  try {
    const data = await couponApi.verify(code)
    result.value = data
    resultMsg.value = data.valid ? '卡券已成功核销' : '卡券无效或已使用'
    couponCode.value = ''
  } catch (err) {
    result.value = { valid: false }
    resultMsg.value = err.message || '核销失败'
  }
}
</script>

<style scoped>
.scan-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.scan-header { text-align: center; margin-bottom: 48rpx; }
.scan-title { font-size: 40rpx; font-weight: 700; color: #15803D; display: block; }
.scan-desc { font-size: 28rpx; color: #6B7280; margin-top: 8rpx; display: block; }
.scan-area { background: #fff; border-radius: 24rpx; padding: 80rpx 0; text-align: center; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06); }
.scan-icon { font-size: 80rpx; }
.scan-text { font-size: 30rpx; color: #15803D; margin-top: 16rpx; display: block; }
.result-card { background: #fff; border-radius: 16rpx; padding: 32rpx; margin-top: 32rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06); }
.result-status { font-size: 36rpx; font-weight: 700; text-align: center; }
.result-status.success { color: #15803D; }
.result-status.error { color: #DC2626; }
.result-info { margin-top: 16rpx; text-align: center; }
.result-name { font-size: 30rpx; color: #1F2937; display: block; }
.result-code { font-size: 24rpx; color: #6B7280; margin-top: 4rpx; display: block; }
.result-msg { font-size: 26rpx; color: #6B7280; text-align: center; margin-top: 12rpx; display: block; }
.manual-input { margin-top: 48rpx; }
.manual-label { font-size: 28rpx; color: #374151; margin-bottom: 12rpx; display: block; }
.manual-row { display: flex; gap: 16rpx; }
.manual-input-field { flex: 1; background: #fff; border-radius: 12rpx; padding: 16rpx 24rpx; font-size: 28rpx; border: 2rpx solid #D1D5DB; }
.manual-btn { background: #15803D; color: #fff; border-radius: 12rpx; padding: 16rpx 32rpx; font-size: 28rpx; border: none; }
</style>
