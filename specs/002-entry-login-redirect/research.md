# 研究与技术选型

**项目**: 农家乐小程序 - 入口登录重定向
**日期**: 2026-05-07

---

## 1. 登录状态检测时机

### Decision: App.vue onLaunch 中检测

**Rationale:**
- `onLaunch` 是小程序启动时最早的生命周期，在页面渲染前执行
- 在此处检测可以尽早决定重定向，减少页面闪烁
- `onShow` 会在每次从后台切回前台时触发，不适合做入口重定向（用户可能正在其他页面操作）
- 只需在首次启动时判断，后续切换不需要重新判断

**Alternatives Considered:**
- 首页 onShow 中检测: 会导致首页先渲染再跳转，有闪烁
- 路由拦截器: uni-app 没有全局路由拦截器机制
- 使用 uni-simple-router 等第三方库: 引入额外依赖，过度设计

---

## 2. 未登录重定向方式

### Decision: uni.switchTab 跳转到"我的"页面

**Rationale:**
- "我的"页面（pages/profile/index）是 TabBar 页面，必须使用 `uni.switchTab`
- "我的"页面已有完整的未登录状态 UI（登录提示 + 按钮），无需额外开发
- 使用 `switchTab` 可以保持 TabBar 可见，用户仍可切换到其他页面浏览

**Alternatives Considered:**
- uni.redirectTo 到登录页: 直接跳登录页体验太突兀，用户无法先浏览内容
- uni.reLaunch: 会关闭所有页面，过于激进
- 自定义中间页: 增加额外页面，过度设计

---

## 3. 登录成功后跳转逻辑

### Decision: 修改登录页登录成功后的跳转目标为首页

**Rationale:**
- 当前登录页登录成功后硬编码 `uni.switchTab({ url: '/pages/index/index' })`
- auth store 的 `navigateToRoleHome()` 方法已实现按角色跳转首页的逻辑
- 登录成功后应使用 `navigateToRoleHome()` 而非硬编码首页路径
- 这样管理员登录后会跳转到管理后台，员工登录后也能正确跳转

**Alternatives Considered:**
- 保持硬编码首页: 管理员和员工登录后无法跳转到正确的工作台
- 登录后返回上一页: 未登录时是从入口重定向来的，没有有意义的"上一页"

---

## 4. 页面闪烁问题

### Decision: 使用首页 loading 状态 + 延迟渲染减轻闪烁

**Rationale:**
- 微信小程序的 `onLaunch` 无法完全阻止首页的初始渲染
- 首页是 pages.json 中的第一个页面，小程序启动时一定会先加载
- 可以在首页增加一个初始化检查，如果检测到未登录且是从启动进入的，立即跳转
- 结合 `onLaunch` 中的重定向，闪烁时间极短

**Alternatives Considered:**
- 使用自定义启动页: 需要额外页面和配置，复杂度高
- 修改 pages.json 把登录页放第一个: 会影响已登录用户的体验，每次启动先看到登录页再跳转
