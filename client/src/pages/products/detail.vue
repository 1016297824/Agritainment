<template>
  <view class="page">
    <image v-if="product.image_url" class="hero-img" :src="product.image_url" mode="aspectFill" />

    <view class="info-section">
      <text class="product-name">{{ product.name }}</text>
      <text v-if="product.description" class="product-desc">{{ product.description }}</text>
      <view class="price-row">
        <text class="product-price">¥{{ product.price }}</text>
        <text v-if="product.member_price" class="member-price">会员 ¥{{ product.member_price }}</text>
      </view>
      <view v-if="product.type === 'physical' && product.remaining_quota !== null && product.remaining_quota !== -1" class="stock-info">
        <text class="stock-text">今日可兑: {{ product.remaining_quota }}份</text>
      </view>
    </view>

    <view class="action-section">
      <view class="buy-btn" :class="{ disabled: purchasing }" @tap="handlePurchase">
        <text class="buy-text">{{ purchasing ? '购买中...' : '立即购买' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi, couponApi } from '@/api'

const product = ref({})
const purchasing = ref(false)
const productId = ref(0)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  productId.value = Number(page.options?.id || 0)
  if (productId.value) {
    try {
      product.value = await productApi.getProduct(productId.value)
    } catch (e) {}
  }
})

const handlePurchase = async () => {
  if (purchasing.value) return
  purchasing.value = true
  try {
    await productApi.purchase(productId.value)
    uni.showToast({ title: '购买成功，卡券已存入卡包', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {} finally {
    purchasing.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); }
.hero-img { width: 100%; height: 480rpx; }
.info-section { padding: 32rpx; background: var(--color-surface); }
.product-name { font-size: 36rpx; font-weight: 700; color: var(--color-text); display: block; }
.product-desc { font-size: 26rpx; color: var(--color-muted); display: block; margin-top: 12rpx; line-height: 1.6; }
.price-row { display: flex; align-items: center; gap: 16rpx; margin-top: 20rpx; }
.product-price { font-size: 40rpx; font-weight: 700; color: var(--color-cta); }
.member-price { font-size: 26rpx; color: var(--color-primary); background: #D1FAE5; padding: 4rpx 16rpx; border-radius: 8rpx; font-weight: 500; }
.stock-info { margin-top: 16rpx; }
.stock-text { font-size: 24rpx; color: var(--color-muted); }
.action-section { padding: 32rpx; position: fixed; bottom: 0; left: 0; right: 0; background: var(--color-surface); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); }
.buy-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 28rpx; text-align: center; }
.buy-btn.disabled { opacity: 0.6; }
.buy-text { color: #fff; font-size: 32rpx; font-weight: 600; }
</style>
