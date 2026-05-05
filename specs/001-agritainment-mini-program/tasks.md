# 任务清单 - 农家乐小程序

**项目**: 农家乐小程序
**日期**: 2026-05-05
**最后更新**: 2026-05-05
**MVP范围**: Phase 1-6 (账户+餐饮+产品售卖核心流程)

> **实现说明**: 项目使用 CommonJS (.js) 而非 TypeScript (.ts)；业务逻辑直接写在路由文件中（未拆分 service 层），符合最小开发原则。
> 标记: [x]=已完成, [~]=功能已实现但结构与原计划不同, [ ]=未实现

---

## Phase 1: 项目初始化

- [x] T001 初始化后端项目 server/ (Node.js + Express + CommonJS) 含 package.json, .env.example
- [x] T002 初始化前端项目 client/ (Uni-app + Vue 3 + Pinia) 含 package.json, pages.json, manifest.json
- [ ] T003 创建共享类型定义 shared/types/ (User, Table, Dish, Product, Coupon, Plot, Camera, Journal 等接口)
- [x] T004 配置后端数据库连接 (Sequelize + MySQL) server/src/config/database.js
- [x] T005 配置后端 Redis 连接 server/src/config/redis.js
- [x] T006 创建后端目录结构 server/src/{routes,models,middleware,utils,config}
- [x] T007 创建前端目录结构 client/src/{pages,stores,api,styles}

---

## Phase 2: 基础设施 (阻塞性前置)

- [x] T008 实现后端 JWT 认证中间件 server/src/middleware/auth.js
- [x] T009 实现后端角色权限中间件 (customer/staff/admin) server/src/middleware/role.js
- [x] T010 实现后端统一错误处理中间件 server/src/middleware/errorHandler.js
- [ ] T011 实现后端请求验证中间件 (Zod) server/src/middleware/validator.js
- [x] T012 实现前端 API 请求封装 (uni.request + token 拦截) client/src/api/request.js
- [ ] T013 实现前端路由守卫 (角色判断 + 登录检查) client/src/utils/guard.js
- [x] T014 实现前端全局样式和设计系统变量 client/src/styles/variables.css
- [~] T015 数据库表同步: 使用 Sequelize sync 替代迁移文件 (server/src/server.js)
- [ ] T016 创建数据库迁移文件 (如需正式迁移管理)
- [ ] T017 实现图片上传服务 (本地存储 + Nginx) server/src/services/uploadService.js

---

## Phase 3: [US1] 用户注册与认证

**目标**: 客户手机号注册、员工/管理员账户管理
**独立验收**: 用户可注册登录，管理员可管理员工

- [x] T018 [US1] 实现 User 模型 server/src/models/User.js
- [~] T019 [US1] 短信验证码服务 (模拟，逻辑在 auth 路由中)
- [~] T020 [US1] 认证服务 (注册/登录/Token，逻辑在 auth 路由中)
- [x] T021 [US1] 实现认证路由 server/src/routes/auth.js
- [x] T022 [US1] [P] 实现前端登录页面 client/src/pages/login/index.vue
- [~] T023 [US1] [P] 注册功能 (集成在登录页面中，非独立页面)
- [x] T024 [US1] 实现前端认证 Store (Pinia) client/src/stores/auth.js
- [x] T025 [US1] 实现管理员用户管理路由 server/src/routes/admin.js (users 部分)
- [ ] T026 [US1] [P] 实现管理员用户管理页面 client/src/pages/admin/users/index.vue
- [x] T027 [US1] 实现员工管理路由 server/src/routes/admin.js (staff 部分)

---

## Phase 4: [US2] 餐饮 - 桌位预约

**目标**: 客户可预约桌位，员工/管理员可管理
**独立验收**: 完整预约-取消-爽约流程

- [x] T028 [US2] 实现 Table 模型 server/src/models/Table.js
- [x] T029 [US2] 实现 TableReservation 模型 server/src/models/TableReservation.js
- [~] T030 [US2] 桌位预约服务 (含爽约机制，逻辑在 dining 路由中)
- [x] T031 [US2] 实现桌位预约路由 server/src/routes/dining.js
- [x] T032 [US2] 实现管理员桌位管理路由 server/src/routes/admin.js (tables 部分)
- [x] T033 [US2] [P] 实现前端桌位预约页面 client/src/pages/dining/index.vue
- [ ] T034 [US2] [P] 实现前端预约日历组件 client/src/components/ReservationCalendar.vue
- [ ] T035 [US2] [P] 实现员工预约管理页面 client/src/pages/staff/reservations/index.vue
- [ ] T036 [US2] [P] 实现管理员桌位管理页面 client/src/pages/admin/tables/index.vue

---

## Phase 5: [US3] 餐饮 - 扫码点餐

**目标**: 客户扫码点餐，员工管理订单
**独立验收**: 完整点餐-加菜-退菜-结账流程

