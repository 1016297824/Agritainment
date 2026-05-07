const uniCalls = {
  switchTab: [],
  reLaunch: [],
  redirectTo: [],
  navigateTo: [],
  showToast: [],
  setStorageSync: [],
  getStorageSync: {},
  removeStorageSync: [],
  login: []
}

export function mockUni() {
  const uni = {
    switchTab: vi.fn((opts) => {
      uniCalls.switchTab.push(opts)
    }),
    reLaunch: vi.fn((opts) => {
      uniCalls.reLaunch.push(opts)
    }),
    redirectTo: vi.fn((opts) => {
      uniCalls.redirectTo.push(opts)
    }),
    navigateTo: vi.fn((opts) => {
      uniCalls.navigateTo.push(opts)
    }),
    showToast: vi.fn((opts) => {
      uniCalls.showToast.push(opts)
    }),
    setStorageSync: vi.fn((key, val) => {
      uniCalls.setStorageSync.push({ key, val })
      uniCalls.getStorageSync[key] = val
    }),
    getStorageSync: vi.fn((key) => {
      return uniCalls.getStorageSync[key] || ''
    }),
    removeStorageSync: vi.fn((key) => {
      uniCalls.removeStorageSync.push(key)
      delete uniCalls.getStorageSync[key]
    }),
    login: vi.fn(() => Promise.resolve([null, { code: 'mock-wx-code' }]))
  }

  vi.stubGlobal('uni', uni)
  return uni
}

export function clearUniCalls() {
  uniCalls.switchTab = []
  uniCalls.reLaunch = []
  uniCalls.redirectTo = []
  uniCalls.navigateTo = []
  uniCalls.showToast = []
  uniCalls.setStorageSync = []
  uniCalls.getStorageSync = {}
  uniCalls.removeStorageSync = []
  uniCalls.login = []
}

export function getUniCalls() {
  return uniCalls
}

export function setStorageItems(items) {
  Object.assign(uniCalls.getStorageSync, items)
}
