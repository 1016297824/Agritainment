# 入口登录重定向 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 小程序启动时检测登录状态，未登录跳转"我的"页面引导登录，同时修复管理员登录后无法进入管理后台的 bug。

**Architecture:** 在 App.vue onLaunch 做第一道登录检测，首页 onShow 做兜底检测（含防抖），登录页删除硬编码跳转让 store 内部导航生效，navigateToRoleHome 改用 reLaunch 清空页面栈。

**Tech Stack:** uni-app + Vue 3 + Pinia

---

### Task 1: 修改 auth.js — navigateToRoleHome 改用 reLaunch

**Files:**
- Modify: `client/src/stores/auth.js:48-55`

- [ ] **Step 1: 修改 navigateToRoleHome 方法**

将 `navigateToRoleHome()` 中的 `uni.switchTab` 改为 `uni.reLaunch`，避免登录后返回键回到未登录状态的 profile 页。

```javascript
navigateToRoleHome() {
  const home = getRoleHome(this.role)
  uni.reLaunch({ url: home })
},
```

- [ ] **Step 2: Commit**

```bash
git add client/src/stores/auth.js
git commit -m "fix: navigateToRoleHome 使用 reLaunch 清空页面栈"
```

---

### Task 2: 修改 login/index.vue — 修复导航逻辑 + 修复定时器泄漏 + 错误提示

**Files:**
- Modify: `client/src/pages/login/index.vue:63-84`

- [ ] **Step 1: 修改 handleLogin 方法**

删除 `store.login()` 后的硬编码 `uni.switchTab`（store.login() 内部已调用 navigateToRoleHome()），将 `store.register()` 后的 `uni.switchTab` 替换为 `store.navigateToRoleHome()`，在 register 的 catch 中添加错误提示。

```javascript
async handleLogin() {
  if (!this.phone || !this.code) {
    uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
    return
  }
  try {
    const store = useAuthStore()
    await store.login(this.phone, this.code)
  } catch (e) {
    try {
      const store = useAuthStore()
      await store.register(this.phone, this.code)
      store.navigateToRoleHome()
    } catch (e2) {
      uni.showToast({ title: '登录/注册失败，请重试', icon: 'none' })
    }
  }
},
```

- [ ] **Step 2: 添加 beforeUnmount 清理定时器**

在 `export default` 对象中添加 `beforeUnmount` 生命周期：

```javascript
beforeUnmount() {
  if (this.timer) clearInterval(this.timer)
},
```

- [ ] **Step 3: Commit**

```bash
git add client/src/pages/login/index.vue
git commit -m "fix: 删除登录页硬编码跳转，修复管理员登录 bug 和定时器泄漏"
```

---

### Task 3: 修改 index/index.vue — 首页兜底登录检查

**Files:**
- Modify: `client/src/pages/index/index.vue:59-83`

- [ ] **Step 1: 在 onShow 中添加登录检查（含防抖）**

引入 auth store，在 onShow 中检查登录状态，未登录时跳转"我的"页面。使用防抖标志避免与 App.vue onLaunch 的 switchTab 竞争。

```javascript
import { journalApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

export default {
  data() {
    return {
      journals: [],
      _redirecting: false
    }
  },
  onShow() {
    const store = useAuthStore()
    if (!store.isLoggedIn && !this._redirecting) {
      this._redirecting = true
      uni.switchTab({ url: '/pages/profile/index' })
      setTimeout(() => { this._redirecting = false }, 500)
      return
    }
    this.loadJournals()
  },
```

- [ ] **Step 2: Commit**

```bash
git add client/src/pages/index/index.vue
git commit -m "feat: 首页 onShow 兜底登录检查，防止 onLaunch switchTab 失效"
```

---

### Task 4: 验证 App.vue 已有逻辑正确

**Files:**
- Read: `client/src/App.vue`

- [ ] **Step 1: 确认 App.vue 的 onLaunch 逻辑**

确认 App.vue 已包含正确的登录检测逻辑（之前已实现）：

```javascript
import { useAuthStore } from '@/stores/auth'

export default {
  onLaunch() {
    const store = useAuthStore()
    if (!store.isLoggedIn) {
      uni.switchTab({ url: '/pages/profile/index' })
    }
  },
```

如果已存在则无需修改，跳过此步骤。

---

### Task 5: 手动测试验证

- [ ] **Step 1: 验证未登录启动流程**
清除本地存储的 token，启动小程序，确认跳转到"我的"页面。

- [ ] **Step 2: 验证已登录启动流程**
确保 token 存在，启动小程序，确认正常进入首页。

- [ ] **Step 3: 验证管理员登录**
使用管理员账号登录，确认跳转到管理后台而非首页。

- [ ] **Step 4: 验证登录后返回键**
登录成功后按返回键，确认退出小程序而非回到未登录状态。

- [ ] **Step 5: 验证注册流程**
使用新手机号注册，确认注册成功后跳转到首页。
