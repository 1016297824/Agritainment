<template>
  <view class="custom-tabbar">
    <view class="tabbar-bg" />
    <view class="tabbar-inner">
      <view
        v-for="(tab, i) in tabs"
        :key="tab.pagePath"
        class="tab-item"
        :class="{ active: i === current }"
        @tap="switchTab(tab.pagePath, i)"
      >
        <view class="tab-icon-wrap">
          <view v-if="i === current" class="active-glow" />
          <image class="tab-icon" :src="i === current ? tab.activeIcon : tab.icon" mode="aspectFit" />
        </view>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
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
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
}

.tabbar-bg {
  position: absolute;
  top: 0;
  left: 16rpx;
  right: 16rpx;
  bottom: 0;
  background: #FFFFFF;
  border-radius: 28rpx 28rpx 0 0;
  box-shadow: 0 -6rpx 32rpx rgba(20, 83, 45, 0.08), 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(20, 83, 45, 0.06);
  border-bottom: none;
}

.tabbar-inner {
  position: relative;
  display: flex;
  height: 104rpx;
  padding: 8rpx 8rpx 0;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  min-height: 88rpx;
  position: relative;
  transition: transform 0.2s ease;
}

.tab-item:active {
  transform: scale(0.94);
}

.tab-icon-wrap {
  position: relative;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.active-glow {
  position: absolute;
  top: -4rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(21, 128, 61, 0.12) 0%, transparent 70%);
  pointer-events: none;
}

.tab-icon {
  width: 44rpx;
  height: 44rpx;
  transition: transform 0.2s ease;
}

.tab-item.active .tab-icon {
  transform: scale(1.1);
}

.tab-text {
  font-size: 20rpx;
  font-weight: 400;
  color: #9CA3AF;
  letter-spacing: 1rpx;
  transition: color 0.2s ease, font-weight 0.2s ease;
}

.tab-item.active .tab-text {
  color: #15803D;
  font-weight: 600;
}
</style>
