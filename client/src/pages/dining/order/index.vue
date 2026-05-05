<template>
  <view class="order-page">
    <view v-if="!order" class="dish-list">
      <text class="section-title">菜单</text>
      <view v-for="dish in dishes" :key="dish.id" class="dish-item">
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-price">¥{{ dish.price }}</text>
          <text v-if="dish.remaining_stock !== null && dish.remaining_stock !== undefined && dish.remaining_stock <= 0" class="sold-out">售罄</text>
        </view>
        <view class="dish-actions">
          <view class="btn-minus" @tap="removeFromCart(dish)"><text class="btn-text">-</text></view>
          <text class="cart-count">{{ cart[dish.id] || 0 }}</text>
          <view class="btn-plus" @tap="addToCart(dish)"><text class="btn-text">+</text></view>
        </view>
      </view>
      <view class="cart-bar" v-if="cartTotal > 0">
        <text class="cart-summary">已选 {{ cartCount }} 件，合计 ¥{{ cartTotal.toFixed(2) }}</text>
        <view class="submit-btn" @tap="submitOrder"><text class="submit-text">下单</text></view>
      </view>
    </view>

    <view v-else class="order-detail">
      <text class="section-title">当前订单</text>
      <view v-for="item in order.items" :key="item.id" class="order-item">
        <text class="item-name">{{ item.dish?.name || '菜品#' + item.dishId }}</text>
        <text class="item-subtotal">x{{ item.quantity }} ¥{{ (item.price * item.quantity).toFixed(2) }}</text>
      </view>
      <view class="order-total">
        <text class="total-label">合计: ¥{{ order.total_amount }}</text>
      </view>
      <view class="add-more-btn" @tap="addMore"><text class="add-more-text">继续加菜</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { diningApi } from '@/api'

const tableQr = ref('')
const dishes = ref([])
const cart = ref({})
const order = ref(null)

const cartCount = computed(() => Object.values(cart.value).reduce((a, b) => a + b, 0))
const cartTotal = computed(() => dishes.value.reduce((sum, d) => sum + (cart.value[d.id] || 0) * d.price, 0))

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  tableQr.value = page.options?.qr || ''

  try {
    dishes.value = await diningApi.getDishes()
  } catch (e) {}

  if (tableQr.value) {
    try {
      order.value = await diningApi.getActiveOrder(tableQr.value)
    } catch (e) {}
  }
})

const addToCart = (dish) => {
  if (dish.remaining_stock !== null && dish.remaining_stock !== undefined && dish.remaining_stock <= 0) return
  cart.value[dish.id] = (cart.value[dish.id] || 0) + 1
}

const removeFromCart = (dish) => {
  if (cart.value[dish.id] && cart.value[dish.id] > 0) {
    cart.value[dish.id]--
    if (cart.value[dish.id] === 0) delete cart.value[dish.id]
  }
}

const submitOrder = async () => {
  if (!tableQr.value) {
    uni.showToast({ title: '请扫描桌位二维码', icon: 'none' })
    return
  }
  const items = Object.entries(cart.value).map(([dishId, quantity]) => ({ dish_id: Number(dishId), quantity }))
  if (items.length === 0) return
  try {
    order.value = await diningApi.createOrder(tableQr.value, items)
    cart.value = {}
    uni.showToast({ title: '下单成功', icon: 'success' })
  } catch (e) {}
}

const addMore = () => { order.value = null }
</script>

<style scoped>
.order-page { padding: 24rpx; min-height: 100vh; background: var(--color-background); }
.section-title { font-size: 34rpx; font-weight: 700; display: block; margin-bottom: 20rpx; color: var(--color-text); }
.dish-item { display: flex; justify-content: space-between; align-items: center; background: var(--color-surface); border-radius: var(--radius-sm); padding: 24rpx; margin-bottom: 12rpx; }
.dish-info { flex: 1; }
.dish-name { font-size: 28rpx; display: block; color: var(--color-text); }
.dish-price { font-size: 26rpx; color: var(--color-primary); }
.sold-out { font-size: 24rpx; color: var(--color-error); margin-left: 12rpx; }
.dish-actions { display: flex; align-items: center; gap: 16rpx; }
.btn-minus, .btn-plus { width: 48rpx; height: 48rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-minus { background: #F3F4F6; }
.btn-plus { background: var(--color-primary); }
.btn-text { font-size: 28rpx; color: #fff; }
.btn-minus .btn-text { color: var(--color-text); }
.cart-count { font-size: 28rpx; min-width: 40rpx; text-align: center; }
.cart-bar { position: fixed; bottom: 0; left: 0; right: 0; background: var(--color-surface); padding: 20rpx 24rpx; display: flex; justify-content: space-between; align-items: center; border-top: 1rpx solid var(--color-border); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); }
.cart-summary { font-size: 28rpx; color: var(--color-text); }
.submit-btn { background: var(--color-primary); padding: 16rpx 48rpx; border-radius: var(--radius-sm); }
.submit-text { color: #fff; font-size: 28rpx; font-weight: 600; }
.order-detail { padding-bottom: 120rpx; }
.order-item { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-name { font-size: 28rpx; color: var(--color-text); }
.item-subtotal { font-size: 28rpx; color: var(--color-text); }
.order-total { text-align: right; font-size: 32rpx; font-weight: 700; color: var(--color-primary); padding: 20rpx 0; }
.add-more-btn { background: var(--color-primary); border-radius: var(--radius-sm); padding: 24rpx; text-align: center; margin-top: 24rpx; }
.add-more-text { color: #fff; font-size: 30rpx; font-weight: 600; }
</style>
