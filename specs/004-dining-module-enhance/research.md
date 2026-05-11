# Research: 餐饮模块功能增强

**Date**: 2026-05-11
**Feature**: 004-dining-module-enhance

## 1. 预约日期独立性 Bug 根因分析

### Decision

去除 `DiningService.createReservation()` 中对 `DiningTable.status` 的直接更新（第90行），预约状态完全由 `TableReservation` 记录通过日期+时段查询决定。

### Rationale

**Bug 根因**: 在 `DiningService.createReservation()` 方法中，第90行：

```java
tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>()
    .eq(DiningTable::getId, tableId).set(DiningTable::getStatus, "reserved"));
```

这行代码在创建预约后将 `DiningTable.status` 直接设为 `"reserved"`。而 `getTables()` 方法中，桌位的 `reserved` 标记是通过查询 `TableReservation` 表按日期+时段过滤得出的——这是正确的逻辑。但桌位级别的 `status` 被改后，可能在其他查询路径（如 getAllTables、getActiveOrder 等）中产生误导。

**影响链**:
1. 用户在"今天中午"预约 A 桌 → `A.status = "reserved"` 
2. 其他用户在"明天中午"查询桌位时，`getTables()` 正确按日期过滤——A 桌仍显示可预约（正确）
3. 但如果 getTables 调用时没有传递 date/timeSlot 参数（当前 controller 接受可选参数），回退逻辑会漏掉日期过滤，此时 A 桌的状态显示异常

**修复方案**: 去除第90行的 `table.status` 更新。其他方法中依赖 `table.status == "dining"` 的逻辑（如 settleOrder、changeTable、deleteTable）保持不变，因为它们操作的是"正在用餐中"状态，与预约状态正交。

### Alternatives Considered

| 方案 | 评估 |
|------|------|
| 增加 DiningTable.reserved_date 字段 | 引入冗余字段，且与 TableReservation 数据重复，违反 DRY 原则 |
| 在 getTables 中增加 fallback 日期过滤 | 治标不治本，其他接口仍可能被污染状态影响 |
| **去除 status 写入** (选定) | 最小改动，预约状态完全由 reservation 表管理，table.status 仅表示 dining/idle |

## 2. 预约与订单关联方案

### Decision

在 `Order` 实体增加 `reservationId` 字段，订单创建接口参数从 `table_qr` 改为 `reservation_id`。

### Rationale

当前 `createOrder` 通过 `table_qr` 查找桌位，这要求用户必须扫描桌位二维码。在预约→点餐的业务流程中，用户已经在预约页完成了桌位选择，不应再要求扫码。

通过 `reservation_id` 关联订单：
- 服务端可从预约记录中获取 `tableId`，无需额外参数
- 可验证预约有效性（status 必须为 pending/checked_in）
- 保持向后兼容：保留原 `table_qr` 参数作为可选（扫码点餐场景仍可用）

### Alternatives Considered

| 方案 | 评估 |
|------|------|
| 保留 table_qr，前端自动填充 | 增加了不必要的复杂度（生成/传递 QR），且预约流程中无 QR 概念 |
| 直接传 table_id | 无法验证用户是否真的有该桌位的预约 |
| **传 reservation_id** (选定) | 一次验证解决关联+鉴权，逻辑内聚 |

## 3. uni-app 页面拆分与导航方案

### Decision

使用 uni-app 的 `pages.json` 配置两个独立页面路由 + Pinia store 管理跨页面状态 + 路由守卫控制访问权限。

### Rationale

- **页面拆分**: 在 `pages/dining/` 下创建 `reservation/index.vue` 和 `order/index.vue`。原 `index.vue` 改造为预约页面的容器/入口。
- **状态共享**: 创建 `stores/dining.js` Pinia store，存储当前预约、购物车、菜品列表等跨页面共享状态
- **流程控制**: 点餐页面的 `onShow` 钩子中检查 store 中是否有有效预约，无则提示并重定向
- **底部导航**: 使用自定义 tabbar 组件（非 uni-app 原生 tabbar，因为是在餐饮模块内部切换）

### Alternatives Considered

| 方案 | 评估 |
|------|------|
| 单页面 + tab 切换 | 不符合"页面拆分"需求，路由和历史栈混乱 |
| 两个独立分包 | 过度设计，餐饮模块规模不需要分包 |
| **两个页面 + Pinia store** (选定) | 符合项目现有架构模式，学习成本低，维护简单 |

## 4. 向后兼容性

### Decision

保持现有 API 接口路径不变，仅调整参数和内部逻辑。

### Rationale

- `GET /api/v1/dining/tables` 保持接口签名，内部逻辑已正确（按日期+时段过滤）
- `POST /api/v1/dining/reservations` 保持接口签名，仅去除 `table.status` 副作用
- `POST /api/v1/dining/orders` 增加 `reservation_id` 可选参数，同时保留 `table_qr`
- `GET /api/v1/dining/dishes` 无需变更

所有修改对现有调用方透明，不影响已部署功能。