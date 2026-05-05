<template>
  <view class="reservations-page">
    <view class="tabs">
      <text :class="['tab', activeTab === 'dining' ? 'active' : '']" @tap="activeTab = 'dining'">餐饮预约</text>
      <text :class="['tab', activeTab === 'service' ? 'active' : '']" @tap="activeTab = 'service'">服务预约</text>
    </view>

    <view v-if="activeTab === 'dining'">
      <view v-if="diningList.length === 0" class="empty">
        <text class="empty-text">暂无餐饮预约</text>
      </view>
      <view v-for="item in diningList" :key="item.id" class="reservation-card">
        <view class="res-info">
          <text class="res-date">{{ item.date }} {{ item.time_slot === 'lunch' ? '午餐' : '晚餐' }}</text>
          <text class="res-table">{{ item.table?.table_number || '' }}</text>
        </view>
        <view class="res-right">
          <text class="res-status" :class="item.status">{{ statusMap[item.status] }}</text>
          <view v-if="item.status === 'pending'" class="cancel-btn" @tap="cancelDining(item.id)">
            <text class="cancel-text">取消</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'service'">
      <view v-if="serviceList.length === 0" class="empty">
        <text class="empty-text">暂无服务预约</text>
      </view>
      <view v-for="item in serviceList" :key="item.id" class="reservation-card">
        <view class="res-info">
          <text class="res-date">{{ item.createdAt ? item.createdAt.substring(0, 10) : '' }}</text>
          <text class="res-product">{{ item.service?.name || 'service #' + item.serviceId }}</text>
        </view>
        <view class="res-right">
          <text class="res-status" :class="item.status">{{ serviceStatusMap[item.status] || item.status }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { diningApi, plantingApi } from '@/api'

const activeTab = ref('dining')
const diningList = ref([])
const serviceList = ref([])
const statusMap = { pending: '待确认', checked_in: '已签到', cancelled: '已取消', no_show: '爽约' }
const serviceStatusMap = { pending: '待确认', completed: '已完成', cancelled: '已取消', no_show: '爽约' }

const loadDining = async () => {
  try {
    diningList.value = await diningApi.getReservations()
  } catch (e) {}
}

const loadService = async () => {
  try {
    serviceList.value = await plantingApi.getServiceOrders()
  } catch (e) {}
}

onMounted(() => {
  loadDining()
  loadService()
})

watch(activeTab, (val) => {
  if (val === 'dining') loadDining()
  else loadService()
})

const cancelDining = (id) => {
  uni.showModal({
    title: '确认取消',
    content: '距离预约6小时内取消将计爽约',
    success: async (e) => {
      if (e.confirm) {
        try {
          await diningApi.cancelReservation(id)
          uni.showToast({ title: '已取消', icon: 'success' })
          loadDining()
        } catch (err) {}
      }
    }
  })
}
</script>

<style scoped>
.reservations-page { padding: 24rpx; min-height: 100vh; background: var(--color-background); }
.tabs { display: flex; margin-bottom: 24rpx; background: var(--color-surface); border-radius: var(--radius-sm); overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: var(--color-muted); }
.tab.active { color: var(--color-primary); font-weight: 600; border-bottom: 4rpx solid var(--color-primary); }
.empty { text-align: center; padding: 120rpx 0; }
.empty-text { color: var(--color-muted); font-size: 28rpx; }
.reservation-card { display: flex; align-items: center; justify-content: space-between; background: var(--color-surface); border-radius: var(--radius-sm); padding: 28rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.res-info { flex: 1; }
.res-date { font-size: 28rpx; color: var(--color-text); display: block; }
.res-table, .res-product { font-size: 24rpx; color: var(--color-muted); display: block; margin-top: 4rpx; }
.res-right { display: flex; align-items: center; gap: 16rpx; }
.res-status { font-size: 26rpx; color: var(--color-primary); }
.res-status.cancelled, .res-status.no_show { color: var(--color-error); }
.cancel-btn { background: #FEE2E2; padding: 8rpx 20rpx; border-radius: 8rpx; }
.cancel-text { font-size: 24rpx; color: var(--color-error); }
</style>
