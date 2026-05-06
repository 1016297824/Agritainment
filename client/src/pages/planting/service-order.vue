<template>
  <view class="page">
    <view class="section">
      <text class="section-title">服务信息</text>
      <view class="service-info-card">
        <text class="service-name">{{ service.name }}</text>
        <text class="service-desc">{{ service.description }}</text>
        <text class="service-price">¥{{ service.price }}</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">选择卡券支付</text>
      <view v-if="availableCoupons.length === 0" class="empty">
        <text class="empty-text">暂无可用卡券</text>
      </view>
      <view
        v-for="c in availableCoupons"
        :key="c.id"
        class="coupon-option"
        :class="{ selected: selectedCouponId === c.id }"
        @tap="selectedCouponId = c.id"
      >
        <text class="coupon-name">{{ c.Product?.name || '卡券' }}</text>
        <text class="coupon-code">{{ c.code }}</text>
        <view v-if="selectedCouponId === c.id" class="check-icon">✓</view>
      </view>
    </view>

    <view class="action-section">
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @tap="handleSubmit">
        <text class="submit-text">{{ submitting ? '提交中...' : '确认下单' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { plantingApi, couponApi } from '@/api'
import { requestServiceOrderNotify } from '@/utils/subscribeMessage'

const service = ref({})
const coupons = ref([])
const selectedCouponId = ref(null)
const submitting = ref(false)
const plotId = ref(0)
const serviceId = ref(0)

const availableCoupons = computed(() => coupons.value.filter(c => c.status === 'available' && (!serviceId.value || c.Product?.id === serviceId.value)))
const canSubmit = computed(() => selectedCouponId.value && !submitting.value)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  plotId.value = Number(page.options?.plotId || 0)
  serviceId.value = Number(page.options?.serviceId || 0)

  try {
    const [services, couponList] = await Promise.all([
      plantingApi.getGardenServices(),
      couponApi.getCoupons()
    ])
    coupons.value = couponList
    const found = services.find(s => s.id === serviceId.value)
    if (found) service.value = found
  } catch (e) {}
})

const handleSubmit = async () => {
  if (!canSubmit.value) return
  submitting.value = true
  await requestServiceOrderNotify()
  try {
    await plantingApi.createServiceOrder(plotId.value, serviceId.value, selectedCouponId.value)
    uni.showToast({ title: '下单成功，已通知园丁', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {} finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 140rpx; }
.section { padding: 32rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); margin-bottom: 24rpx; display: block; }
.service-info-card { background: var(--color-surface); border-radius: var(--radius-md); padding: 28rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.service-name { font-size: 30rpx; font-weight: 600; color: var(--color-text); display: block; }
.service-desc { font-size: 26rpx; color: var(--color-muted); display: block; margin-top: 8rpx; line-height: 1.5; }
.service-price { font-size: 32rpx; font-weight: 700; color: var(--color-cta); display: block; margin-top: 16rpx; }
.empty { padding: 60rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: var(--color-muted); }
.coupon-option { display: flex; align-items: center; background: var(--color-surface); border-radius: var(--radius-md); padding: 28rpx; margin-bottom: 16rpx; border: 2rpx solid var(--color-border); }
.coupon-option.selected { border-color: var(--color-primary); background: #F0FDF4; }
.coupon-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); flex: 1; }
.coupon-code { font-size: 24rpx; color: var(--color-muted); font-family: monospace; margin-right: 16rpx; }
.check-icon { color: var(--color-primary); font-size: 32rpx; font-weight: 700; }
.action-section { position: fixed; bottom: 0; left: 0; right: 0; padding: 32rpx; background: var(--color-surface); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); }
.submit-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 28rpx; text-align: center; }
.submit-btn.disabled { opacity: 0.5; }
.submit-text { color: #fff; font-size: 32rpx; font-weight: 600; }
</style>
