import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mockUni, clearUniCalls } from './helpers/uni-mock'
import appOptions from '@/App.vue'
import { useAuthStore } from '@/stores/auth'

function createAppInstance() {
  const vm = {}
  vm.onLaunch = appOptions.onLaunch.bind(vm)
  return vm
}

describe('App onLaunch 登录状态检测', () => {
  beforeEach(() => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
  })

  it('未登录时应 switchTab 到个人中心', () => {
    const vm = createAppInstance()
    vm.onLaunch()
    expect(uni.switchTab).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/profile/index' })
    )
  })

  it('已登录时不应调用 switchTab', () => {
    const store = useAuthStore()
    store.token = 'valid-token'
    store.userInfo = { id: 1, role: 'customer' }

    const vm = createAppInstance()
    vm.onLaunch()
    expect(uni.switchTab).not.toHaveBeenCalled()
  })

  it('switchTab 失败时应有 fail 回调', () => {
    const vm = createAppInstance()
    vm.onLaunch()
    const call = uni.switchTab.mock.calls[0]
    expect(call[0]).toHaveProperty('fail')
    expect(typeof call[0].fail).toBe('function')
  })
})
