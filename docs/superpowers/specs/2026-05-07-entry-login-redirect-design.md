# 小程序入口登录重定向设计

**日期**: 2026-05-07
**状态**: 已确认

---

## 需求

进入小程序时判定登录状态：已登录进入首页，未登录进入"我的"页面引导登录。

## 现状分析

- 入口页 `pages/index/index` 无登录拦截
- `App.vue` 的 `onLaunch` 仅 console.log，无逻辑
- "我的"页面已有未登录 UI（登录提示 + 按钮）
- 登录页 `handleLogin()` 硬编码 `uni.switchTab` 到首页，覆盖了 `store.login()` 内部的 `navigateToRoleHome()`

## Bug 修复

管理员登录后无法进入管理后台：`store.login()` 调用 `navigateToRoleHome()` 对 admin 使用 `redirectTo` 跳转管理后台，但 `handleLogin()` 随后又执行 `switchTab` 到首页，覆盖了跳转。

## 设计

### 变更 1: App.vue 启动登录检测

在 `onLaunch` 中引入 auth store，检查 `isLoggedIn`：
- 未登录 → `uni.switchTab({ url: '/pages/profile/index' })`
- 已登录 → 不操作，保持默认进入首页

### 变更 2: 登录页跳转修复

删除 `login/index.vue` 中 `handleLogin()` 的两处硬编码跳转：
- `store.login()` 后的 `uni.switchTab({ url: '/pages/index/index' })`
- `store.register()` 后的 `uni.switchTab({ url: '/pages/index/index' })`

跳转完全由 `store.login()` 内部的 `navigateToRoleHome()` 处理，实现按角色跳转。

### 不变更

- `auth.js`：已有 `navigateToRoleHome()` 按角色跳转
- `profile/index.vue`：已有未登录 UI
- `admin/login/index.vue`：已正确使用 `store.adminLogin()`

## 影响范围

2 个文件，约 5 行代码变更。无后端变更，无数据库变更。
