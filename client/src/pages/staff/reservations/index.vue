<template>
  <view class="reservations-page">
    <view class="page-header">
      <text class="page-title">预约管理</text>
    </view>

    <view class="date-picker">
      <text class="date-label">日期</text>
      <picker mode="date" :value="selectedDate" @change="onDateChange">
        <view class="date-value">{{ selectedDate }}</view>
      </picker>
    </view>

    <view class="reservation-list">
      <view v-for="item in reservations" :key="item.id" class="reservation-card">
        <view class="card-header">
          <text class="table-name">{{ item.table?.table_number || '桌位' }}</text>
          <text class="status-tag" :class="item.status">{{ statusMap[item.status] }}</text>
        </view>
        <view class="card-info">
          <text>时段: {{ item.time_slot === 'lunch' ? '午餐' : '晚餐' }}</text>
          <text>客户: {{ item.user?.phone || '-' }}</text>
        </view>
        <view v-if="item.status === 'pending'" class="card-actions">
          <button class="btn-checkin" @tap="checkin(item.id)">签到</button>
          <button class="btn-cancel" @tap="cancel(item.id)">取消</button>
        </view>
      </view>
      <view v-if="reservations.length === 0" class="empty">
        <text>暂无预约</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { diningApi } from '@/api'

const selectedDate = ref(new Date().toISOString().split('T')[0])
const reservations = ref([])
const statusMap = { pending: '待处理', checked_in: '已签到', cancelled: '已取消' }

const loadReservations = async () => {
  try {
    const data = await diningApi.getReservations()
    reservations.value = (data || []).filter(r => r.reservation_date === selectedDate.value)
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const onDateChange = (e) => {
  selectedDate.value = e.detail.value
  loadReservations()
}

const checkin = async (id) => {
  try {
    await diningApi.staffCheckin(id)
    uni.showToast({ title: '签到成功', icon: 'success' })
    loadReservations()
  } catch (err) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const cancel = async (id) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消此预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await diningApi.staffCancel(id)
          uni.showToast({ title: '已取消', icon: 'success' })
          loadReservations()
        } catch (err) {
          uni.showToast({ title: err.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(loadReservations)
</script>

<style scoped>
.reservations-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.date-picker { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; display: flex; align-items: center; justify-content: space-between; }
.date-label { font-size: 28rpx; color: #374151; }
.date-value { font-size: 28rpx; color: #15803D; font-weight: 600; }
.reservation-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.table-name { font-size: 32rpx; font-weight: 600; color: #1F2937; }
.status-tag { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.status-tag.pending { background: #FEF3C7; color: #92400E; }
.status-tag.checked_in { background: #D1FAE5; color: #065F46; }
.status-tag.cancelled { background: #F3F4F6; color: #6B7280; }
.card-info { display: flex; flex-direction: column; gap: 4rpx; }
.card-info text { font-size: 26rpx; color: #6B7280; }
.card-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }
.btn-checkin { background: #15803D; color: #fff; border-radius: 8rpx; font-size: 26rpx; padding: 8rpx 24rpx; border: none; }
.btn-cancel { background: #FEE2E2; color: #DC2626; border-radius: 8rpx; font-size: 26rpx; padding: 8rpx 24rpx; border: none; }
.empty { text-align: center; padding: 80rpx 0; color: #9CA3AF; font-size: 28rpx; }
</style>
