import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mockUni, clearUniCalls } from '../helpers/uni-mock'

vi.mock('@/api', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    sendSmsCode: vi.fn(),
    getMe: vi.fn(),
    wxLogin: vi.fn()
  }
}))

import { authApi } from '@/api'

describe('login handleLogin 逻辑', () => {
  let useAuthStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    authApi.login.mockReset()
    authApi.register.mockReset()
    const mod = await import('@/stores/auth')
    useAuthStore = mod.useAuthStore
  })

  it('customer 登录成功后 store.login 内部应 switchTab 到首页', async () => {
    authApi.login.mockResolvedValue({
      token: 'test-token',
      user: { id: 1, role: 'customer' }
    })
    authApi.wxLogin.mockResolvedValue({})

    const store = useAuthStore()
    await store.login('13800138000', '123456')

    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/index/index' })
  })

  it('admin 登录成功后应 reLaunch 到管理后台', async () => {
    authApi.login.mockResolvedValue({
      token: 'admin-token',
      user: { id: 2, role: 'admin' }
    })
    authApi.wxLogin.mockResolvedValue({})

    const store = useAuthStore()
    await store.login('13800138001', '123456')

    expect(uni.reLaunch).toHaveBeenCalledWith({ url: '/pages/admin/dashboard/index' })
  })

  it('customer 注册成功后 navigateToRoleHome 应 switchTab 到首页', async () => {
    authApi.register.mockResolvedValue({
      token: 'new-token',
      user: { id: 3, role: 'customer' }
    })
    authApi.wxLogin.mockResolvedValue({})

    const store = useAuthStore()
    await store.register('13800138002', '123456')
    store.navigateToRoleHome()

    expect(uni.switchTab).toHaveBeenCalledWith({ url: '/pages/index/index' })
  })

  it('登录和注册都失败时不应调用任何导航', async () => {
    authApi.login.mockRejectedValue(new Error('login failed'))
    authApi.register.mockRejectedValue(new Error('register failed'))

    const store = useAuthStore()

    try { await store.login('13800138000', '000000') } catch (e) {}
    try { await store.register('13800138000', '000000') } catch (e) {}

    expect(uni.reLaunch).not.toHaveBeenCalled()
    expect(uni.switchTab).not.toHaveBeenCalled()
  })

  it('customer 登录成功后不应使用 reLaunch（因为首页是 tabBar 页面）', async () => {
    authApi.login.mockResolvedValue({
      token: 'test-token',
      user: { id: 1, role: 'customer' }
    })
    authApi.wxLogin.mockResolvedValue({})

    const store = useAuthStore()
    await store.login('13800138000', '123456')

    expect(uni.reLaunch).not.toHaveBeenCalled()
  })
})
