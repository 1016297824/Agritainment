<template>
  <view class="journals-page">
    <view class="actions">
      <view class="create-btn" @tap="goCreate">
        <text class="create-text">写日志</text>
      </view>
    </view>
    <view v-if="journals.length === 0" class="empty">
      <text class="empty-text">暂无日志</text>
    </view>
    <JournalCard
      v-for="item in journals"
      :key="item.id"
      :journal="item"
      @tap="goEdit(item)"
    />
    <view v-for="item in journals" :key="'act-' + item.id" class="journal-actions-row">
      <view class="share-btn" :class="{ shared: item.isShared }" @tap="toggleShare(item)">
        <text class="share-text">{{ item.isShared ? '取消分享' : '分享到首页' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { journalApi } from '@/api'
import JournalCard from '@/components/JournalCard.vue'

const journals = ref([])

const loadJournals = async () => {
  try {
    journals.value = await journalApi.getJournals()
  } catch (e) {}
}

onMounted(loadJournals)

const goCreate = () => {
  uni.navigateTo({ url: '/pages/profile/journal/edit' })
}

const goEdit = (item) => {
  uni.navigateTo({ url: `/pages/profile/journal/edit?id=${item.id}` })
}

const toggleShare = async (item) => {
  try {
    if (item.isShared) {
      await journalApi.unshareJournal(item.id)
    } else {
      await journalApi.shareJournal(item.id)
    }
    uni.showToast({ title: item.isShared ? '已取消分享' : '已分享', icon: 'success' })
    loadJournals()
  } catch (e) {}
}
</script>

<style scoped>
.journals-page { padding: 24rpx; min-height: 100vh; background: var(--color-background); }
.actions { text-align: right; margin-bottom: 24rpx; }
.create-btn { display: inline-block; background: var(--color-primary); padding: 16rpx 32rpx; border-radius: var(--radius-sm); }
.create-text { color: #fff; font-size: 28rpx; font-weight: 500; }
.empty { text-align: center; padding: 120rpx 0; }
.empty-text { color: var(--color-muted); font-size: 28rpx; }
.journal-actions-row { display: flex; justify-content: flex-end; margin-bottom: 24rpx; padding: 0 8rpx; }
.share-btn { padding: 8rpx 24rpx; border-radius: 8rpx; background: #D1FAE5; }
.share-btn.shared { background: #F3F4F6; }
.share-text { font-size: 24rpx; color: var(--color-primary); }
.share-btn.shared .share-text { color: var(--color-muted); }
</style>
