<template>
  <view class="dashboard-page">
    <view class="page-header">
      <text class="page-title">管理后台</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card">
        <text class="stat-value">{{ stats.customerCount || 0 }}</text>
        <text class="stat-label">客户数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.memberCount || 0 }}</text>
        <text class="stat-label">会员数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.tableCount || 0 }}</text>
        <text class="stat-label">桌位数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.plotCount || 0 }}</text>
        <text class="stat-label">地块数</text>
      </view>
    </view>

    <view class="menu-grid">
      <view class="menu-item" @tap="goTo('/pages/admin/users/index')">
        <text class="menu-icon">👥</text>
        <text class="menu-text">人员管理</text>
      </view>
      <view class="menu-item" @tap="goTo('/pages/admin/business/index')">
        <text class="menu-icon">📋</text>
        <text class="menu-text">业务管理</text>
      </view>
      <view class="menu-item" @tap="goTo('/pages/admin/system/index')">
        <text class="menu-icon">⚙️</text>
        <text class="menu-text">系统管理</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'

const stats = ref({})

const loadDashboard = async () => {
  try {
    stats.value = await adminApi.getDashboard()
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const goTo = (url) => {
  uni.navigateTo({ url })
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 32rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; margin-bottom: 32rpx; }
.stat-card { background: #fff; border-radius: 16rpx; padding: 32rpx; text-align: center; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.stat-value { font-size: 48rpx; font-weight: 700; color: #15803D; display: block; }
.stat-label { font-size: 26rpx; color: #6B7280; margin-top: 4rpx; display: block; }
.menu-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16rpx; }
.menu-item { background: #fff; border-radius: 16rpx; padding: 32rpx 16rpx; text-align: center; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.menu-icon { font-size: 48rpx; display: block; }
.menu-text { font-size: 26rpx; color: #374151; margin-top: 8rpx; display: block; }
</style>
