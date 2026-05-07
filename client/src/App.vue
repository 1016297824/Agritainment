<script>
import { useAuthStore } from '@/stores/auth'

export default {
  onLaunch() {
    const store = useAuthStore()
    if (!store.isLoggedIn) {
      uni.switchTab({
        url: '/pages/profile/index',
        fail: (err) => {
          console.warn('[App] switchTab failed in onLaunch:', err)
        }
      })
    }
  },
  onShow() {
  },
  onHide() {
  }
}
</script>

<style>
@import './styles/variables.css';

page {
  background-color: var(--color-background);
  color: var(--color-text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 28rpx;
  line-height: 1.6;
}

.container {
  padding: 24rpx;
}
</style>
