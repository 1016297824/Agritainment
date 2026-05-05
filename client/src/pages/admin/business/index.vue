<template>
  <view class="business-page">
    <view class="page-header">
      <text class="page-title">业务管理</text>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">菜品管理</text>
        <button class="btn-add-small" @tap="showAddDish = true">添加</button>
      </view>
      <view v-for="dish in dishes" :key="dish.id" class="item-row">
        <view class="item-info">
          <text class="item-name">{{ dish.name }}</text>
          <text class="item-price">¥{{ dish.price }}</text>
        </view>
        <button class="btn-del" @tap="deleteDish(dish.id)">删除</button>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">产品管理</text>
      </view>
      <view v-for="product in products" :key="product.id" class="item-row">
        <view class="item-info">
          <text class="item-name">{{ product.name }}</text>
          <text class="item-price">¥{{ product.price }}</text>
        </view>
        <button class="btn-del" @tap="deleteProduct(product.id)">删除</button>
      </view>
    </view>

    <view v-if="showAddDish" class="modal">
      <view class="modal-content">
        <text class="modal-title">添加菜品</text>
        <input v-model="newDish.name" placeholder="菜品名称" class="modal-input" />
        <input v-model="newDish.price" placeholder="价格" type="digit" class="modal-input" />
        <view class="modal-actions">
          <button @tap="showAddDish = false">取消</button>
          <button class="btn-confirm" @tap="addDish">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi, diningApi, productApi } from '@/api'

const dishes = ref([])
const products = ref([])
const showAddDish = ref(false)
const newDish = ref({ name: '', price: '' })

const loadDishes = async () => {
  try {
    dishes.value = await diningApi.getDishes()
  } catch (err) { /* ignore */ }
}

const loadProducts = async () => {
  try {
    products.value = await productApi.getList()
  } catch (err) { /* ignore */ }
}

const addDish = async () => {
  try {
    await adminApi.createDish({ name: newDish.value.name, price: parseFloat(newDish.value.price) })
    showAddDish.value = false
    newDish.value = { name: '', price: '' }
    loadDishes()
    uni.showToast({ title: '添加成功', icon: 'success' })
  } catch (err) {
    uni.showToast({ title: err.message || '添加失败', icon: 'none' })
  }
}

const deleteDish = async (id) => {
  try {
    await adminApi.deleteDish(id)
    loadDishes()
  } catch (err) { /* ignore */ }
}

const deleteProduct = async (id) => {
  try {
    await adminApi.deleteProduct(id)
    loadProducts()
  } catch (err) { /* ignore */ }
}

onMounted(() => { loadDishes(); loadProducts() })
</script>

<style scoped>
.business-page { padding: 32rpx; min-height: 100vh; background: #F0FDF4; }
.page-header { margin-bottom: 24rpx; }
.page-title { font-size: 40rpx; font-weight: 700; color: #15803D; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-size: 32rpx; font-weight: 600; color: #1F2937; }
.btn-add-small { font-size: 24rpx; background: #15803D; color: #fff; border-radius: 8rpx; padding: 8rpx 20rpx; border: none; }
.item-row { display: flex; justify-content: space-between; align-items: center; padding: 12rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-info { display: flex; gap: 16rpx; }
.item-name { font-size: 28rpx; color: #374151; }
.item-price { font-size: 28rpx; color: #CA8A04; font-weight: 600; }
.btn-del { font-size: 24rpx; background: #FEE2E2; color: #DC2626; border-radius: 8rpx; padding: 6rpx 16rpx; border: none; }
.modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { background: #fff; border-radius: 16rpx; padding: 32rpx; width: 80%; }
.modal-title { font-size: 32rpx; font-weight: 600; color: #1F2937; margin-bottom: 16rpx; display: block; }
.modal-input { border: 2rpx solid #D1D5DB; border-radius: 8rpx; padding: 12rpx 16rpx; margin-bottom: 12rpx; font-size: 28rpx; }
.modal-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }
.btn-confirm { background: #15803D; color: #fff; border-radius: 8rpx; }
</style>
