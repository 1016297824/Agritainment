const customerTabs = [
  { pagePath: '/pages/index/index', text: '首页' },
  { pagePath: '/pages/dining/index', text: '餐饮' },
  { pagePath: '/pages/products/index', text: '产品' },
  { pagePath: '/pages/planting/index', text: '种植' },
  { pagePath: '/pages/profile/index', text: '我的' }
]

const staffTabs = [
  { pagePath: '/pages/index/index', text: '首页' },
  { pagePath: '/pages/staff/scan/index', text: '扫码' },
  { pagePath: '/pages/staff/reservations/index', text: '预约' },
  { pagePath: '/pages/staff/orders/index', text: '订单' },
  { pagePath: '/pages/profile/index', text: '我的' }
]

const adminTabs = [
  { pagePath: '/pages/index/index', text: '首页' },
  { pagePath: '/pages/admin/dashboard/index', text: '管理' },
  { pagePath: '/pages/admin/business/index', text: '业务' },
  { pagePath: '/pages/admin/system/index', text: '系统' },
  { pagePath: '/pages/profile/index', text: '我的' }
]

const tabBarPages = new Set([
  '/pages/index/index',
  '/pages/dining/index',
  '/pages/products/index',
  '/pages/planting/index',
  '/pages/profile/index'
])

export function isTabBarPage(path) {
  return tabBarPages.has(path)
}

export function getRoleTabs(role) {
  switch (role) {
    case 'admin': return adminTabs
    case 'staff': return staffTabs
    default: return customerTabs
  }
}

export function getRoleHome(role) {
  switch (role) {
    case 'admin': return '/pages/admin/dashboard/index'
    case 'staff': return '/pages/index/index'
    default: return '/pages/index/index'
  }
}
