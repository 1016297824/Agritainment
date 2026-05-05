<template>
  <view class="calendar">
    <view class="date-tabs">
      <view
        v-for="(d, i) in dateList"
        :key="i"
        class="date-tab"
        :class="{ active: modelValue === d.value }"
        @tap="selectDate(d.value)"
      >
        <text class="date-day">{{ d.day }}</text>
        <text class="date-label">{{ d.label }}</text>
      </view>
    </view>

    <view v-if="showTimeSlot" class="time-tabs">
      <view
        class="time-tab"
        :class="{ active: timeSlot === 'lunch' }"
        @tap="$emit('update:timeSlot', 'lunch')"
      >中午</view>
      <view
        class="time-tab"
        :class="{ active: timeSlot === 'dinner' }"
        @tap="$emit('update:timeSlot', 'dinner')"
      >夜晚</view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  timeSlot: { type: String, default: 'lunch' },
  showTimeSlot: { type: Boolean, default: true },
  days: { type: Number, default: 3 }
})

const emit = defineEmits(['update:modelValue', 'update:timeSlot'])

const dateList = computed(() => {
  const list = []
  for (let i = 0; i < props.days; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    list.push({
      value: `${yyyy}-${mm}-${dd}`,
      day: `${d.getMonth() + 1}/${d.getDate()}`,
      label: i === 0 ? '今天' : i === 1 ? '明天' : '后天'
    })
  }
  return list
})

const selectDate = (val) => {
  emit('update:modelValue', val)
}
</script>

<style scoped>
.calendar { margin-bottom: 24rpx; }
.date-tabs { display: flex; padding: 0 32rpx; gap: 16rpx; }
.date-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); }
.date-tab.active { background: var(--color-primary); }
.date-day { display: block; font-size: 28rpx; font-weight: 600; color: var(--color-text); }
.date-label { display: block; font-size: 22rpx; margin-top: 4rpx; color: var(--color-muted); }
.date-tab.active .date-day, .date-tab.active .date-label { color: #fff; }
.time-tabs { display: flex; padding: 16rpx 32rpx 0; gap: 16rpx; }
.time-tab { flex: 1; padding: 16rpx; text-align: center; border-radius: var(--radius-sm); background: var(--color-surface); font-size: 28rpx; color: var(--color-text); }
.time-tab.active { background: var(--color-secondary); color: #fff; }
</style>
