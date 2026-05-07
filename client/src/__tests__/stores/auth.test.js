import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mockUni, clearUniCalls } from '../helpers/uni-mock'

describe('auth store - navigateToRoleHome', () => {
  let useAuthStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/auth')
    useAuthStore = mod.useAuthStore
  })

  it('customer 角色应 switchTab 到首页（tabBar 页面）', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.userInfo = { role: 'customer', id: 1 }
    store.navigateToRoleHome()
    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/index/index' })
    expect(uni.reLaunch).not.toHaveBeenCalled()
  })

  it('staff 角色应 switchTab 到首页（tabBar 页面）', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.userInfo = { role: 'staff', id: 2 }
    store.navigateToRoleHome()
    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/index/index' })
    expect(uni.reLaunch).not.toHaveBeenCalled()
  })

  it('admin 角色应 reLaunch 到管理后台（非 tabBar 页面）', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.userInfo = { role: 'admin', id: 3 }
    store.navigateToRoleHome()
    expect(uni.reLaunch).toHaveBeenCalledWith({ url: '/pages/admin/dashboard/index' })
    expect(uni.switchTab).not.toHaveBeenCalled()
  })

  it('未设置角色时默认 switchTab 到首页（tabBar 页面）', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.userInfo = { id: 4 }
    store.navigateToRoleHome()
    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/index/index' })
  })

  it('不应使用 redirectTo', () => {
    const store = useAuthStore()
    store.token = 'test-token'
    store.userInfo = { role: 'customer', id: 1 }
    store.navigateToRoleHome()
    expect(uni.redirectTo).not.toHaveBeenCalled()
  })
})

describe('auth store - isLoggedIn', () => {
  let useAuthStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/auth')
    useAuthStore = mod.useAuthStore
  })

  it('有 token 时 isLoggedIn 为 true', () => {
    const store = useAuthStore()
    store.token = 'valid-token'
    expect(store.isLoggedIn).toBe(true)
  })

  it('无 token 时 isLoggedIn 为 false', () => {
    const store = useAuthStore()
    store.token = ''
    expect(store.isLoggedIn).toBe(false)
  })
})
