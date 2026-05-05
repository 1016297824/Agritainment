<template>
  <view class="system-page">
    <view class="page-header">
      <text class="page-title">系统管理</text>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">地块管理</text>
      </view>
      <view v-for="plot in plots" :key="plot.id" class="item-row">
        <view class="item-info">
          <text class="item-name">{{ plot.name }}</text>
          <text class="item-status" :class="plot.status">{{ plot.status === 'available' ? '可租' : '已租' }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">会员配置</text>
      </view>
      <view v-if="memberConfig" class="config-info">
        <text>年费: ¥{{ memberConfig.annual_price }}</text>
        <text>折扣: {{ (memberConfig.discount_rate * 100).toFixed(0) }}%</text>
        <text>赠送产品: {{ (memberConfig.gift_product_ids || []).length }}个</text>
      </view>
    </view>
    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import CustomTabBar from '@/components/CustomTabBar.vue'
import { ref, onMounted } from 'vue'
import { adminApi, membershipApi, plantingApi } from '@/api'

const plots = ref([])
const memberConfig = ref(null)

const loadPlots = async () => {
  try {
    plots.value = await plantingApi.getPlots()
  } catch (err) { /* ignore */ }
}

const loadMemberConfig = async () => {
  try {
    memberConfig.value = await membershipApi.getConfig()
  } catch (err) { /* ignore */ }
}

onMounted(() => { loadPlots(); loadMemberConfig() })
</script>

<style scoped>
.system-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.section-header { margin-bottom: 16rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: #1F2937; }
.item-row { display: flex; justify-content: space-between; align-items: center; padding: 12rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-info { display: flex; gap: 16rpx; align-items: center; }
.item-name { font-size: 28rpx; color: #374151; }
.item-status { font-size: 22rpx; padding: 2rpx 12rpx; border-radius: 6rpx; }
.item-status.available { background: #D1FAE5; color: #065F46; }
.item-status.rented { background: #FEF3C7; color: #92400E; }
.config-info { display: flex; flex-direction: column; gap: 8rpx; }
.config-info text { font-size: 28rpx; color: #374151; }
</style>
