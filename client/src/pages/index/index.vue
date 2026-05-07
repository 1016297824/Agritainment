<template>
  <view class="page">
    <view class="hero">
      <image class="hero-bg" src="/static/farm-hero.jpg" mode="aspectFill" />
      <view class="hero-overlay">
        <text class="hero-title">田园农家乐</text>
        <text class="hero-subtitle">回归自然 · 体验田园生活</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">农场动态</text>
      <view class="journal-list" v-if="journals.length">
        <view class="journal-card" v-for="item in journals" :key="item.id" @tap="viewJournal(item)">
          <image v-if="item.images && item.images[0]" class="journal-img" :src="item.images[0]" mode="aspectFill" />
          <view class="journal-info">
            <text class="journal-title">{{ item.title }}</text>
            <text class="journal-author">{{ item.author?.nickname || '匿名' }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">
        <text class="empty-text">暂无动态</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">特色服务</text>
      <view class="service-grid">
        <view class="service-item" @tap="goDining">
          <view class="service-icon dining-icon">
            <text class="icon-text">🍽</text>
          </view>
          <text class="service-name">餐饮预约</text>
        </view>
        <view class="service-item" @tap="goProducts">
          <view class="service-icon product-icon">
            <text class="icon-text">🎁</text>
          </view>
          <text class="service-name">产品服务</text>
        </view>
        <view class="service-item" @tap="goPlanting">
          <view class="service-icon plant-icon">
            <text class="icon-text">🌱</text>
          </view>
          <text class="service-name">种植体验</text>
        </view>
        <view class="service-item" @tap="goMembership">
          <view class="service-icon member-icon">
            <text class="icon-text">⭐</text>
          </view>
          <text class="service-name">会员中心</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { journalApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

export default {
  data() {
    return {
      journals: [],
      _redirecting: false
    }
  },
  onShow() {
    const store = useAuthStore()
    if (!store.isLoggedIn && !this._redirecting) {
      this._redirecting = true
      uni.switchTab({ url: '/pages/profile/index' })
      setTimeout(() => { this._redirecting = false }, 500)
      return
    }
    this.loadJournals()
  },
  methods: {
    async loadJournals() {
      try {
        this.journals = await journalApi.getSharedJournals()
      } catch (e) {}
    },
    viewJournal(item) {},
    goDining() { uni.switchTab({ url: '/pages/dining/index' }) },
    goProducts() { uni.switchTab({ url: '/pages/products/index' }) },
    goPlanting() { uni.switchTab({ url: '/pages/planting/index' }) },
    goMembership() { uni.switchTab({ url: '/pages/profile/index' }) }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); }
.hero { position: relative; height: 400rpx; overflow: hidden; }
.hero-bg { width: 100%; height: 100%; }
.hero-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(21,128,61,0.6); display: flex; flex-direction: column; justify-content: center; align-items: center; }
.hero-title { font-size: 48rpx; font-weight: 700; color: #fff; }
.hero-subtitle { font-size: 28rpx; color: rgba(255,255,255,0.9); margin-top: 16rpx; }
.section { padding: 32rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); margin-bottom: 24rpx; }
.journal-list { display: flex; flex-direction: column; gap: 24rpx; }
.journal-card { display: flex; background: var(--color-surface); border-radius: var(--radius-md); overflow: hidden; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06); }
.journal-img { width: 240rpx; height: 180rpx; }
.journal-info { flex: 1; padding: 24rpx; display: flex; flex-direction: column; justify-content: space-between; }
.journal-title { font-size: 28rpx; font-weight: 500; color: var(--color-text); }
.journal-author { font-size: 24rpx; color: var(--color-muted); }
.empty { padding: 80rpx 0; text-align: center; }
.empty-text { color: var(--color-muted); font-size: 28rpx; }
.service-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24rpx; }
.service-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.service-icon { width: 96rpx; height: 96rpx; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; }
.dining-icon { background: #FEF3C7; }
.product-icon { background: #DBEAFE; }
.plant-icon { background: #D1FAE5; }
.member-icon { background: #FDE68A; }
.icon-text { font-size: 40rpx; }
.service-name { font-size: 24rpx; color: var(--color-text); }
</style>