- [x] T037 [US3] 实现 Dish 模型 server/src/models/Dish.js
- [x] T038 [US3] 实现 Order + OrderItem 模型 server/src/models/Order.js, OrderItem.js
- [~] T039 [US3] 点餐服务 (下单/加菜/退菜/结账/换桌，逻辑在 dining 路由中)
- [x] T040 [US3] 实现点餐路由 server/src/routes/dining.js
- [x] T041 [US3] 实现管理员菜品管理路由 server/src/routes/admin.js (dishes 部分)
- [x] T042 [US3] [P] 实现前端点餐页面 client/src/pages/dining/index.vue
- [ ] T043 [US3] [P] 实现前端菜品列表组件 client/src/components/DishList.vue
- [ ] T044 [US3] [P] 实现前端购物车组件 client/src/components/CartDrawer.vue
- [ ] T045 [US3] [P] 实现员工订单管理页面 client/src/pages/staff/orders/index.vue
- [ ] T046 [US3] [P] 实现管理员菜品管理页面 client/src/pages/admin/dishes/index.vue

---

## Phase 6: [US4] 产品售卖 - 卡券与会员

**目标**: 客户购买产品卡券，会员管理
**独立验收**: 购买-卡券生成-会员折扣完整流程

- [x] T047 [US4] 实现 Product 模型 server/src/models/Product.js
- [x] T048 [US4] 实现 Coupon 模型 server/src/models/Coupon.js
- [x] T049 [US4] 实现 MembershipConfig 模型 server/src/models/MembershipConfig.js
- [~] T050 [US4] 产品服务 (购买/卡券生成/转赠，逻辑在 products/coupons 路由中)
- [~] T051 [US4] 会员服务 (购买/赠送/折扣，逻辑在 membership 路由中)
- [x] T052 [US4] 实现产品路由 server/src/routes/products.js
- [x] T053 [US4] 实现卡券路由 server/src/routes/coupons.js
- [x] T054 [US4] 实现会员路由 server/src/routes/membership.js
- [x] T055 [US4] [P] 实现前端产品列表页面 client/src/pages/products/index.vue
- [ ] T056 [US4] [P] 实现前端产品详情页面 client/src/pages/products/detail.vue
- [ ] T057 [US4] [P] 实现前端卡包页面 client/src/pages/profile/coupons/index.vue
- [ ] T058 [US4] [P] 实现前端卡券详情组件 (含二维码) client/src/components/CouponCard.vue
- [ ] T059 [US4] [P] 实现管理员产品管理页面 client/src/pages/admin/products/index.vue
- [ ] T060 [US4] [P] 实现管理员会员配置页面 client/src/pages/admin/membership/index.vue

---

## Phase 7: [US5] 产品售卖 - 服务预约与核销

**目标**: 服务预约+爽约机制+线下扫码核销
**独立验收**: 预约-取消-核销完整流程

- [x] T061 [US5] 实现 ServiceReservation 模型 server/src/models/ServiceReservation.js
- [~] T062 [US5] 服务预约服务 (含爽约机制，逻辑在 coupons 路由中)
- [~] T063 [US5] 核销服务 (扫码/标识码验证，逻辑在 coupons 路由中)
- [x] T064 [US5] 实现服务预约路由 server/src/routes/coupons.js (service-reservations 部分)
- [x] T065 [US5] 实现核销路由 server/src/routes/coupons.js (verify 部分)
- [ ] T066 [US5] [P] 实现前端服务预约页面 client/src/pages/products/reservation/index.vue
- [ ] T067 [US5] [P] 实现员工扫码核销页面 client/src/pages/staff/scan/index.vue
- [ ] T068 [US5] [P] 实现员工预约管理页面 client/src/pages/staff/service-reservations/index.vue

---

## Phase 8: [US6] 种植体验

**目标**: 地块管理、种植服务下单、摄像头接口预留
**独立验收**: 租用地块-下单管理服务-摄像头接口可用

- [x] T069 [US6] 实现 Plot 模型 server/src/models/Plot.js
- [x] T070 [US6] 实现 Camera + CameraPlotBinding 模型 server/src/models/Camera.js, CameraPlotBinding.js
- [x] T071 [US6] 实现 GardenService + GardenServiceOrder 模型 server/src/models/GardenService.js, GardenServiceOrder.js
- [x] T072 [US6] 实现 CameraQueue 模型 server/src/models/CameraQueue.js
- [~] T073 [US6] 地块服务 (租用/绑定，逻辑在 planting 路由中)
- [~] T074 [US6] 种植管理服务 (下单/通知/完成，逻辑在 planting 路由中)
- [~] T075 [US6] 摄像头服务 (预留接口，逻辑在 planting 路由中)
- [x] T076 [US6] 实现种植体验路由 server/src/routes/planting.js
- [x] T077 [US6] 实现管理员地块管理路由 server/src/routes/admin.js (plots 部分)
- [x] T078 [US6] 实现管理员摄像头管理路由 server/src/routes/admin.js (cameras 部分)
- [x] T079 [US6] [P] 实现前端种植体验首页 client/src/pages/planting/index.vue
- [ ] T080 [US6] [P] 实现前端地块详情页面 client/src/pages/planting/plot-detail.vue
- [ ] T081 [US6] [P] 实现前端种植服务下单页面 client/src/pages/planting/service-order.vue
- [ ] T082 [US6] [P] 实现管理员地块管理页面 client/src/pages/admin/plots/index.vue
- [ ] T083 [US6] [P] 实现管理员摄像头管理页面 client/src/pages/admin/cameras/index.vue

