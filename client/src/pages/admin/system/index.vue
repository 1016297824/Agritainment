<template>
  <view class="system-page">
    <view class="page-header">
      <text class="page-title">系统管理</text>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">地块管理</text>
        <button class="btn-add-small" @tap="showAddPlot = true">添加</button>
      </view>
      <view v-for="plot in plots" :key="plot.id" class="item-row">
        <view class="item-info">
          <text class="item-name">{{ plot.name }}</text>
          <text class="item-status" :class="plot.status">{{ plot.status === 'available' ? '可租' : '已租' }}</text>
        </view>
        <button class="btn-del" @tap="deletePlot(plot.id)">删除</button>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">会员配置</text>
      </view>
      <view v-if="editConfig" class="config-form">
        <view class="form-row">
          <text class="form-label">年费(元)</text>
          <input v-model="editConfig.annualPrice" type="digit" class="form-input" />
        </view>
        <view class="form-row">
          <text class="form-label">折扣率</text>
          <input v-model="editConfig.discountRate" type="digit" class="form-input" placeholder="0.8 = 八折" />
        </view>
        <view class="form-row">
          <text class="form-label">赠送产品ID</text>
          <input v-model="editConfig.giftProductIds" class="form-input" placeholder="逗号分隔, 如 1,2,3" />
        </view>
        <button class="btn-save" @tap="saveConfig">保存配置</button>
      </view>
      <view v-else class="config-info">
        <text>年费: ¥{{ memberConfig.annualPrice }}</text>
        <text>折扣: {{ ((memberConfig.discountRate || 0) * 100).toFixed(0) }}%</text>
        <text>赠送产品: {{ (memberConfig.giftProductIds || []).length }}个</text>
      </view>
    </view>

    <view v-if="showAddPlot" class="modal">
      <view class="modal-content">
        <text class="modal-title">添加地块</text>
        <input v-model="newPlot.plot_number" placeholder="地块编号" class="modal-input" />
        <input v-model="newPlot.name" placeholder="地块名称" class="modal-input" />
        <input v-model="newPlot.area" placeholder="面积(㎡)" type="digit" class="modal-input" />
        <view class="modal-actions">
          <button @tap="showAddPlot = false">取消</button>
          <button class="btn-confirm" @tap="addPlot">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi, membershipApi, plantingApi } from '@/api'

const plots = ref([])
const memberConfig = ref({})
const editConfig = ref(null)
const showAddPlot = ref(false)
const newPlot = ref({ plot_number: '', name: '', area: '' })

const loadPlots = async () => {
  try {
    plots.value = await plantingApi.getPlots()
  } catch (err) { /* ignore */ }
}

const loadMemberConfig = async () => {
  try {
    const data = await membershipApi.getConfig()
    memberConfig.value = data
    editConfig.value = {
      annualPrice: String(data.annualPrice || ''),
      discountRate: String(data.discountRate || ''),
      giftProductIds: Array.isArray(data.giftProductIds) ? data.giftProductIds.join(',') : (data.giftProductIds || '')
    }
  } catch (err) { /* ignore */ }
}

const saveConfig = async () => {
  try {
    const payload = {
      annual_price: parseFloat(editConfig.value.annualPrice),
      discount_rate: parseFloat(editConfig.value.discountRate),
      gift_product_ids: editConfig.value.giftProductIds
    }
    await membershipApi.updateConfig(payload)
    uni.showToast({ title: '保存成功', icon: 'success' })
    loadMemberConfig()
  } catch (err) {
    uni.showToast({ title: err.message || '保存失败', icon: 'none' })
  }
}

const addPlot = async () => {
  try {
    await adminApi.createPlot({
      plot_number: newPlot.value.plot_number,
      name: newPlot.value.name,
      area: parseFloat(newPlot.value.area) || 0
    })
    showAddPlot.value = false
    newPlot.value = { plot_number: '', name: '', area: '' }
    loadPlots()
    uni.showToast({ title: '添加成功', icon: 'success' })
  } catch (err) {
    uni.showToast({ title: err.message || '添加失败', icon: 'none' })
  }
}

const deletePlot = async (id) => {
  try {
    await adminApi.deletePlot(id)
    loadPlots()
  } catch (err) { /* ignore */ }
}

onMounted(() => { loadPlots(); loadMemberConfig() })
</script>

<style scoped>
.system-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: #1F2937; }
.btn-add-small { font-size: 24rpx; background: #15803D; color: #fff; border-radius: 8rpx; padding: 8rpx 20rpx; border: none; }
.item-row { display: flex; justify-content: space-between; align-items: center; padding: 12rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-info { display: flex; gap: 16rpx; align-items: center; }
.item-name { font-size: 28rpx; color: #374151; }
.item-status { font-size: 22rpx; padding: 2rpx 12rpx; border-radius: 6rpx; }
.item-status.available { background: #D1FAE5; color: #065F46; }
.item-status.rented { background: #FEF3C7; color: #92400E; }
.btn-del { font-size: 24rpx; background: #FEE2E2; color: #DC2626; border-radius: 8rpx; padding: 6rpx 16rpx; border: none; }
.config-form { display: flex; flex-direction: column; gap: 16rpx; }
.form-row { display: flex; align-items: center; gap: 16rpx; }
.form-label { font-size: 28rpx; color: #374151; min-width: 160rpx; }
.form-input { flex: 1; border: 2rpx solid #D1D5DB; border-radius: 8rpx; padding: 12rpx 16rpx; font-size: 28rpx; }
.btn-save { background: #15803D; color: #fff; border-radius: 8rpx; font-size: 28rpx; margin-top: 8rpx; }
.config-info { display: flex; flex-direction: column; gap: 8rpx; }
.config-info text { font-size: 28rpx; color: #374151; }
.modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { background: #fff; border-radius: 16rpx; padding: 32rpx; width: 80%; }
.modal-title { font-size: 32rpx; font-weight: 600; color: #1F2937; margin-bottom: 16rpx; display: block; }
.modal-input { border: 2rpx solid #D1D5DB; border-radius: 8rpx; padding: 12rpx 16rpx; margin-bottom: 12rpx; font-size: 28rpx; }
.modal-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }
.btn-confirm { background: #15803D; color: #fff; border-radius: 8rpx; }
</style>
