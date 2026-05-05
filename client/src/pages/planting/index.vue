<template>
  <view class="page">
    <view class="section">
      <text class="section-title">我的地块</text>
      <view v-if="myPlot" class="plot-card rented" @tap="viewPlot(myPlot.id)">
        <view class="plot-info">
          <text class="plot-number">{{ myPlot.plot_number }}</text>
          <text class="plot-name">{{ myPlot.name }}</text>
          <text class="plot-area">{{ myPlot.area }}㎡</text>
        </view>
        <text class="plot-status rented">已租用</text>
      </view>
      <view v-else class="empty-plot">
        <text class="empty-text">您还未租用地块</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">可租用地块</text>
      <view class="plot-list">
        <view v-for="p in availablePlots" :key="p.id" class="plot-card" @tap="viewPlot(p.id)">
          <view class="plot-info">
            <text class="plot-number">{{ p.plot_number }}</text>
            <text class="plot-name">{{ p.name }}</text>
            <text class="plot-area">{{ p.area }}㎡</text>
          </view>
          <view class="plot-action" @tap.stop="rentPlot(p)">
            <text class="action-btn">租用</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">种植管理服务</text>
      <view class="service-list">
        <view v-for="s in gardenServices" :key="s.id" class="service-card">
          <view class="service-info">
            <text class="service-name">{{ s.name }}</text>
            <text class="service-desc">{{ s.description }}</text>
            <text class="service-price">¥{{ s.price }}</text>
          </view>
          <view class="service-action" @tap="orderService(s)">
            <text class="action-btn">下单</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { plantingApi } from '@/api'

export default {
  data() {
    return { plots: [], gardenServices: [] }
  },
  computed: {
    myPlot() { return this.plots.find(p => p.renter_id) },
    availablePlots() { return this.plots.filter(p => p.status === 'available') }
  },
  onShow() { this.loadData() },
  methods: {
    async loadData() {
      try {
        const [plots, services] = await Promise.all([
          plantingApi.getPlots(),
          plantingApi.getGardenServices()
        ])
        this.plots = plots
        this.gardenServices = services
      } catch (e) {}
    },
    viewPlot(id) {
      uni.navigateTo({ url: `/pages/planting/plot-detail?id=${id}` })
    },
    async rentPlot(plot) {
      try {
        await plantingApi.rentPlot(plot.id)
        uni.showToast({ title: '租用成功', icon: 'success' })
        this.loadData()
      } catch (e) {}
    },
    orderService(service) {
      if (!this.myPlot) {
        uni.showToast({ title: '请先租用地块', icon: 'none' })
        return
      }
      uni.navigateTo({ url: `/pages/planting/service-order?plotId=${this.myPlot.id}&serviceId=${service.id}` })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 32rpx; }
.section { padding: 32rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); margin-bottom: 24rpx; }
.plot-card { background: var(--color-surface); border-radius: var(--radius-md); padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.plot-card.rented { border-left: 8rpx solid var(--color-primary); }
.plot-info { flex: 1; }
.plot-number { font-size: 28rpx; font-weight: 600; color: var(--color-text); display: block; }
.plot-name { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 4rpx; }
.plot-area { font-size: 24rpx; color: var(--color-muted); display: block; }
.plot-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.plot-status.rented { background: #D1FAE5; color: var(--color-primary); }
.action-btn { background: var(--color-cta); color: #fff; padding: 12rpx 32rpx; border-radius: var(--radius-sm); font-size: 26rpx; font-weight: 500; }
.empty-plot { padding: 60rpx 0; text-align: center; }
.empty-text { color: var(--color-muted); font-size: 28rpx; }
.service-card { background: var(--color-surface); border-radius: var(--radius-md); padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.service-info { flex: 1; }
.service-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); display: block; }
.service-desc { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 4rpx; }
.service-price { font-size: 28rpx; font-weight: 600; color: var(--color-cta); display: block; margin-top: 8rpx; }
</style>
