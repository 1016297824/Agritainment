<template>
  <view class="page">
    <view class="section">
      <text class="section-title">服务体验</text>
      <view class="product-grid">
        <view v-for="p in serviceProducts" :key="p.id" class="product-card" @tap="viewProduct(p.id)">
          <image v-if="p.image_url" class="product-img" :src="p.image_url" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ p.name }}</text>
            <view class="price-row">
              <text class="product-price">¥{{ p.price }}</text>
              <text v-if="p.member_price" class="member-price">会员 ¥{{ p.member_price }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">农副产品</text>
      <view class="product-grid">
        <view v-for="p in physicalProducts" :key="p.id" class="product-card" @tap="viewProduct(p.id)">
          <image v-if="p.image_url" class="product-img" :src="p.image_url" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ p.name }}</text>
            <view class="price-row">
              <text class="product-price">¥{{ p.price }}</text>
              <text v-if="p.member_price" class="member-price">会员 ¥{{ p.member_price }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <CustomTabBar :current="2" />
  </view>
</template>

<script>
import CustomTabBar from '@/components/CustomTabBar.vue'
import { productApi } from '@/api'

export default {
  data() {
    return { products: [] }
  },
  computed: {
    serviceProducts() { return this.products.filter(p => p.type === 'service') },
    physicalProducts() { return this.products.filter(p => p.type === 'physical') }
  },
  onShow() { this.loadProducts() },
  methods: {
    async loadProducts() {
      try { this.products = await productApi.getProducts() } catch (e) {}
    },
    viewProduct(id) {
      uni.navigateTo({ url: `/pages/products/detail?id=${id}` })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 32rpx; }
.section { padding: 32rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); margin-bottom: 24rpx; }
.product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.product-card { background: var(--color-surface); border-radius: var(--radius-md); overflow: hidden; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.product-img { width: 100%; height: 240rpx; }
.product-info { padding: 20rpx; }
.product-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); display: block; }
.price-row { display: flex; align-items: center; gap: 12rpx; margin-top: 8rpx; }
.product-price { font-size: 32rpx; font-weight: 600; color: var(--color-cta); }
.member-price { font-size: 22rpx; color: var(--color-primary); background: #D1FAE5; padding: 2rpx 8rpx; border-radius: 4rpx; }
</style>
