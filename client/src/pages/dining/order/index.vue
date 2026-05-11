<template>
  <view class="order-page">
    <view v-if="store.hasValidReservation" class="reservation-info">
      <text class="reservation-label">已预约：
        {{ store.currentReservation.tableId ? '桌位#' + store.currentReservation.tableId : '' }}
        - {{ store.currentReservation.reservationDate }}
        {{ store.currentReservation.timeSlot === 'lunch' ? '中午' : '夜晚' }}
      </text>
    </view>

    <view v-if="!currentOrder" class="dish-list">
      <text class="section-title">菜品菜单</text>
      <view v-if="store.dishes.length === 0" class="empty-state">
        <text class="empty-text">暂无可用菜品</text>
      </view>
      <view v-for="dish in store.dishes" :key="dish.id" class="dish-item">
        <image v-if="dish.imageUrl" class="dish-img" :src="dish.imageUrl" mode="aspectFill" />
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-desc">{{ dish.description }}</text>
          <view class="dish-price-row">
            <text class="dish-price">¥{{ dish.price }}</text>
            <text v-if="dish.remainingStock !== null && dish.remainingStock !== undefined && dish.remainingStock <= 0" class="sold-out">售罄</text>
          </view>
        </view>
        <view class="dish-actions">
          <view class="btn-minus" :class="{ disabled: !store.cart[dish.id] }" @tap="removeFromCart(dish)"><text class="btn-text">-</text></view>
          <text class="cart-count">{{ store.cart[dish.id] || 0 }}</text>
          <view class="btn-plus" :class="{ disabled: dish.remainingStock !== null && dish.remainingStock !== undefined && dish.remainingStock <= 0 }" @tap="addToCart(dish)"><text class="btn-text">+</text></view>
        </view>
      </view>
    </view>

    <view v-else class="order-detail">
      <text class="section-title">当前订单</text>
      <view v-for="item in currentOrder.items" :key="item.id" class="order-item">
        <text class="item-name">{{ item.dish?.name || '菜品#' + item.dishId }}</text>
        <text class="item-subtotal">x{{ item.quantity }} ¥{{ (item.price * item.quantity).toFixed(2) }}</text>
      </view>
      <view class="order-total">
        <text class="total-label">合计: ¥{{ currentOrder.totalAmount }}</text>
      </view>
      <view class="add-more-btn" @tap="addMore"><text class="add-more-text">继续加菜</text></view>
    </view>

    <view v-if="!currentOrder && store.cartCount > 0" class="cart-bar">
      <view class="cart-summary">
        <text>已选 {{ store.cartCount }} 件，合计 ¥{{ store.cartTotal.toFixed(2) }}</text>
      </view>
      <view class="submit-btn" @tap="submitOrder"><text class="submit-text">下单</text></view>
    </view>

    <view v-if="!store.hasValidReservation" class="no-reservation">
      <text class="no-reservation-text">请先完成预约后再进行点餐</text>
      <view class="go-reserve-btn" @tap="goReserve"><text class="go-reserve-text">去预约</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useDiningStore } from '@/stores/dining'

const store = useDiningStore()
const currentOrder = ref(null)
const submitting = ref(false)

onMounted(async () => {
  await store.fetchReservation()
  if (store.hasValidReservation) {
    await store.fetchDishes()
  }
})

const addToCart = (dish) => {
  store.addToCart(dish)
}

const removeFromCart = (dish) => {
  store.removeFromCart(dish)
}

const submitOrder = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    currentOrder.value = await store.submitOrder()
    uni.showToast({ title: '下单成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '下单失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const addMore = () => {
  currentOrder.value = null
}

const goReserve = () => {
  uni.navigateTo({ url: '/pages/dining/index' })
}
</script>

<style scoped>
.order-page { padding: 24rpx; min-height: 100vh; background: var(--color-background); padding-bottom: 120rpx; }
.reservation-info {
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  padding: 20rpx 24rpx;
  margin-bottom: 20rpx;
  border-left: 6rpx solid var(--color-primary);
}
.reservation-label { font-size: 26rpx; color: var(--color-primary); font-weight: 500; }
.section-title { font-size: 34rpx; font-weight: 700; display: block; margin-bottom: 20rpx; color: var(--color-text); }
.empty-state { padding: 80rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: var(--color-muted); }
.dish-item { display: flex; background: var(--color-surface); border-radius: var(--radius-md); overflow: hidden; padding: 16rpx; margin-bottom: 12rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); align-items: center; }
.dish-img { width: 140rpx; height: 140rpx; border-radius: var(--radius-sm); flex-shrink: 0; margin-right: 16rpx; }
.dish-info { flex: 1; min-width: 0; }
.dish-name { font-size: 28rpx; font-weight: 500; display: block; color: var(--color-text); }
.dish-desc { font-size: 22rpx; color: var(--color-muted); display: block; margin-top: 4rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dish-price-row { display: flex; align-items: center; margin-top: 8rpx; }
.dish-price { font-size: 28rpx; font-weight: 600; color: var(--color-cta); }
.sold-out { font-size: 22rpx; color: var(--color-error); margin-left: 12rpx; }
.dish-actions { display: flex; align-items: center; gap: 12rpx; flex-shrink: 0; }
.btn-minus, .btn-plus { width: 48rpx; height: 48rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-minus { background: #F3F4F6; }
.btn-minus.disabled { opacity: 0.4; }
.btn-plus { background: var(--color-primary); }
.btn-plus.disabled { opacity: 0.4; }
.btn-text { font-size: 28rpx; color: #fff; }
.btn-minus .btn-text { color: var(--color-text); }
.cart-count { font-size: 28rpx; min-width: 40rpx; text-align: center; color: var(--color-text); }
.cart-bar { position: fixed; bottom: 0; left: 0; right: 0; background: var(--color-surface); padding: 20rpx 24rpx; display: flex; justify-content: space-between; align-items: center; border-top: 1rpx solid var(--color-border); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); z-index: 100; }
.cart-summary { font-size: 28rpx; color: var(--color-text); }
.submit-btn { background: var(--color-primary); padding: 16rpx 48rpx; border-radius: var(--radius-sm); }
.submit-text { color: #fff; font-size: 28rpx; font-weight: 600; }
.order-detail { padding-bottom: 40rpx; }
.order-item { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-name { font-size: 28rpx; color: var(--color-text); }
.item-subtotal { font-size: 28rpx; color: var(--color-text); }
.order-total { text-align: right; font-size: 32rpx; font-weight: 700; color: var(--color-primary); padding: 20rpx 0; }
.add-more-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 24rpx; text-align: center; margin-top: 24rpx; }
.add-more-text { color: #fff; font-size: 30rpx; font-weight: 600; }
.no-reservation { padding: 120rpx 48rpx; text-align: center; }
.no-reservation-text { font-size: 28rpx; color: var(--color-muted); display: block; margin-bottom: 32rpx; }
.go-reserve-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 20rpx 64rpx; display: inline-block; }
.go-reserve-text { color: #fff; font-size: 30rpx; font-weight: 600; }
</style>