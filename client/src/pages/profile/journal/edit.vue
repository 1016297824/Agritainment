<template>
  <view class="page">
    <view class="section">
      <text class="section-title">{{ isEdit ? '编辑日志' : '写日志' }}</text>

      <view class="form-group">
        <text class="form-label">标题</text>
        <input v-model="form.title" class="form-input" placeholder="给日志起个标题" />
      </view>

      <view class="form-group">
        <text class="form-label">内容</text>
        <textarea v-model="form.content" class="form-textarea" placeholder="记录你的农家乐体验..." />
      </view>

      <view class="form-group">
        <text class="form-label">图片</text>
        <view class="image-list">
          <view v-for="(img, i) in form.images" :key="i" class="image-item">
            <image class="preview-img" :src="img" mode="aspectFill" />
            <view class="remove-img" @tap="removeImage(i)">✕</view>
          </view>
          <view v-if="form.images.length < 9" class="add-image" @tap="chooseImage">
            <text class="add-icon">+</text>
          </view>
        </view>
      </view>
    </view>

    <view class="action-section">
      <view class="btn-row">
        <view class="btn-save" @tap="handleSave">
          <text class="btn-text">保存</text>
        </view>
        <view v-if="!isEdit" class="btn-share" @tap="handleSaveAndShare">
          <text class="btn-text">保存并分享</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { journalApi, uploadApi } from '@/api'

const form = ref({ title: '', content: '', images: [] })
const isEdit = ref(false)
const journalId = ref(null)

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  journalId.value = Number(page.options?.id || 0)

  if (journalId.value) {
    isEdit.value = true
    loadJournal()
  }
})

const loadJournal = async () => {
  try {
    const journals = await journalApi.getJournals()
    const found = journals.find(j => j.id === journalId.value)
    if (found) {
      form.value.title = found.title || ''
      form.value.content = found.content || ''
      form.value.images = found.images || []
    }
  } catch (e) {}
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - form.value.images.length,
    success: async (res) => {
      for (const path of res.tempFilePaths) {
        try {
          const data = await uploadApi.upload(path)
          form.value.images.push(data.url)
        } catch (e) {
          uni.showToast({ title: '图片上传失败', icon: 'none' })
        }
      }
    }
  })
}

const removeImage = (index) => {
  form.value.images.splice(index, 1)
}

const handleSave = async () => {
  if (!form.value.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  try {
    if (isEdit.value) {
      await journalApi.updateJournal(journalId.value, form.value.title, form.value.content, form.value.images)
    } else {
      await journalApi.createJournal(form.value.title, form.value.content, form.value.images)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (e) {}
}

const handleSaveAndShare = async () => {
  if (!form.value.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  try {
    const data = await journalApi.createJournal(form.value.title, form.value.content, form.value.images)
    const id = data?.id || data
    if (id) {
      await journalApi.shareJournal(id)
    }
    uni.showToast({ title: '保存并分享成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (e) {}
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--color-background); padding-bottom: 140rpx; }
.section { padding: 32rpx; }
.section-title { font-size: 36rpx; font-weight: 700; color: var(--color-text); margin-bottom: 32rpx; display: block; }
.form-group { margin-bottom: 32rpx; }
.form-label { font-size: 28rpx; font-weight: 500; color: var(--color-text); display: block; margin-bottom: 12rpx; }
.form-input { background: var(--color-surface); border-radius: var(--radius-sm); padding: 24rpx; font-size: 28rpx; border: 2rpx solid var(--color-border); }
.form-textarea { background: var(--color-surface); border-radius: var(--radius-sm); padding: 24rpx; font-size: 28rpx; border: 2rpx solid var(--color-border); height: 300rpx; }
.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.image-item { position: relative; width: 200rpx; height: 200rpx; }
.preview-img { width: 100%; height: 100%; border-radius: var(--radius-sm); }
.remove-img { position: absolute; top: -8rpx; right: -8rpx; width: 40rpx; height: 40rpx; background: var(--color-error); color: #fff; border-radius: 50%; font-size: 22rpx; display: flex; align-items: center; justify-content: center; }
.add-image { width: 200rpx; height: 200rpx; border: 2rpx dashed var(--color-border); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; background: var(--color-surface); }
.add-icon { font-size: 64rpx; color: var(--color-muted); }
.action-section { position: fixed; bottom: 0; left: 0; right: 0; padding: 32rpx; background: var(--color-surface); box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06); }
.btn-row { display: flex; gap: 16rpx; }
.btn-save { flex: 1; background: var(--color-primary); border-radius: var(--radius-sm); padding: 28rpx; text-align: center; }
.btn-share { flex: 1; background: var(--color-cta); border-radius: var(--radius-sm); padding: 28rpx; text-align: center; }
.btn-text { color: #fff; font-size: 30rpx; font-weight: 600; }
</style>
