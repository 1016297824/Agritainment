# API Contracts: 餐饮模块功能增强

**Date**: 2026-05-11
**Base Path**: `/api/v1/dining`

## Modified Endpoints

### 1. GET /tables

**Change**: 无接口签名变更。内部逻辑保持按日期+时段过滤预约状态。

**Request**:
```
GET /api/v1/dining/tables?date=2026-05-11&time_slot=lunch
```

**Response** (200):
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "tableNumber": "A01",
      "capacity": 4,
      "status": "idle",
      "qrCode": "QR_A01",
      "reserved": false
    }
  ]
}
```

**关键变更**: `reserved` 字段由 `createReservation` 按日期+时段查询 `TableReservation` 决定，不再受 `DiningTable.status` 影响。

---

### 2. POST /reservations

**Change**: 内部逻辑变更——去除 `DiningTable.status` 的直接更新。接口签名不变。

**Request**:
```json
{
  "table_id": 1,
  "date": "2026-05-11",
  "time_slot": "lunch"
}
```

**Response** (200):
```json
{
  "code": 200,
  "data": {
    "id": 100,
    "userId": 42,
    "tableId": 1,
    "reservationDate": "2026-05-11",
    "timeSlot": "lunch",
    "status": "pending"
  }
}
```

**Error Responses**:
- `40002`: "该时段已有预约" — 用户在相同时段已有一个 pending 预约
- `40003`: "该桌位已被预约" — 目标桌位在该日期时段已被其他人预约

---

### 3. POST /orders

**Change**: 增加 `reservation_id` 可选参数。保留 `table_qr` 可选参数（向后兼容）。

**Request** (预约点餐场景):
```json
{
  "reservation_id": 100,
  "items": [
    { "dish_id": 1, "quantity": 2 },
    { "dish_id": 3, "quantity": 1 }
  ]
}
```

**Request** (扫码点餐场景 - 保留):
```json
{
  "table_qr": "QR_A01",
  "items": [
    { "dish_id": 1, "quantity": 2 }
  ]
}
```

**Response** (200):
```json
{
  "code": 200,
  "data": {
    "id": 200,
    "tableId": 1,
    "userId": 42,
    "reservationId": 100,
    "totalAmount": "128.00",
    "status": "active"
  }
}
```

**Error Responses**:
- `40006`: "桌位不存在" — table_qr 或 reservation_id 对应的桌位无效
- `40010`: "菜品库存不足" — 某菜品库存不足以满足数量
- `40011`: "预约不存在或已失效" — reservation_id 对应的预约不存在或已取消
- `40012`: "table_qr 和 reservation_id 至少提供一个"

---

### 4. GET /reservations

**Change**: 新增查询参数支持，可查询当前有效预约。

**Request**:
```
GET /api/v1/dining/reservations
GET /api/v1/dining/reservations?status=pending
```

**Response** (200):
```json
{
  "code": 200,
  "data": [
    {
      "id": 100,
      "userId": 42,
      "tableId": 1,
      "reservationDate": "2026-05-11",
      "timeSlot": "lunch",
      "status": "pending"
    }
  ]
}
```

## Unchanged Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/dishes` | GET | 获取菜品列表 |
| `/reservations/{id}` | DELETE | 取消预约 |
| `/orders/active` | GET | 获取活跃订单 |
| `/orders/{id}/settle` | POST | 结算订单 |
| `/staff/reservations/{id}/checkin` | POST | 员工签到 |
| `/staff/reservations/{id}/cancel` | POST | 员工取消预约 |
| `/orders/{id}/change-table` | POST | 换桌 |
| `/order-items/{id}/refund` | POST | 退菜退款 |
| `/all-tables` | GET | 管理员查看所有桌位 |
| `/tables` | POST | 管理员创建桌位 |
| `/tables/{id}` | DELETE | 管理员删除桌位 |