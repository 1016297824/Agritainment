<template>
  <view v-if="visible" class="drawer-mask" @tap="close">
    <view class="drawer" @tap.stop>
      <view class="drawer-header">
        <text class="drawer-title">购物车</text>
        <text class="drawer-close" @tap="close">✕</text>
      </view>

      <view v-if="items.length === 0" class="empty">
        <text class="empty-text">购物车为空</text>
      </view>

      <view v-else class="cart-items">
        <view v-for="item in items" :key="item.dish.id" class="cart-item">
          <view class="item-info">
            <text class="item-name">{{ item.dish.name }}</text>
            <text class="item-price">¥{{ item.dish.price }}</text>
          </view>
          <view class="item-qty">
            <view class="btn-minus" @tap="remove(item.dish)"><text class="btn-text">-</text></view>
            <text class="qty">{{ item.qty }}</text>
            <view class="btn-plus" @tap="add(item.dish)"><text class="btn-text">+</text></view>
          </view>
          <text class="item-subtotal">¥{{ (item.dish.price * item.qty).toFixed(2) }}</text>
        </view>
      </view>

      <view v-if="items.length > 0" class="drawer-footer">
        <view class="total">
          <text class="total-label">合计</text>
          <text class="total-value">¥{{ total.toFixed(2) }}</text>
        </view>
        <view class="submit-btn" @tap="submit">
          <text class="submit-text">下单</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  cart: { type: Object, default: () => ({}) },
  dishes: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'add', 'remove', 'submit'])

const items = computed(() => {
  return Object.entries(props.cart)
    .map(([id, qty]) => {
      const dish = props.dishes.find(d => d.id === Number(id))
      return dish ? { dish, qty } : null
    })
    .filter(Boolean)
})

const total = computed(() => items.value.reduce((sum, item) => sum + item.dish.price * item.qty, 0))

const close = () => emit('update:visible', false)
const add = (dish) => emit('add', dish)
const remove = (dish) => emit('remove', dish)
const submit = () => emit('submit')
</script>

<style scoped>
.drawer-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: flex-end; }
.drawer { width: 100%; max-height: 70vh; background: var(--color-surface); border-radius: var(--radius-lg) var(--radius-lg) 0 0; display: flex; flex-direction: column; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 32rpx; border-bottom: 1rpx solid var(--color-border); }
.drawer-title { font-size: 32rpx; font-weight: 600; color: var(--color-text); }
.drawer-close { font-size: 36rpx; color: var(--color-muted); padding: 8rpx; }
.empty { padding: 80rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: var(--color-muted); }
.cart-items { flex: 1; overflow-y: auto; padding: 16rpx 32rpx; }
.cart-item { display: flex; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.item-info { flex: 1; }
.item-name { font-size: 28rpx; color: var(--color-text); display: block; }
.item-price { font-size: 24rpx; color: var(--color-muted); }
.item-qty { display: flex; align-items: center; gap: 16rpx; margin: 0 24rpx; }
.btn-minus, .btn-plus { width: 44rpx; height: 44rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-minus { background: #F3F4F6; }
.btn-plus { background: var(--color-primary); }
.btn-text { font-size: 24rpx; color: #fff; }
.btn-minus .btn-text { color: var(--color-text); }
.qty { font-size: 28rpx; min-width: 32rpx; text-align: center; }
.item-subtotal { font-size: 28rpx; font-weight: 600; color: var(--color-cta); min-width: 120rpx; text-align: right; }
.drawer-footer { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 32rpx; border-top: 1rpx solid var(--color-border); }
.total { display: flex; align-items: baseline; gap: 8rpx; }
.total-label { font-size: 28rpx; color: var(--color-text); }
.total-value { font-size: 36rpx; font-weight: 700; color: var(--color-cta); }
.submit-btn { background: var(--color-primary); padding: 20rpx 64rpx; border-radius: var(--radius-sm); }
.submit-text { color: #fff; font-size: 30rpx; font-weight: 600; }
</style>
