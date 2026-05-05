# 技术债务

> 记录已知但暂未实现的功能、架构缺陷和待优化项。按优先级排序。

## HIGH — 影响核心功能

### TD-001: 后端缺少详情接口

- **影响页面**: plot-detail.vue, journal/edit.vue
- **现状**: 前端通过列表 API 获取全部数据后 `find(id)` 匹配，数据量大时性能差
- **需新增接口**:
  - `GET /api/v1/planting/plots/:id` — 地块详情
  - `GET /api/v1/journals/:id` — 日记详情
- **涉及文件**: PlotController, JournalController, 对应 Service 和 Mapper

### TD-002: 后端缺少服务预约查询接口

- **影响页面**: profile/reservations/index.vue
- **现状**: 服务预约从 locked 状态的优惠券中硬构造，状态为硬编码 "confirmed"
- **需新增接口**: `GET /api/v1/coupons/service-reservations` 或 `GET /api/v1/planting/service-orders`
- **涉及文件**: CouponController 或新建 ServiceOrderController

### TD-003: uni-app 原生 tabBar 不支持动态角色切换

- **影响页面**: 全局底部导航
- **现状**: tabBar.js 定义了 customer/staff/admin 三套 Tab，但 pages.json 的 tabBar.list 是静态的，只配置了 customer 的 5 个 Tab
- **方案**: 实现自定义 tabBar 组件（uni-app custom-tab-bar），根据角色动态渲染
- **涉及文件**: pages.json, 新建 components/CustomTabBar.vue, 各 Tab 页面

## MEDIUM — 影响体验或可维护性

### TD-004: Dish.price 类型为 Double，应改为 BigDecimal

- **现状**: 数据库 DECIMAL(10,2)，Java 实体 Double，存在浮点精度丢失
- **风险**: 订单金额计算可能不精确（如 68.00 变 67.999999）
- **涉及文件**: Dish.java, DiningService.java 的 recalcOrderTotal

### TD-005: 乐观锁形同虚设

- **现状**: Dish 和 Product 有 `@Version` 注解，但 DiningService 扣库存用 `LambdaUpdateWrapper.setSql()` 绕过了乐观锁
- **风险**: 并发下单时库存可能超卖
- **涉及文件**: DiningService.java:145-148

### TD-006: schema.sql 索引创建被注释掉

- **现状**: MySQL 不支持 `CREATE INDEX IF NOT EXISTS`，Spring Boot SQL init 不支持 `DELIMITER`/存储过程，索引创建语句被注释
- **影响**: 数据量增长后查询性能下降
- **方案**: 迁移到 Flyway/Liquibase 管理数据库版本，或在应用启动后用 Java 代码检查并创建索引
- **涉及文件**: schema.sql

### TD-007: 图片上传未实现 OSS

- **现状**: 图片上传到服务器本地 `uploads/` 目录，单机部署可用，但无法水平扩展
- **方案**: 接入阿里云 OSS / 腾讯云 COS
- **涉及文件**: FileController.java

## LOW — 功能缺失但不阻塞核心流程

### TD-008: 支付服务未实现

- **现状**: 下单后无实际支付流程，订单直接创建
- **方案**: 接入微信支付 API

### TD-009: 通知服务未实现

- **现状**: 预约成功、订单状态变更等无通知
- **方案**: 接入微信模板消息 / 订阅消息

### TD-010: 定时任务未实现

- **现状**: 优惠券过期、订单超时取消等无自动处理
- **方案**: Spring @Scheduled 或 Quartz

### TD-011: 管理后台未实现

- **现状**: 只有 admin 角色登录，无管理页面
- **需要**: 菜品管理、订单管理、用户管理、数据统计等

---

*最后更新: 2026-05-06*
