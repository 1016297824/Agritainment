<template>
  <view class="users-page">
    <view class="page-header">
      <text class="page-title">人员管理</text>
    </view>

    <view class="tabs">
      <text class="tab" :class="activeTab === 'customer' ? 'active' : ''" @tap="switchTab('customer')">客户</text>
      <text class="tab" :class="activeTab === 'staff' ? 'active' : ''" @tap="switchTab('staff')">员工</text>
    </view>

    <view class="user-list">
      <view v-for="user in users" :key="user.id" class="user-card">
        <view class="user-info">
          <text class="user-name">{{ user.nickname || user.phone }}</text>
          <text class="user-phone">{{ user.phone }}</text>
        </view>
        <view class="user-meta">
          <text v-if="user.isBlacklisted" class="tag blacklist">已拉黑</text>
          <text v-if="user.isMember" class="tag member">会员</text>
          <text v-if="user.noShowCount > 0" class="tag noshow">爽约{{ user.noShowCount }}次</text>
        </view>
        <view class="user-actions">
          <button v-if="!user.isBlacklisted" class="btn-blacklist" @tap="toggleBlacklist(user.id, true)">拉黑</button>
          <button v-else class="btn-unblacklist" @tap="toggleBlacklist(user.id, false)">解除</button>
          <button v-if="activeTab === 'customer'" class="btn-grant" @tap="grantMember(user.id)">赠会员</button>
          <button v-if="activeTab === 'staff'" class="btn-remove" @tap="removeStaff(user.id)">移除</button>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'staff'" class="add-section">
      <text class="add-title">添加员工</text>
      <view class="add-row">
        <input v-model="newStaffPhone" class="add-input" placeholder="手机号" />
        <input v-model="newStaffName" class="add-input" placeholder="姓名" />
        <button class="btn-add" @tap="addStaff">添加</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi, membershipApi } from '@/api'

const activeTab = ref('customer')
const users = ref([])
const newStaffPhone = ref('')
const newStaffName = ref('')

const loadUsers = async () => {
  try {
    const data = await adminApi.getUsers(activeTab.value)
    users.value = data.records || []
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  loadUsers()
}

const toggleBlacklist = async (id, isBlacklisted) => {
  try {
    await adminApi.updateUserStatus(id, { is_blacklisted: isBlacklisted })
    uni.showToast({ title: isBlacklisted ? '已拉黑' : '已解除', icon: 'success' })
    loadUsers()
  } catch (err) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const grantMember = async (id) => {
  try {
    await membershipApi.grant(id)
    uni.showToast({ title: '已赠送会员', icon: 'success' })
    loadUsers()
  } catch (err) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const addStaff = async () => {
  if (!newStaffPhone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  try {
    await adminApi.createStaff({ phone: newStaffPhone.value, nickname: newStaffName.value })
    uni.showToast({ title: '添加成功', icon: 'success' })
    newStaffPhone.value = ''
    newStaffName.value = ''
    loadUsers()
  } catch (err) {
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

const removeStaff = async (id) => {
  uni.showModal({
    title: '确认移除',
    content: '确定要移除此员工权限吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adminApi.deleteStaff(id)
          uni.showToast({ title: '已移除', icon: 'success' })
          loadUsers()
        } catch (err) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(loadUsers)
</script>

<style scoped>
.users-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.tabs { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.tab { padding: 12rpx 32rpx; border-radius: 8rpx; font-size: 28rpx; background: #fff; color: #6B7280; }
.tab.active { background: #15803D; color: #fff; }
.user-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.user-info { display: flex; justify-content: space-between; align-items: center; }
.user-name { font-size: 30rpx; font-weight: 600; color: #1F2937; }
.user-phone { font-size: 26rpx; color: #6B7280; }
.user-meta { display: flex; gap: 8rpx; margin-top: 8rpx; }
.tag { font-size: 22rpx; padding: 2rpx 12rpx; border-radius: 6rpx; }
.tag.blacklist { background: #FEE2E2; color: #DC2626; }
.tag.member { background: #FEF3C7; color: #92400E; }
.tag.noshow { background: #F3F4F6; color: #6B7280; }
.user-actions { display: flex; gap: 12rpx; margin-top: 12rpx; }
.btn-blacklist, .btn-unblacklist, .btn-grant, .btn-remove, .btn-add { font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 8rpx; border: none; }
.btn-blacklist { background: #FEE2E2; color: #DC2626; }
.btn-unblacklist { background: #D1FAE5; color: #065F46; }
.btn-grant { background: #FEF3C7; color: #92400E; }
.btn-remove { background: #FEE2E2; color: #DC2626; }
.btn-add { background: #15803D; color: #fff; }
.add-section { margin-top: 32rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; }
.add-title { font-size: 30rpx; font-weight: 600; color: #1F2937; margin-bottom: 16rpx; display: block; }
.add-row { display: flex; gap: 12rpx; }
.add-input { flex: 1; background: #F9FAFB; border-radius: 8rpx; padding: 12rpx 16rpx; font-size: 26rpx; border: 2rpx solid #D1D5DB; }
</style>
