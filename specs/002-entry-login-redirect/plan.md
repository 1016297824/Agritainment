# 实现计划：小程序入口登录重定向

**功能**: 入口登录重定向
**日期**: 2026-05-07
**状态**: 待确认

---

## Technical Context

| 项目 | 值 |
|------|-----|
| 前端框架 | uni-app + Vue 3 + Pinia |
| 目标平台 | 微信小程序 (mp-weixin) |
| 状态管理 | Pinia (auth store) |
| 现有认证 | JWT token + auth store isLoggedIn getter |
| 入口页 | pages/index/index |
| 目标重定向页 | pages/profile/index (TabBar 页面) |

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 检查 | 结果 |
|------|------|------|
| I. 最小开发原则 | 仅修改 3 个文件，不引入新依赖 | ✅ 通过 |
| II. 用户体验优先 | 未登录用户直接引导到登录入口，减少迷失 | ✅ 通过 |
| III. 安全底线 | 不改变认证机制，仅调整页面流向 | ✅ 通过 |
| IV. 一致性 | 使用现有 auth store 和 uni-app 标准 API | ✅ 通过 |
| V. 渐进增强 | 不破坏现有已登录用户体验 | ✅ 通过 |

**结论**: 所有原则检查通过，可继续。

---

## Phase 0: Research

已完成，见 [research.md](./research.md)。

关键决策：
1. 在 `App.vue` 的 `onLaunch` 中检测登录状态
2. 未登录时使用 `uni.switchTab` 跳转到"我的"页面
3. 删除 login/index.vue 中的硬编码跳转（store.login() 内部已调用 navigateToRoleHome()）
4. 首页 onShow 增加兜底登录检查
5. register 流程补上 navigateToRoleHome() 调用

---

## Phase 1: Design

### 变更范围

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `client/src/App.vue` | 已修改 | onLaunch 中已有登录状态检测和重定向逻辑 |
| `client/src/pages/login/index.vue` | 修改 | 删除硬编码 switchTab，register 后补 navigateToRoleHome()，修复定时器泄漏和错误提示 |
| `client/src/pages/index/index.vue` | 修改 | onShow 中增加兜底登录检查（含防抖） |
| `client/src/stores/auth.js` | 修改 | navigateToRoleHome() 改用 reLaunch 清空页面栈 |

### 不变更的文件

| 文件 | 原因 |
|------|------|
| `client/src/stores/auth.js` | navigateToRoleHome() 改用 reLaunch，register() 不改动（在调用方补导航） |
| `client/src/pages/profile/index.vue` | 已有未登录状态 UI，无需修改 |
| `client/src/utils/guard.js` | 现有守卫函数不参与此次变更 |
| `client/src/pages.json` | 不修改页面注册顺序 |
| 后端代码 | 无后端变更 |

### 数据模型

无数据库变更。

### API 接口

无新增/修改 API。

---

## 实现步骤

### Step 1: 修改 App.vue - 启动时登录检测（已实现）

`App.vue` 的 `onLaunch` 中已有登录检测逻辑，无需修改：
1. 引入 auth store ✅
2. 检查 `isLoggedIn` 状态 ✅
3. 未登录时 `uni.switchTab` 到 `pages/profile/index` ✅

### Step 2: 修改 login/index.vue - 修复导航逻辑

1. 删除 `store.login()` 后的 `uni.switchTab({ url: '/pages/index/index' })`（store.login() 内部已调用 navigateToRoleHome()）
2. 将 `store.register()` 后的 `uni.switchTab({ url: '/pages/index/index' })` 替换为 `store.navigateToRoleHome()`（store.register() 内部没有调用 navigateToRoleHome()）
3. 在 register 的 catch 中添加错误提示（避免静默吞错）
4. 添加 `beforeUnmount` 清理定时器（防止内存泄漏）

### Step 3: 修改 index/index.vue - 首页兜底登录检查

在首页的 `onShow` 生命周期中：
1. 引入 auth store
2. 检查 `isLoggedIn`，如果未登录则 `uni.switchTab({ url: '/pages/profile/index' })`
3. 添加防抖标志，避免与 onLaunch 的 switchTab 竞争

### Step 4: 修改 navigateToRoleHome() - 使用 reLaunch 清空页面栈

在 `auth.js` 的 `navigateToRoleHome()` 中：
1. 将 `uni.switchTab` 改为 `uni.reLaunch`，避免登录后返回键回到未登录状态的 profile 页

---

## 测试 Checklist

