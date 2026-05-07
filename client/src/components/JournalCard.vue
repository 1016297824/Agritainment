<template>
  <view class="journal-card" @tap="$emit('tap', journal)">
    <view v-if="journal.images && journal.images.length" class="journal-images">
      <image
        v-for="(img, i) in journal.images.slice(0, 3)"
        :key="i"
        class="journal-img"
        :src="img"
        mode="aspectFill"
      />
    </view>
    <text class="journal-title">{{ journal.title }}</text>
    <text class="journal-content">{{ journal.content }}</text>
    <view class="journal-footer">
      <view class="journal-meta">
        <text class="journal-author">{{ journal.User?.nickname || '匿名' }}</text>
        <text class="journal-date">{{ formatDate(journal.createdAt) }}</text>
      </view>
      <view v-if="journal.isShared" class="shared-badge">
        <text class="shared-text">已分享</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  journal: { type: Object, required: true }
})

defineEmits(['tap'])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.journal-card { background: var(--color-surface); border-radius: var(--radius-md); padding: 28rpx; margin-bottom: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.journal-images { display: flex; gap: 8rpx; margin-bottom: 16rpx; }
.journal-img { width: 200rpx; height: 200rpx; border-radius: var(--radius-sm); flex-shrink: 0; }
.journal-title { font-size: 30rpx; font-weight: 600; color: var(--color-text); display: block; margin-bottom: 8rpx; }
.journal-content { font-size: 26rpx; color: var(--color-muted); display: block; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.journal-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; }
.journal-meta { display: flex; gap: 16rpx; }
.journal-author { font-size: 24rpx; color: var(--color-text); }
.journal-date { font-size: 24rpx; color: var(--color-muted); }
.shared-badge { background: #D1FAE5; padding: 4rpx 12rpx; border-radius: 8rpx; }
.shared-text { font-size: 22rpx; color: var(--color-primary); }
</style>
