<template>
  <view class="custom-tabbar">
    <view
      v-for="(tab, i) in tabs"
      :key="tab.pagePath"
      class="tab-item"
      :class="{ active: i === current }"
      @tap="switchTab(tab.pagePath, i)"
    >
      <text class="tab-icon">{{ icons[i] }}</text>
      <text class="tab-text">{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { getRoleTabs } from '@/utils/tabBar'

const props = defineProps({
  current: { type: Number, default: 0 }
})

const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
const tabs = computed(() => getRoleTabs(userInfo.role || 'customer'))

const icons = computed(() => {
  return tabs.value.map(t => {
    if (t.text === '首页') return '🏠'
    if (t.text === '餐饮') return '🍽'
    if (t.text === '产品') return '🎁'
    if (t.text === '种植') return '🌱'
    if (t.text === '我的') return '👤'
    if (t.text === '扫码') return '📷'
    if (t.text === '预约') return '📋'
    if (t.text === '订单') return '📦'
    if (t.text === '管理') return '⚙'
    if (t.text === '业务') return '📊'
    if (t.text === '系统') return '🔧'
    return '•'
  })
})

const switchTab = (path, index) => {
  if (index === props.current) return
  uni.switchTab({
    url: path,
    fail: () => { uni.reLaunch({ url: path }) }
  })
}
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  border-top: 1rpx solid #e5e7eb;
  display: flex;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}
.tab-icon { font-size: 40rpx; }
.tab-text { font-size: 20rpx; color: #6b7280; }
.tab-item.active .tab-text { color: #15803d; font-weight: 600; }
</style>
