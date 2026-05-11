# Tasks: 餐饮模块功能增强

**Feature**: 004-dining-module-enhance | **Branch**: 005-modify-catering
**Date**: 2026-05-11 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Implementation Strategy

MVP 范围（Phase 1-3）: 修复预约日期独立性 bug → 基础点餐功能 → 页面拆分。
推荐顺序执行（依赖关系紧密），每个 Phase 完成后独立验证。

---

## Phase 1: Setup

> 基础设施准备——数据库迁移和数据清理

- [ ] T001 Run DB migration: `ALTER TABLE orders ADD COLUMN reservation_id BIGINT DEFAULT NULL; ALTER TABLE orders ADD INDEX idx_reservation_id (reservation_id);` in MySQL
- [ ] T002 Run cleanup SQL: `UPDATE tables SET status = 'idle' WHERE status = 'reserved';`

---

## Phase 2: Foundational (Blocking Prerequisites)

> 后端实体和 DTO 变更——所有 User Story 的前置依赖

- [ ] T003 [P] Add `reservationId` field to Order entity in `server/src/main/java/com/agritainment/entity/Order.java`
- [ ] T004 [P] Add `reservation_id` field to CreateOrderRequest DTO in `server/src/main/java/com/agritainment/dto/CreateOrderRequest.java`
- [ ] T005 Update dining API module to support new parameters in `client/src/api/dining.js`

**Checkpoint**: 所有 User Story 可以并行开始

---

## Phase 3: User Story 1 - 日期独立的桌位预约 (P1)

**Goal**: 修复"一个桌位被预定则所有日期均显示被预定"的 bug，确保每个桌位在不同日期的预约状态相互独立

**Independent Test**: 预约A桌今天中午 → 切换到明天中午 → A桌仍可预约 → 切换到今晚 → A桌仍可预约

### Implementation

- [ ] T006 [US1] Remove `tableMapper.update(...status="reserved")` in `createReservation()` in `server/src/main/java/com/agritainment/service/DiningService.java` (line 90)
- [ ] T007 [US1] Remove `tableMapper.update(...status="idle")` in `cancelReservation()` in `server/src/main/java/com/agritainment/service/DiningService.java` (line 114)
- [ ] T008 [US1] Verify `getTables()` uses `reservationDate` + `timeSlot` filtering correctly in `server/src/main/java/com/agritainment/service/DiningService.java` (lines 38-60)

**Checkpoint**: 桌位在不同日期的预约状态完全独立

---

## Phase 4: User Story 2 - 点餐功能实现 (P2)

**Goal**: 实现预约→点餐业务闭环，用户完成预约后可进入点餐页面选菜下单

**Independent Test**: 完成预约 → 点餐页面显示预约桌位信息 → 加菜到购物车 → 调整数量 → 提交订单成功

### Implementation

- [ ] T009 [US2] Modify `createOrder()` to support `reservation_id` parameter, resolve table from reservation in `server/src/main/java/com/agritainment/service/DiningService.java` (lines 138-178)
- [ ] T010 [US2] Create dining Pinia store with state: currentReservation, cart, dishes; actions: fetchReservation, fetchDishes, addToCart, removeFromCart, submitOrder in `client/src/stores/dining.js`
- [ ] T011 [US2] Implement full ordering page: add reservation context display, shopping cart bar, submit order via reservation_id in `client/src/pages/dining/order/index.vue`
- [ ] T012 [US2] Update DiningController `createOrder` endpoint signature and `getReservations` to support status filter in `server/src/main/java/com/agritainment/controller/DiningController.java`

**Checkpoint**: 预约→点餐完整流程畅通

---

## Phase 5: User Story 3 - 页面结构调整 (P2)

**Goal**: 将餐饮模块拆分为独立预约页和点餐页，底部导航 + 路由守卫控制流程

**Independent Test**: 进入餐饮模块 → 预约页默认显示 → 底部两个 tab → 完成预约自动跳转点餐页 → 无预约点击点餐 tab 被拦截

### Implementation

- [ ] T013 [US3] Restructure dining/index.vue as standalone reservation page: remove dish list section, add reservation success callback in `client/src/pages/dining/index.vue`
- [ ] T014 [US3] Implement navigation guard in order page: check for valid reservation on entry, show toast "请先完成预约" and redirect if none in `client/src/pages/dining/order/index.vue`
- [ ] T015 [US3] Implement auto-redirect from reservation → order after successful booking, pass reservation context via Pinia store in `client/src/pages/dining/index.vue`
- [ ] T016 [US3] Add bottom navigation bar component with "预约"/"点餐" tabs in dining pages in `client/src/pages/dining/index.vue` and `client/src/pages/dining/order/index.vue`

**Checkpoint**: 预约页→点餐页导航流畅，流程控制正确

---

## Phase 6: Polish & Edge Cases

> 边界情况处理和最终打磨

- [ ] T017 Handle edge case: cancelled reservation → order page should reject entry in `client/src/pages/dining/order/index.vue`
- [ ] T018 Handle edge case: expired reservation (date passed) → filter in getReservations in `server/src/main/java/com/agritainment/service/DiningService.java`
- [ ] T019 Handle edge case: duplicate submit prevention for reservation in `client/src/pages/dining/index.vue`
- [ ] T020 Handle edge case: network error during order submit preserves cart data in `client/src/pages/dining/order/index.vue`
- [ ] T021 Handle edge case: sold-out dishes disable "+" button in `client/src/pages/dining/order/index.vue`
- [ ] T022 Handle edge case: empty reservation state shows guidance on order page in `client/src/pages/dining/order/index.vue`

---

## Dependencies

```
Phase 1 (Setup)
  └── Phase 2 (Foundational)
        ├── Phase 3 (US1: 预约修复) ── 独立完成
        ├── Phase 4 (US2: 点餐功能) ── 依赖 US1 修复 + Phase 2 DTO
        └── Phase 5 (US3: 页面拆分) ── 依赖 US2 store + US1 流程
              └── Phase 6 (Polish)
```

- **US1 可独立执行**: 不需要等 Phase 2 完成（仅修改 service 层，实体不变）
- **US2 依赖 US1 T006**: createOrder 修改涉及与 createReservation 共享的逻辑
- **US3 依赖 US2 T010**: Pinia store 是页面间通信的基础
- **Phase 4 内部**: T010 (store) 先于 T011 (order page)，T009 (backend) 可与 T010 并行

## Parallel Execution Opportunities

| 并行组 | 任务 | 条件 |
|--------|------|------|
| Phase 2 | T003 ‖ T004 | 修改不同文件，无依赖 |
| Phase 3 | T006 ‖ T007 ‖ T008 | 同一文件但不同方法，需注意 merge |
| Phase 4 后端 | T009 ‖ T010 | T009 (Java) 和 T010 (JS) 不同语言 |
| Phase 6 | T017 ‖ T018 ‖ T019 ‖ T020 ‖ T021 ‖ T022 | 全部 [P] 标记，不同文件/方法 |

---

## Task Summary

| Phase | Task Count | Story |
|-------|-----------|-------|
| Phase 1: Setup | 2 | - |
| Phase 2: Foundational | 3 | - |
| Phase 3: US1 预约修复 | 3 | P1 |
| Phase 4: US2 点餐功能 | 4 | P2 |
| Phase 5: US3 页面拆分 | 4 | P2 |
| Phase 6: Polish | 6 | - |
| **Total** | **22** | |

---

## Suggested MVP Scope

Phase 1-3 (T001-T008) = 日期独立预约修复，可独立交付验证。