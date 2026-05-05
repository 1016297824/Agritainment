<template>
  <view class="coupon-card" :class="coupon.status" @tap="$emit('tap', coupon)">
    <view class="coupon-left">
      <text class="coupon-product">{{ coupon.Product?.name || '卡券' }}</text>
      <text class="coupon-code">{{ coupon.code }}</text>
      <text class="coupon-source">{{ sourceLabel }}</text>
    </view>
    <view class="coupon-right">
      <text class="coupon-status" :class="coupon.status">{{ statusLabel }}</text>
      <view v-if="coupon.status === 'available'" class="coupon-actions">
        <view v-if="showTransfer" class="action-btn transfer" @tap.stop="$emit('transfer', coupon)">转赠</view>
        <view v-if="showQr" class="action-btn qr" @tap.stop="showQrCode = true">二维码</view>
      </view>
    </view>

    <view v-if="showQrCode" class="qr-modal" @tap.stop="showQrCode = false">
      <view class="qr-content" @tap.stop>
        <text class="qr-title">{{ coupon.Product?.name || '卡券' }}</text>
        <view class="qr-placeholder">
          <text class="qr-code-text">{{ coupon.code }}</text>
        </view>
        <text class="qr-hint">出示此码给工作人员核销</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  coupon: { type: Object, required: true },
  showTransfer: { type: Boolean, default: true },
  showQr: { type: Boolean, default: true }
})

defineEmits(['tap', 'transfer'])

const showQrCode = ref(false)

const statusMap = { available: '可用', locked: '已锁定', used: '已使用', expired: '已过期' }
const sourceMap = { purchase: '购买', membership: '会员赠送', transfer: '转赠', grant: '赠送' }

const statusLabel = computed(() => statusMap[props.coupon.status] || props.coupon.status)
const sourceLabel = computed(() => sourceMap[props.coupon.source] || props.coupon.source || '购买')
</script>

<style scoped>
.coupon-card { display: flex; justify-content: space-between; align-items: center; background: var(--color-surface); border-radius: var(--radius-md); padding: 28rpx; margin-bottom: 16rpx; border-left: 8rpx solid var(--color-primary); box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.coupon-card.used, .coupon-card.expired { opacity: 0.5; border-left-color: var(--color-muted); }
.coupon-card.locked { border-left-color: var(--color-cta); }
.coupon-left { flex: 1; }
.coupon-product { font-size: 28rpx; font-weight: 600; color: var(--color-text); display: block; }
.coupon-code { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 4rpx; font-family: monospace; }
.coupon-source { font-size: 22rpx; color: var(--color-muted); display: block; margin-top: 4rpx; }
.coupon-right { text-align: right; flex-shrink: 0; }
.coupon-status { font-size: 26rpx; font-weight: 500; display: block; margin-bottom: 8rpx; }
.coupon-status.available { color: var(--color-primary); }
.coupon-status.locked { color: var(--color-cta); }
.coupon-status.used, .coupon-status.expired { color: var(--color-muted); }
.coupon-actions { display: flex; gap: 12rpx; }
.action-btn { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 8rpx; }
.action-btn.transfer { background: #FEF3C7; color: #92400E; }
.action-btn.qr { background: #D1FAE5; color: var(--color-primary); }
.qr-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 999; }
.qr-content { background: var(--color-surface); border-radius: var(--radius-lg); padding: 48rpx; text-align: center; width: 70%; }
.qr-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); display: block; margin-bottom: 32rpx; }
.qr-placeholder { width: 320rpx; height: 320rpx; margin: 0 auto; background: #F9FAFB; border: 2rpx dashed var(--color-border); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; }
.qr-code-text { font-size: 28rpx; font-weight: 700; color: var(--color-text); font-family: monospace; word-break: break-all; text-align: center; padding: 16rpx; }
.qr-hint { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 24rpx; }
</style>
