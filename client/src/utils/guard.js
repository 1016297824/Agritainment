const whiteList = ['/pages/login/index']

export function checkAuth() {
  const token = uni.getStorageSync('token')
  if (!token) {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    if (current && current.route !== 'pages/login/index') {
      uni.redirectTo({ url: '/pages/login/index' })
    }
    return false
  }
  return true
}

export function checkRole(allowedRoles) {
  const userInfo = uni.getStorageSync('userInfo')
  if (!userInfo || !userInfo.role) {
    uni.navigateTo({ url: '/pages/login/index' })
    return false
  }
  if (allowedRoles && !allowedRoles.includes(userInfo.role)) {
    uni.showToast({ title: '权限不足', icon: 'none' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
    return false
  }
  return true
}

export function navigateWithGuard(url, options = {}) {
  if (whiteList.some(w => url.startsWith(w))) {
    uni.navigateTo({ url, ...options })
    return
  }
  if (!checkAuth()) return
  uni.navigateTo({ url, ...options })
}

export function requireLogin() {
  return checkAuth()
}

export function requireRole(...roles) {
  return checkAuth() && checkRole(roles)
}
