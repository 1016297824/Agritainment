# Data Model: 餐饮模块功能增强

**Date**: 2026-05-11
**Feature**: 004-dining-module-enhance

## Entity Changes Summary

| Entity | Table | Change Type | Description |
|--------|-------|-------------|-------------|
| DiningTable | `tables` | 语义变更 | `status` 字段不再表示预约状态，仅表示 `idle`/`dining` |
| TableReservation | `table_reservations` | 无结构变更 | 预约状态管理的唯一数据源 |
| Order | `orders` | 新增字段 | 增加 `reservation_id` 关联预约 |
| OrderItem | `order_items` | 无变更 | - |
| Dish | `dishes` | 无变更 | - |

## Entity Details

### DiningTable (`tables`)

| Field | Type | Description | Change |
|-------|------|-------------|--------|
| id | BIGINT PK | 桌位ID | - |
| table_number | VARCHAR | 桌号 | - |
| capacity | INT | 容量（人数） | - |
| qr_code | VARCHAR | 二维码标识 | - |
| status | VARCHAR | `idle`=空闲, `dining`=用餐中 | **语义变更**: 不再有 `reserved` 状态 |

**状态机变更**:
- 旧: `idle` → `reserved` (预约时) → `dining` (签到时) → `idle` (结算时)
- 新: `idle` → `dining` (签到时) → `idle` (结算时)
- 预约状态 (`reserved`) 由 TableReservation 管理，不体现在 DiningTable

### TableReservation (`table_reservations`)

| Field | Type | Description | Change |
|-------|------|-------------|--------|
| id | BIGINT PK | 预约ID | - |
| user_id | BIGINT FK | 用户ID | - |
| table_id | BIGINT FK | 桌位ID | - |
| reservation_date | DATE | 预约日期 | - |
| time_slot | VARCHAR | `lunch`/`dinner` | - |
| status | VARCHAR | `pending`/`cancelled`/`checked_in` | - |
| cancelled_by | VARCHAR | 取消操作者角色 | - |
| is_late_cancel | BOOLEAN | 是否迟到取消 | - |
| created_at | DATETIME | 创建时间 | - |

**当前有效预约** 定义: `status = 'pending'` 且 `reservation_date >= CURRENT_DATE`

### Order (`orders`)

| Field | Type | Description | Change |
|-------|------|-------------|--------|
| id | BIGINT PK | 订单ID | - |
| table_id | BIGINT FK | 桌位ID | - |
| user_id | BIGINT FK | 用户ID | - |
| **reservation_id** | **BIGINT FK** | **关联的预约ID** | **NEW** |
| total_amount | DECIMAL | 订单总额 | - |
| status | VARCHAR | `active`/`settled` | - |
| settled_at | DATETIME | 结算时间 | - |
| created_at | DATETIME | 创建时间 | - |

**新增字段 `reservation_id`**: 可空，用于关联预约记录。预约流程创建订单时必填；扫码点餐场景下可为空。

### DTO Changes

#### CreateOrderRequest

```java
@Data
public class CreateOrderRequest {
    // 保留原字段（可选，扫码点餐场景）
    private String table_qr;
    // 新增字段（可选，预约点餐场景）
    private Long reservation_id;
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemInput> items;
    
    // 校验：table_qr 和 reservation_id 至少提供一个
    @Data
    public static class OrderItemInput {
        private Long dish_id;
        private Integer quantity = 1;
    }
}
```

## Database Migration

```sql
-- 为 orders 表增加 reservation_id 字段
ALTER TABLE orders ADD COLUMN reservation_id BIGINT DEFAULT NULL;
ALTER TABLE orders ADD INDEX idx_reservation_id (reservation_id);
```

无需其他 DDL 变更。`DiningTable.status` 现有数据中如有 `reserved` 值，可通过以下脚本清理：

```sql
UPDATE tables SET status = 'idle' WHERE status = 'reserved';
```