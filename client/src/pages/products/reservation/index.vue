<template>
  <view class="page">
    <view class="section">
      <text class="section-title">选择预约日期</text>
      <ReservationCalendar v-model="selectedDate" :showTimeSlot="false" :days="3" />
    </view>

    <view class="section">
      <text class="section-title">选择卡券</text>
      <view v-if="availableCoupons.length === 0" class="empty">
        <text class="empty-text">暂无可用卡券，请先购买产品</text>
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
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @tap="handleReserve">
        <text class="submit-text">{{ submitting ? '预约中...' : '确认预约' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { couponApi } from '@/api'
import { requestReservationNotify } from '@/utils/subscribeMessage'
import ReservationCalendar from '@/components/ReservationCalendar.vue'

const coupons = ref([])
const selectedDate = ref('')
const selectedCouponId = ref(null)
const submitting = ref(false)
const productId = ref(0)

const availableCoupons = computed(() => coupons.value.filter(c => c.status === 'available' && (!productId.value || c.Product?.id === productId.value)))

const canSubmit = computed(() => selectedDate.value && selectedCouponId.value && !submitting.value)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  productId.value = Number(page.options?.productId || 0)

  const today = new Date()
  selectedDate.value = today.toISOString().split('T')[0]

  try {
    coupons.value = await couponApi.getCoupons()
  } catch (e) {}
})

const handleReserve = async () => {
  if (!canSubmit.value) return
  submitting.value = true
  await requestReservationNotify()
  try {
    await couponApi.createServiceReservation(selectedCouponId.value, productId.value, selectedDate.value)
    uni.showToast({ title: '预约成功', icon: 'success' })
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
.empty { padding: 60rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: var(--color-muted); }
.coupon-option { display: flex; align-items: center; background: var(--color-surface); border-radius: var(--radius-md); padding: 28rpx; margin-bottom: 16rpx; border: 2rpx solid var(--color-border); box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.coupon-option.selected { border-color: var(--color-primary); background: #F0FDF4; }
.coupon-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); flex: 1; }
.coupon-code { font-size: 24rpx; color: var(--color-muted); font-family: monospace; margin-right: 16rpx; }
.check-icon { color: var(--color-primary); font-size: 32rpx; font-weight: 700; }
.action-section { position: fixed; bottom: 0; left: 0; right: 0; padding: 32rpx; background: var(--color-surface); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); }
.submit-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 28rpx; text-align: center; }
.submit-btn.disabled { opacity: 0.5; }
.submit-text { color: #fff; font-size: 32rpx; font-weight: 600; }
</style>
