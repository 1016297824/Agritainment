<template>
  <view class="orders-page">
    <view class="page-header">
      <text class="page-title">订单管理</text>
    </view>

    <view class="order-list">
      <view v-for="order in orders" :key="order.id" class="order-card">
        <view class="card-header">
          <text class="order-id">订单 #{{ order.id }}</text>
          <text class="status-tag" :class="order.status">{{ statusMap[order.status] }}</text>
        </view>
        <view class="card-info">
          <text>桌位: {{ order.table?.table_number || '-' }}</text>
          <text>金额: ¥{{ order.total_amount }}</text>
        </view>
        <view v-if="order.items && order.items.length" class="items-list">
          <view v-for="item in order.items" :key="item.id" class="item-row">
            <text>{{ item.dish?.name || '菜品' }} x{{ item.quantity }}</text>
            <text>¥{{ item.price * item.quantity }}</text>
          </view>
        </view>
        <view v-if="order.status === 'active'" class="card-actions">
          <button class="btn-settle" @tap="settle(order.id)">结账</button>
        </view>
      </view>
      <view v-if="orders.length === 0" class="empty">
        <text>暂无活跃订单</text>
      </view>
    </view>
    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import CustomTabBar from '@/components/CustomTabBar.vue'
import { ref, onMounted } from 'vue'
import { diningApi } from '@/api'

const orders = ref([])
const statusMap = { active: '进行中', settled: '已结账' }

const loadOrders = async () => {
  try {
    const data = await diningApi.getActiveOrders()
    orders.value = Array.isArray(data) ? data : (data ? [data] : [])
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const settle = async (id) => {
  uni.showModal({
    title: '确认结账',
    content: '确定要结账此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await diningApi.settleOrder(id)
          uni.showToast({ title: '结账成功', icon: 'success' })
          loadOrders()
        } catch (err) {
          uni.showToast({ title: err.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.order-id { font-size: 30rpx; font-weight: 600; color: #1F2937; }
.status-tag { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.status-tag.active { background: #DBEAFE; color: #1E40AF; }
.status-tag.settled { background: #D1FAE5; color: #065F46; }
.card-info { display: flex; gap: 32rpx; margin-bottom: 12rpx; }
.card-info text { font-size: 26rpx; color: #6B7280; }
.items-list { border-top: 1rpx solid #F3F4F6; padding-top: 12rpx; }
.item-row { display: flex; justify-content: space-between; padding: 4rpx 0; }
.item-row text { font-size: 26rpx; color: #374151; }
.card-actions { margin-top: 16rpx; }
.btn-settle { background: #CA8A04; color: #fff; border-radius: 8rpx; font-size: 26rpx; padding: 12rpx 32rpx; border: none; }
.empty { text-align: center; padding: 80rpx 0; color: #9CA3AF; font-size: 28rpx; }
</style>
