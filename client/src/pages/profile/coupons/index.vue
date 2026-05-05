<template>
  <view class="coupons-page">
    <view v-if="coupons.length === 0" class="empty">
      <text class="empty-text">暂无卡券</text>
    </view>
    <CouponCard
      v-for="item in coupons"
      :key="item.id"
      :coupon="item"
      @transfer="handleTransfer"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { couponApi } from '@/api'
import CouponCard from '@/components/CouponCard.vue'

const coupons = ref([])

const loadCoupons = async () => {
  try {
    coupons.value = await couponApi.getCoupons()
  } catch (e) {}
}

onMounted(loadCoupons)

const handleTransfer = (coupon) => {
  uni.showModal({
    title: '转赠卡券',
    editable: true,
    placeholderText: '输入对方用户ID',
    success: async (e) => {
      if (e.confirm && e.content) {
        try {
          await couponApi.transfer(coupon.id, Number(e.content))
          uni.showToast({ title: '转赠成功', icon: 'success' })
          loadCoupons()
        } catch (err) {}
      }
    }
  })
}
</script>

<style scoped>
.coupons-page { padding: 24rpx; min-height: 100vh; background: var(--color-background); }
.empty { text-align: center; padding: 120rpx 0; }
.empty-text { color: var(--color-muted); font-size: 28rpx; }
</style>
