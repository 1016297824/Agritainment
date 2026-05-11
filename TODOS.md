# TODOS

## 餐饮模块后端 (004-dining-module-enhance)

**Priority:** P1
**T001** DB 迁移：`ALTER TABLE orders ADD COLUMN reservation_id BIGINT DEFAULT NULL; ALTER TABLE orders ADD INDEX idx_reservation_id (reservation_id);`
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T002** 清理 SQL：`UPDATE tables SET status = 'idle' WHERE status = 'reserved';`
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T003** Order 实体添加 `reservationId` 字段 — `server/src/main/java/com/agritainment/entity/Order.java`
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T004** CreateOrderRequest DTO 添加 `reservation_id` 字段
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T006** DiningService.createReservation() 中移除 `tableMapper.update(...status="reserved")` — server/src/main/java/com/agritainment/service/DiningService.java (line 90)
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T007** DiningService.cancelReservation() 中移除 `tableMapper.update(...status="idle")` — server/src/main/java/com/agritainment/service/DiningService.java (line 114)
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T008** 验证 getTables() 使用 reservationDate + timeSlot 进行过滤 — server/src/main/java/com/agritainment/service/DiningService.java (lines 38-60)
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T009** createOrder() 支持 reservation_id 参数，从预约中解析桌位 — server/src/main/java/com/agritainment/service/DiningService.java (lines 138-178)
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T012** 更新 DiningController createOrder 端点签名，getReservations 支持 status 过滤
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

**Priority:** P1
**T018** getReservations 中过滤已过期的预约 — server/src/main/java/com/agritainment/service/DiningService.java
**Deferred from plan:** specs/004-dining-module-enhance/tasks.md (v0.1.0.0)

## Completed