---

## Phase 9: [US7] 首页与个人中心

**目标**: 首页展示、个人空间、日志分享
**独立验收**: 首页可浏览农场信息和日志，个人空间可管理卡券和日志

- [x] T084 [US7] 实现 Journal 模型 server/src/models/Journal.js
- [~] T085 [US7] 日志服务 (创建/编辑/分享，逻辑在 journals 路由中)
- [x] T086 [US7] 实现日志路由 server/src/routes/journals.js
- [x] T087 [US7] [P] 实现前端首页 client/src/pages/index/index.vue
- [x] T088 [US7] [P] 实现前端个人中心页面 client/src/pages/profile/index.vue
- [ ] T089 [US7] [P] 实现前端日志编辑页面 client/src/pages/profile/journal/edit.vue
- [ ] T090 [US7] [P] 实现前端日志列表组件 client/src/components/JournalCard.vue

---

## Phase 10: 收尾与跨模块

- [ ] T091 实现微信支付集成 (预留接口) server/src/services/paymentService.js
- [ ] T092 实现微信模板消息/订阅消息 server/src/services/notificationService.js
- [ ] T093 实现定时任务 (每日库存重置、预约超时处理) server/src/services/cronService.js
- [~] T094 实现前端 TabBar 导航 client/src/pages.json (客户TabBar已完成，需补充角色切换)
- [ ] T095 配置 Nginx 反向代理和静态文件服务
- [ ] T096 种子数据脚本 (初始管理员、示例菜品、示例产品) server/src/seeders/
- [ ] T097 前端全局组件注册和页面路由配置
- [ ] T098 端到端流程验证 (注册→预约→点餐→购买→核销)

---

## Phase 11: 角色路由与员工/管理员页面 (brainstorming确认后新增)

> 以下任务基于 brainstorming 确认的"单小程序 + 角色路由"方案

- [ ] T099 实现角色 TabBar 切换工具 client/src/utils/tabBar.js
- [ ] T100 更新 auth store 添加角色切换后跳转逻辑 client/src/stores/auth.js
- [ ] T101 实现员工扫码核销页面 client/src/pages/staff/scan/index.vue
- [ ] T102 实现员工预约管理页面 client/src/pages/staff/reservations/index.vue
- [ ] T103 实现员工订单管理页面 client/src/pages/staff/orders/index.vue
- [ ] T104 实现管理员仪表盘页面 client/src/pages/admin/dashboard/index.vue
- [ ] T105 实现管理员人员管理页面 client/src/pages/admin/users/index.vue
- [ ] T106 实现管理员业务管理页面 (菜品/桌位/产品) client/src/pages/admin/business/index.vue
- [ ] T107 实现管理员系统管理页面 (地块/摄像头/会员) client/src/pages/admin/system/index.vue
- [ ] T108 更新 pages.json 添加员工/管理员页面路由
- [ ] T109 实现前端路由守卫 (角色判断 + 登录检查) client/src/utils/guard.js

---

## 依赖关系

```
Phase 1 (初始化) ✅
  └── Phase 2 (基础设施) 🟡
        ├── Phase 3 [US1] (认证) 🟡
        │     ├── Phase 4 [US2] (桌位预约) 🟡
        │     ├── Phase 5 [US3] (点餐) 🟡
        │     ├── Phase 6 [US4] (卡券会员) 🟡
        │     │     └── Phase 7 [US5] (预约核销) 🟡
        │     ├── Phase 8 [US6] (种植体验) 🟡
        │     └── Phase 9 [US7] (首页个人中心) 🟡
        ├── Phase 10 (收尾)
        └── Phase 11 (角色路由+员工/管理员页面) ← 当前重点
```

## 并行机会

- Phase 4/5/6/8/9 的前端页面可并行开发（不同文件，无依赖）
- Phase 4/5/6 的后端服务可并行开发（不同模型，无依赖）
- Phase 7 依赖 Phase 6（卡券系统）
- Phase 11 中 T101/T102/T103（员工页面）可并行，T104/T105/T106/T107（管理员页面）可并行

## MVP 范围

**Phase 1-6 + Phase 11**: 账户 + 餐饮 + 产品售卖 + 角色路由核心流程
这是最小可交付产品，三种角色均可完成核心操作。

## 进度统计

| 状态 | 数量 | 占比 |
|------|------|------|
| ✅ 已完成 | 47 | 43% |
| 🟡 功能已实现(结构不同) | 14 | 13% |
| ❌ 未实现 | 48 | 44% |
| **总计** | **109** | 100% |
