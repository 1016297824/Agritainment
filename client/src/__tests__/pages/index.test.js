import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mockUni, clearUniCalls } from '../helpers/uni-mock'

vi.mock('@/api', () => ({
  journalApi: {
    getSharedJournals: vi.fn().mockResolvedValue([])
  }
}))

import { useAuthStore } from '@/stores/auth'
import indexPage from '@/pages/index/index.vue'

function createPageInstance() {
  const vm = {}
  const options = indexPage
  vm.journals = []
  vm._redirecting = false
  if (options.methods) {
    Object.keys(options.methods).forEach((key) => {
      vm[key] = options.methods[key].bind(vm)
    })
  }
  vm.onShow = options.onShow.bind(vm)
  return vm
}

describe('index onShow 兜底登录检查', () => {
  beforeEach(() => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
  })

  it('未登录时应 switchTab 到个人中心', () => {
    const vm = createPageInstance()
    vm.onShow()
    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/profile/index' })
  })

  it('未登录时不应调用 loadJournals', () => {
    const vm = createPageInstance()
    const loadSpy = vi.fn()
    vm.loadJournals = loadSpy
    vm.onShow()
    expect(loadSpy).not.toHaveBeenCalled()
  })

  it('未登录时应设置 _redirecting 防抖标志', () => {
    const vm = createPageInstance()
    vm.onShow()
    expect(vm._redirecting).toBe(true)
  })

  it('已登录时应调用 loadJournals 而非跳转', () => {
    const store = useAuthStore()
    store.token = 'valid-token'
    store.userInfo = { id: 1, role: 'customer' }

    const vm = createPageInstance()
    const loadSpy = vi.fn()
    vm.loadJournals = loadSpy
    vm.onShow()

    expect(uni.switchTab).not.toHaveBeenCalled()
    expect(loadSpy).toHaveBeenCalled()
  })

  it('已登录时不应设置 _redirecting 标志', () => {
    const store = useAuthStore()
    store.token = 'valid-token'
    store.userInfo = { id: 1, role: 'customer' }

    const vm = createPageInstance()
    vm.onShow()
    expect(vm._redirecting).toBe(false)
  })

  it('_redirecting 为 true 时不应重复跳转', () => {
    const vm = createPageInstance()
    vm._redirecting = true
    vm.onShow()
    expect(uni.switchTab).not.toHaveBeenCalled()
  })

  it('_redirecting 防抖应在 500ms 后重置', () => {
    vi.useFakeTimers()
    const vm = createPageInstance()
    vm.onShow()
    expect(vm._redirecting).toBe(true)
    vi.advanceTimersByTime(500)
    expect(vm._redirecting).toBe(false)
    vi.useRealTimers()
  })
})