| 场景 | 预期行为 |
|------|----------|
| 首次启动（无 token） | 跳转到"我的"页面 |
| 已登录启动 | 停留在首页 |
| Token 过期启动 | 首页短暂可见后 401 弹出登录 |
| 从分享链接进入非首页 | 首页 onShow 兜底生效 |
| 普通用户登录成功 | 跳转到首页 |
| 管理员登录成功 | 跳转到管理后台 |
| 注册成功 | 跳转到角色首页 |
| 登录后按返回键 | 退出小程序，不回到未登录状态 |
| onLaunch switchTab 失败 | 首页 onShow 兜底生效 |
| register 失败 | 显示错误提示 |

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 首页短暂闪烁 | 低 | onLaunch 中尽早执行重定向 + 首页 onShow 兜底 |
| token 存在但已过期 | 低 | 由现有 401 拦截机制处理 |
| switchTab 在 onLaunch 中可能不生效 | 中 | 首页 onShow 兜底检查 + 防抖标志 |
| 强制登录可能降低新用户转化 | 中 | 当前需求明确，后续可迭代为"先浏览后登录"模式 |
| 其他 TabBar 页面无守卫 | 中 | 当前仅首页和"我的"有守卫，其他页面浏览不强制登录，操作时由 401 拦截 |

---

## 延后项（TODOS）

| 项目 | 原因 |
|------|------|
| 统一使用 guard.js 做全局守卫 | guard.js 用 redirectTo 跳登录页，与当前 switchTab 到 profile 策略不一致，需重构 |
| 401 拦截器流程优化 | 改用 reLaunch 清空页面栈，避免 401 循环，但影响面大 |
| AppTabBar 响应式读取 userInfo | 现有问题，非本次变更引入 |
| logout 后跳转与 onLaunch 统一 | 现有问题，非本次变更引入 |
| 其他 TabBar 页面登录守卫 | 当前允许浏览，操作时由 401 拦截，后续可加 onShow 守卫 |
| token 过期预检 | 现有 401 机制可用，优化可后续迭代 |
| 管理员登录入口在 profile 页 | 影响小，可后续迭代 |

---

<!-- AUTONOMOUS DECISION LOG -->
## Decision Audit Trail

| # | Phase | Decision | Classification | Principle | Rationale | Rejected |
|---|-------|----------|-----------|-----------|----------|----------|
| 1 | CEO | 修正 Step 2：删除 switchTab 而非替换为 navigateToRoleHome() | Mechanical | P5(显式优于巧妙) | store.login() 内部已调用 navigateToRoleHome()，替换会导致双重调用 | 替换方案 |
| 2 | CEO | 增加首页 onShow 兜底登录检查 | Mechanical | P1(完整性) | onLaunch switchTab 在部分机型可能不生效，需兜底 | 仅依赖 onLaunch |
| 3 | CEO | register 后补上 navigateToRoleHome() | Mechanical | P1(完整性) | store.register() 没有调用 navigateToRoleHome()，需在调用方补 | 不处理 register |
| 4 | CEO | 强制登录重定向方向保持不变 | Taste | P6(偏向行动) | 用户已确认需求方向，后续可迭代 | 改为先浏览后登录 |
| 5 | CEO | token 过期体验优化延后 | Mechanical | P3(务实) | 现有 401 机制可用，优化可后续迭代 | 现在增加 token 预检 |
| 6 | CEO | 不重构 store.login() 的导航逻辑 | Mechanical | P3(务实)+P4(DRY) | 改动涉及多文件，风险大于收益 | 从 store 移除导航逻辑 |
| 7 | Eng | 其他 TabBar 页面暂不加守卫 | Mechanical | P3(务实) | 浏览允许，操作时由 401 拦截，后续可迭代 | 所有 TabBar 加守卫 |
| 8 | Eng | guard.js 统一守卫延后 | Mechanical | P3(务实) | guard.js 策略与当前需求不一致，重构超出 blast radius | 现在统一 guard.js |
| 9 | Eng | register 失败添加错误提示 | Mechanical | P1(完整性) | 一行代码修复，在 blast radius 内 | 不处理 |
| 10 | Eng | 登录页定时器泄漏修复 | Mechanical | P1(完整性) | 一行代码修复，在 blast radius 内 | 不处理 |
| 11 | Eng | navigateToRoleHome 改用 reLaunch | Mechanical | P5(显式) | 避免登录后返回键回到未登录状态 | 保持 switchTab |
| 12 | Eng | onShow 守卫加防抖标志 | Mechanical | P5(显式) | 避免与 onLaunch switchTab 竞争 | 不加防抖 |
| 13 | Eng | 401 拦截器流程优化延后 | Mechanical | P3(务实) | 影响面大，可后续迭代 | 现在修改 401 流程 |
| 14 | Eng | AppTabBar 响应式修复延后 | Mechanical | P3(务实) | 现有问题，非本次变更引入 | 现在修复 |
| 15 | Eng | logout 跳转统一延后 | Mechanical | P3(务实) | 现有问题，非本次变更引入 | 现在统一 |
