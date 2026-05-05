<template>
  <view class="custom-tabbar">
    <view
      v-for="(tab, i) in tabs"
      :key="tab.pagePath"
      class="tab-item"
      :class="{ active: i === current }"
      @tap="switchTab(tab.pagePath, i)"
    >
      <image class="tab-icon" :src="i === current ? tab.activeIcon : tab.icon" mode="aspectFit" />
      <text class="tab-text">{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { getRoleTabs, isTabBarPage } from '@/utils/tabBar'

const props = defineProps({
  current: { type: Number, default: 0 }
})

const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
const tabs = computed(() => {
  const roleTabs = getRoleTabs(userInfo.role || 'customer')
  return roleTabs.map(t => {
    const iconMap = {
      '首页': { icon: '/static/tab-home.png', activeIcon: '/static/tab-home-active.png' },
      '餐饮': { icon: '/static/tab-dining.png', activeIcon: '/static/tab-dining-active.png' },
      '产品': { icon: '/static/tab-products.png', activeIcon: '/static/tab-products-active.png' },
      '种植': { icon: '/static/tab-planting.png', activeIcon: '/static/tab-planting-active.png' },
      '我的': { icon: '/static/tab-profile.png', activeIcon: '/static/tab-profile-active.png' },
      '扫码': { icon: '/static/tab-home.png', activeIcon: '/static/tab-home-active.png' },
      '预约': { icon: '/static/tab-dining.png', activeIcon: '/static/tab-dining-active.png' },
      '订单': { icon: '/static/tab-products.png', activeIcon: '/static/tab-products-active.png' },
      '管理': { icon: '/static/tab-home.png', activeIcon: '/static/tab-home-active.png' },
      '业务': { icon: '/static/tab-dining.png', activeIcon: '/static/tab-dining-active.png' },
      '系统': { icon: '/static/tab-planting.png', activeIcon: '/static/tab-planting-active.png' }
    }
    const icons = iconMap[t.text] || { icon: '/static/tab-home.png', activeIcon: '/static/tab-home-active.png' }
    return { ...t, ...icons }
  })
})

const switchTab = (path, index) => {
  if (index === props.current) return
  if (isTabBarPage(path)) {
    uni.switchTab({ url: path })
  } else {
    uni.reLaunch({ url: path })
  }
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
.tab-icon { width: 48rpx; height: 48rpx; }
.tab-text { font-size: 20rpx; color: #6b7280; }
.tab-item.active .tab-text { color: #15803d; font-weight: 600; }
</style>
