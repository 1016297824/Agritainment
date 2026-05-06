# API 接口契约

**项目**: 农家乐小程序
**基础路径**: `/api/v1`
**认证方式**: Bearer Token (JWT)

---

## 1. 认证模块

### POST /auth/register
客户手机号注册

**Request:**
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "phone": "138****8000",
    "identity_code": "123456789012",
    "role": "customer",
    "is_member": false
  }
}
```

### POST /auth/login
登录

### POST /auth/sms-code
发送短信验证码

### POST /auth/admin-login
管理员特殊登录

### POST /auth/wx-login
微信小程序登录（code换取openid，自动绑定或注册）

**Request:**
```json
{ "code": "wx_login_code" }
```

### GET /auth/me
获取当前登录用户信息

---

## 2. 餐饮模块

### GET /dining/tables
获取桌位列表（含预约状态）

**Query:** `?date=2026-05-06&time_slot=lunch`

**Response:**
```json
{
  "tables": [
    {
      "id": 1,
      "table_number": "A1",
      "capacity": 4,
      "status": "idle",
      "qr_code": "TABLE_A1"
    }
  ]
}
```

### POST /dining/reservations
创建桌位预约

**Request:**
```json
{
  "table_id": 1,
  "date": "2026-05-06",
  "time_slot": "lunch"
}
```

### DELETE /dining/reservations/:id
取消预约

### GET /dining/reservations
获取预约列表

### GET /dining/orders/active
获取当前用餐订单（扫码进入）

**Query:** `?table_qr=TABLE_A1`

### POST /dining/orders
创建订单（下单/加菜）

**Request:**
```json
{
  "table_qr": "TABLE_A1",
  "items": [
    { "dish_id": 1, "quantity": 2 }
  ]
}
```

### POST /dining/orders/:id/settle
结账

### POST /dining/orders/:id/change-table
换桌

### POST /dining/orders/:id/refund-item
退菜

### GET /dining/dishes
获取菜品列表

### CRUD /admin/dishes (管理员)
### CRUD /admin/tables (管理员)

---

## 3. 产品售卖模块

### GET /products
获取产品列表

**Response:**
```json
{
  "products": [
    {
      "id": 1,
      "name": "手工香囊体验",
      "type": "service",
      "price": 68.00,
      "member_price": 58.00,
      "image_url": "https://...",
      "remaining_quota": 20
    }
  ]
}
```

### POST /products/:id/purchase
购买产品（生成卡券）

### GET /coupons
获取我的卡券列表

**Response:**
```json
{
  "coupons": [
    {
      "id": 1,
      "code": "123456789012",
      "product_name": "手工香囊体验",
      "status": "available",
      "qr_code_data": "COUPON_123456789012"
    }
  ]
}
```

### POST /coupons/:id/transfer
转赠卡券

### POST /service-reservations
创建服务预约

### DELETE /service-reservations/:id
取消服务预约

### POST /coupons/verify
核销卡券（员工扫码/输入标识码）

**Request:**
```json
{
  "code": "123456789012"
}
```

**Response:**
```json
{
  "valid": true,
  "coupon": {
    "id": 1,
    "product_name": "手工香囊体验",
    "status": "used"
  }
}
```

### CRUD /admin/products (管理员)
### GET /admin/membership-config (管理员)
### PUT /admin/membership-config (管理员)

---

## 4. 会员模块

### POST /membership/purchase
购买会员

### GET /membership/status
获取会员状态

### POST /admin/membership/grant (管理员)
赠送会员

---

## 5. 种植体验模块

### GET /planting/plots
获取地块列表

### POST /planting/plots/:id/rent
租用地块（生成卡券）

### GET /planting/garden-services
获取种植管理服务列表

### POST /planting/service-orders
下单种植管理服务

**Request:**
```json
{
  "plot_id": 1,
  "service_id": 1,
  "coupon_id": 5
}
```

### POST /planting/service-orders/:id/complete
园丁完成服务

### POST /planting/plots/:id/bind
绑定地块（员工操作）

**Request:**
```json
{
  "identity_code": "123456789012"
}
```

### GET /planting/cameras/:id/status
获取摄像头状态（预留接口）

### POST /planting/cameras/:id/control
控制摄像头（预留接口）

**Request:**
```json
{
  "action": "pan_left",
  "speed": 5
}
```

### CRUD /admin/plots (管理员)
### CRUD /admin/cameras (管理员)
### POST /admin/camera-bindings (管理员)
绑定摄像头与地块

---

## 6. 社区模块

### GET /journals
获取日志列表（首页展示）

### POST /journals
创建日志

### PUT /journals/:id
编辑日志

### POST /journals/:id/share
分享日志到首页

### DELETE /journals/:id/share
取消分享

### GET /journals/shared
获取首页分享日志列表

---

## 7. 用户管理模块（管理员）

### GET /admin/users
获取用户列表

### PUT /admin/users/:id/status
修改用户状态（黑名单等）

### POST /admin/staff
添加员工

### DELETE /admin/staff/:id
删除员工授权

### GET /admin/dashboard
管理后台仪表盘数据

---

## 8. 通用响应格式

### 成功响应
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 40001,
  "message": "桌位已被预约",
  "data": null
}
```

### 错误码定义

| 范围 | 说明 |
|------|------|
| 40001-40099 | 餐饮模块错误 |
| 40101-40199 | 产品售卖错误 |
| 40201-40299 | 种植体验错误 |
| 40301-40399 | 会员模块错误 |
| 40401-40499 | 认证错误 |
| 50001-50099 | 服务器内部错误 |
