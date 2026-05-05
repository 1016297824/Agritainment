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

    <view class="table-list">
      <view v-for="t in tables" :key="t.id" class="table-card" :class="{ reserved: t.is_reserved }" @tap="reserveTable(t)">
        <view class="table-header">
          <text class="table-number">{{ t.table_number }}</text>
          <text class="table-status" :class="t.status">{{ t.is_reserved ? '已预订' : '可预约' }}</text>
        </view>
        <text class="table-capacity">{{ t.capacity }}人桌</text>
      </view>
    </view>

    <view class="section" style="margin-top: 32rpx;">
      <text class="section-title">菜品列表</text>
      <view class="dish-list">
        <view v-for="d in dishes" :key="d.id" class="dish-card">
          <image v-if="d.image_url" class="dish-img" :src="d.image_url" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ d.name }}</text>
            <text class="dish-desc">{{ d.description }}</text>
            <text class="dish-price">¥{{ d.price }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { diningApi } from '@/api'

export default {
  data() {
    return {
      dateList: [],
      selectedDateIdx: 0,
      timeSlot: 'lunch',
      tables: [],
      dishes: []
    }
  },
  computed: {
    selectedDate() {
      return this.dateList[this.selectedDateIdx]?.value || ''
    }
  },
  onShow() {
    this.initDates()
    this.loadData()
  },
  methods: {
    initDates() {
      const days = ['日', '一', '二', '三', '四', '五', '六']
      this.dateList = []
      for (let i = 0; i < 3; i++) {
        const d = new Date()
        d.setDate(d.getDate() + i)
        const yyyy = d.getFullYear()
        const mm = String(d.getMonth() + 1).padStart(2, '0')
        const dd = String(d.getDate()).padStart(2, '0')
        this.dateList.push({
          value: `${yyyy}-${mm}-${dd}`,
          day: `${d.getMonth() + 1}/${d.getDate()}`,
          label: i === 0 ? '今天' : i === 1 ? '明天' : '后天'
        })
      }
    },
    selectDate(i) {
      this.selectedDateIdx = i
      this.loadTables()
    },
    async loadData() {
      await Promise.all([this.loadTables(), this.loadDishes()])
    },
    async loadTables() {
      try {
        this.tables = await diningApi.getTables(this.selectedDate, this.timeSlot)
      } catch (e) {}
    },
    async loadDishes() {
      try {
        this.dishes = await diningApi.getDishes()
      } catch (e) {}
    },
    async reserveTable(table) {
      if (table.is_reserved) {
        uni.showToast({ title: '该桌位已被预约', icon: 'none' })
        return
      }
      try {
        await diningApi.createReservation(table.id, this.selectedDate, this.timeSlot)
        uni.showToast({ title: '预约成功', icon: 'success' })
        this.loadTables()
      } catch (e) {}
    }
  },
  watch: {
    timeSlot() { this.loadTables() }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 32rpx; }
.date-tabs { display: flex; padding: 24rpx 32rpx; gap: 16rpx; }
.date-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); }
.date-tab.active { background: var(--color-primary); }
.date-day { display: block; font-size: 28rpx; font-weight: 600; }
.date-label { display: block; font-size: 22rpx; margin-top: 4rpx; }
.date-tab.active .date-day, .date-tab.active .date-label { color: #fff; }
.time-tabs { display: flex; padding: 0 32rpx 24rpx; gap: 16rpx; }
.time-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); font-size: 28rpx; color: var(--color-text); }
.time-tab.active { background: var(--color-secondary); color: #fff; }
.table-list { padding: 0 32rpx; display: flex; flex-wrap: wrap; gap: 16rpx; }
.table-card { width: calc(50% - 8rpx); padding: 24rpx; background: var(--color-surface); border-radius: var(--radius-md); box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.table-card.reserved { opacity: 0.6; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-number { font-size: 32rpx; font-weight: 600; color: var(--color-text); }
.table-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.table-status.idle { background: #D1FAE5; color: var(--color-primary); }
.table-status.reserved { background: #FEE2E2; color: var(--color-error); }
.table-status.dining { background: #FEF3C7; color: #92400E; }
.table-capacity { font-size: 24rpx; color: var(--color-muted); margin-top: 8rpx; }
.section { padding: 0 32rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); margin-bottom: 24rpx; }
.dish-list { display: flex; flex-direction: column; gap: 16rpx; }
.dish-card { display: flex; background: var(--color-surface); border-radius: var(--radius-md); overflow: hidden; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.dish-img { width: 200rpx; height: 200rpx; }
.dish-info { flex: 1; padding: 20rpx; display: flex; flex-direction: column; justify-content: space-between; }
.dish-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); }
.dish-desc { font-size: 24rpx; color: var(--color-muted); }
.dish-price { font-size: 32rpx; font-weight: 600; color: var(--color-cta); }
</style>
