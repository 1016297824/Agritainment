<template>
  <view class="page">
    <view class="date-tabs">
      <view v-for="(d, i) in dateList" :key="i" class="date-tab" :class="{ active: selectedDateIdx === i }" @tap="selectDate(i)">
        <text class="date-day">{{ d.day }}</text>
        <text class="date-label">{{ d.label }}</text>
      </view>
    </view>

    <view class="time-tabs">
      <view class="time-tab" :class="{ active: timeSlot === 'lunch' }" @tap="timeSlot = 'lunch'">中午</view>
      <view class="time-tab" :class="{ active: timeSlot === 'dinner' }" @tap="timeSlot = 'dinner'">夜晚</view>
    </view>

    <view v-if="currentReservation" class="my-reservation">
      <text class="my-reservation-label">我的预约：{{ currentReservation.reservationDate }} {{ currentReservation.timeSlot === 'lunch' ? '中午' : '夜晚' }}</text>
      <view class="cancel-btn" @tap="cancelMyReservation"><text class="cancel-btn-text">取消预约</text></view>
    </view>

    <view class="section-title-row">
      <text class="section-title">选择桌位</text>
    </view>

    <view class="table-list">
      <view v-for="t in tables" :key="t.id" class="table-card" :class="{ reserved: t.reserved }" @tap="reserveTable(t)">
        <view class="table-header">
          <text class="table-number">{{ t.tableNumber }}</text>
          <text class="table-status" :class="t.reserved ? 'status-reserved' : 'status-idle'">{{ t.reserved ? '已预订' : '可预约' }}</text>
        </view>
        <text class="table-capacity">{{ t.capacity }}人桌</text>
      </view>
    </view>

    <view class="bottom-nav">
      <view class="nav-item active" @tap="switchTab('reserve')">
        <text class="nav-icon">📅</text>
        <text class="nav-label">预约</text>
      </view>
      <view class="nav-item" @tap="switchTab('order')">
        <text class="nav-icon">🍽️</text>
        <text class="nav-label">点餐</text>
      </view>
    </view>
  </view>
</template>

<script>
import { diningApi } from '@/api'
import { requestReservationNotify } from '@/utils/subscribeMessage'
import { useDiningStore } from '@/stores/dining'
import { mapState, mapActions } from 'pinia'

export default {
  data() {
    return {
      tables: [],
      currentReservation: null
    }
  },
  computed: {
    ...mapState(useDiningStore, ['dateList', 'selectedDateIdx', 'timeSlot']),
    selectedDate() {
      return this.dateList[this.selectedDateIdx]?.value || ''
    }
  },
  async onShow() {
    const store = useDiningStore()
    store.initDates()
    await this.loadReservation()
    await this.loadTables()
  },
  methods: {
    ...mapActions(useDiningStore, ['selectDate']),
    async loadTables() {
      try {
        this.tables = await diningApi.getTables(this.selectedDate, this.timeSlot)
      } catch (e) {
        console.error('loadTables error:', e)
      }
    },
    async loadReservation() {
      try {
        const reservations = await diningApi.getReservations('pending')
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        this.currentReservation = reservations.find(r => {
          const d = new Date(r.reservationDate)
          d.setHours(0, 0, 0, 0)
          return d >= today
        }) || null
        const store = useDiningStore()
        store.currentReservation = this.currentReservation
      } catch (e) {
        console.error('loadReservation error:', e)
      }
    },
    async reserveTable(table) {
      if (table.reserved) {
        uni.showToast({ title: '该桌位已被预约', icon: 'none' })
        return
      }
      if (this.submitting) return
      this.submitting = true
      try {
        await requestReservationNotify()
        const store = useDiningStore()
        const reservation = await store.createReservation(table.id)
        this.currentReservation = reservation
        uni.showToast({ title: '预约成功', icon: 'success' })
        this.loadTables()
        setTimeout(() => {
          this.switchTab('order')
        }, 1000)
      } catch (e) {
        console.error('reserveTable error:', e)
      } finally {
        this.submitting = false
      }
    },
    async cancelMyReservation() {
      if (!this.currentReservation) return
      try {
        await diningApi.cancelReservation(this.currentReservation.id)
        this.currentReservation = null
        const store = useDiningStore()
        store.clearReservation()
        uni.showToast({ title: '预约已取消', icon: 'success' })
        this.loadTables()
      } catch (e) {
        console.error('cancelMyReservation error:', e)
      }
    },
    switchTab(tab) {
      if (tab === 'order') {
        const store = useDiningStore()
        if (!this.currentReservation && !store.hasValidReservation) {
          uni.showToast({ title: '请先完成预约', icon: 'none' })
          return
        }
        uni.navigateTo({ url: '/pages/dining/order/index' })
      }
    }
  },
  watch: {
    timeSlot() { this.loadTables() }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 120rpx; }
.date-tabs { display: flex; padding: 24rpx 32rpx; gap: 16rpx; }
.date-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); }
.date-tab.active { background: var(--color-primary); }
.date-day { display: block; font-size: 28rpx; font-weight: 600; }
.date-label { display: block; font-size: 22rpx; margin-top: 4rpx; }
.date-tab.active .date-day, .date-tab.active .date-label { color: #fff; }
.time-tabs { display: flex; padding: 0 32rpx 24rpx; gap: 16rpx; }
.time-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); font-size: 28rpx; color: var(--color-text); }
.time-tab.active { background: var(--color-secondary); color: #fff; }
.my-reservation {
  margin: 0 32rpx 20rpx;
  padding: 20rpx 24rpx;
  background: #D1FAE5;
  border-radius: var(--radius-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.my-reservation-label { font-size: 26rpx; color: var(--color-primary); font-weight: 500; }
.cancel-btn { padding: 8rpx 20rpx; background: #FCA5A5; border-radius: 8rpx; }
.cancel-btn-text { font-size: 24rpx; color: #991B1B; }
.section-title-row { padding: 0 32rpx 16rpx; }
.section-title { font-size: 30rpx; font-weight: 600; color: var(--color-text); }
.table-list { padding: 0 32rpx; display: flex; flex-wrap: wrap; gap: 16rpx; }
.table-card { width: calc(50% - 8rpx); padding: 24rpx; background: var(--color-surface); border-radius: var(--radius-md); box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.table-card.reserved { opacity: 0.5; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-number { font-size: 32rpx; font-weight: 600; color: var(--color-text); }
.table-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.status-idle { background: #D1FAE5; color: var(--color-primary); }
.status-reserved { background: #FEE2E2; color: var(--color-error); }
.table-capacity { font-size: 24rpx; color: var(--color-muted); margin-top: 8rpx; }
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: var(--color-surface);
  border-top: 1rpx solid var(--color-border);
  box-shadow: 0 -2rpx 8rpx rgba(0,0,0,0.04);
  z-index: 100;
}
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12rpx 0 20rpx;
  font-size: 22rpx;
  color: var(--color-muted);
}
.nav-item.active { color: var(--color-primary); }
.nav-icon { font-size: 36rpx; margin-bottom: 4rpx; }
.nav-label { font-size: 22rpx; }
</style>