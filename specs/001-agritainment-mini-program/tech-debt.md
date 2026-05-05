# 技术债务

> 记录已知但暂未实现的功能、架构缺陷和待优化项。按优先级排序。
> 标记: ✅=已完成, ⏳=待实现, 🚫=暂不处理

## HIGH — 影响核心功能

### TD-001: 后端缺少详情接口 ✅ (2026-05-06)

- **影响页面**: plot-detail.vue, journal/edit.vue
- **已修复**: 新增 `GET /api/v1/planting/plots/:id` 和 `GET /api/v1/journals/:id`
- **提交**: 0732a6a

### TD-002: 后端缺少服务预约查询接口 ✅ (2026-05-06)

- **影响页面**: profile/reservations/index.vue
- **已修复**: 新增 `GET /api/v1/planting/service-orders`，前端预约页改用此接口
- **提交**: f3f9071

### TD-003: uni-app 原生 tabBar 不支持动态角色切换 ✅ (2026-05-06)

- **影响页面**: 全局底部导航
- **已修复**: 实现 CustomTabBar.vue 组件，pages.json 启用 custom:true，11 个 Tab 页面已引入
- **提交**: 1af16ca

## MEDIUM — 影响体验或可维护性

### TD-004: Dish.price 类型为 Double，应改为 BigDecimal ✅ (2026-05-06)

- **已修复**: 9 个实体/DTO 文件 price 字段从 Double 改为 BigDecimal，Service 层运算同步更新
- **提交**: d1b6cc7

### TD-005: 乐观锁形同虚设 ✅ (2026-05-06)

- **已修复**: DiningService/ProductService 扣库存改用 updateById，乐观锁 WHERE version=? 自动生效
- **提交**: d1b6cc7

### TD-006: schema.sql 索引创建被注释掉 ✅ (2026-05-06)

- **已修复**: 新增 IndexInitializer.java，应用启动后自动检查并创建缺失索引
- **提交**: 1af16ca

### TD-007: 图片上传未实现 OSS 🚫

- **现状**: 图片上传到服务器本地 `uploads/` 目录，单机部署可用，但无法水平扩展
- **方案**: 接入阿里云 OSS / 腾讯云 COS
- **涉及文件**: FileController.java
- **决策**: 暂不处理，MVP 阶段单机部署足够

## LOW — 功能缺失但不阻塞核心流程

### TD-008: 支付服务未实现 ⏳

- **现状**: 下单后无实际支付流程，订单直接创建
- **方案**: 接入微信支付 API
- **对应任务**: T091

### TD-009: 通知服务未实现 ⏳

- **现状**: 预约成功、订单状态变更等无通知
- **方案**: 接入微信模板消息 / 订阅消息
- **对应任务**: T092

### TD-010: 定时任务未实现 ⏳

- **现状**: 优惠券过期、订单超时取消等无自动处理
- **方案**: Spring @Scheduled 或 Quartz
- **对应任务**: T093

### TD-011: 管理后台 ✅ (2026-05-06)

- **已实现**: dashboard(统计)、users(客户/员工管理)、business(菜品/产品增删)、system(地块/会员配置)
- **对应任务**: T104-T107

---

*最后更新: 2026-05-06*
