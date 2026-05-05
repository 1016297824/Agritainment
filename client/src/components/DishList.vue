<template>
  <view class="dish-list">
    <view
      v-for="dish in dishes"
      :key="dish.id"
      class="dish-item"
      :class="{ disabled: isSoldOut(dish) }"
    >
      <image v-if="dish.image_url" class="dish-img" :src="dish.image_url" mode="aspectFill" />
      <view class="dish-info">
        <text class="dish-name">{{ dish.name }}</text>
        <text v-if="dish.description" class="dish-desc">{{ dish.description }}</text>
        <view class="dish-bottom">
          <text class="dish-price">¥{{ dish.price }}</text>
          <text v-if="isSoldOut(dish)" class="sold-out">售罄</text>
        </view>
      </view>
      <view v-if="showCart" class="dish-actions">
        <view class="btn-minus" @tap="removeFromCart(dish)">
          <text class="btn-text">-</text>
        </view>
        <text class="cart-count">{{ cart[dish.id] || 0 }}</text>
        <view class="btn-plus" :class="{ disabled: isSoldOut(dish) }" @tap="addToCart(dish)">
          <text class="btn-text">+</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  dishes: { type: Array, default: () => [] },
  cart: { type: Object, default: () => ({}) },
  showCart: { type: Boolean, default: true }
})

const emit = defineEmits(['add', 'remove'])

const isSoldOut = (dish) => dish.remaining_stock !== null && dish.remaining_stock !== undefined && dish.remaining_stock <= 0

const addToCart = (dish) => {
  if (isSoldOut(dish)) return
  emit('add', dish)
}

const removeFromCart = (dish) => {
  emit('remove', dish)
}
</script>

<style scoped>
.dish-list { display: flex; flex-direction: column; gap: 16rpx; }
.dish-item { display: flex; background: var(--color-surface); border-radius: var(--radius-md); overflow: hidden; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.dish-item.disabled { opacity: 0.5; }
.dish-img { width: 200rpx; height: 200rpx; flex-shrink: 0; }
.dish-info { flex: 1; padding: 20rpx; display: flex; flex-direction: column; justify-content: space-between; }
.dish-name { font-size: 28rpx; font-weight: 500; color: var(--color-text); }
.dish-desc { font-size: 24rpx; color: var(--color-muted); margin-top: 4rpx; }
.dish-bottom { display: flex; align-items: center; gap: 12rpx; margin-top: 8rpx; }
.dish-price { font-size: 32rpx; font-weight: 600; color: var(--color-cta); }
.sold-out { font-size: 22rpx; color: var(--color-error); background: #FEE2E2; padding: 2rpx 12rpx; border-radius: 6rpx; }
.dish-actions { display: flex; align-items: center; gap: 16rpx; padding: 0 20rpx; flex-shrink: 0; }
.btn-minus, .btn-plus { width: 48rpx; height: 48rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-minus { background: #F3F4F6; }
.btn-plus { background: var(--color-primary); }
.btn-plus.disabled { background: #9CA3AF; }
.btn-text { font-size: 28rpx; color: #fff; }
.btn-minus .btn-text { color: var(--color-text); }
.cart-count { font-size: 28rpx; min-width: 40rpx; text-align: center; color: var(--color-text); font-weight: 500; }
</style>
