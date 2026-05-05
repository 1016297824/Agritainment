# 数据模型设计

**项目**: 农家乐小程序
**日期**: 2026-05-05

---

## 1. 用户系统

### users (用户表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 用户ID |
| phone | VARCHAR(11) | UNIQUE, NOT NULL | 手机号 |
| identity_code | VARCHAR(12) | UNIQUE, NOT NULL | 身份码(12位数字) |
| nickname | VARCHAR(50) | | 昵称 |
| avatar_url | VARCHAR(255) | | 头像URL |
| role | ENUM('customer','staff','admin') | NOT NULL, DEFAULT 'customer' | 角色 |
| is_member | BOOLEAN | DEFAULT FALSE | 是否会员 |
| member_expire_at | DATETIME | | 会员过期时间 |
| no_show_count | INT | DEFAULT 0 | 爽约次数 |
| is_blacklisted | BOOLEAN | DEFAULT FALSE | 是否黑名单 |
| created_at | DATETIME | DEFAULT NOW() | 创建时间 |
| updated_at | DATETIME | ON UPDATE NOW() | 更新时间 |

### admin_config (管理员配置表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK | |
| user_id | INT | FK -> users.id | 关联用户 |
| admin_level | ENUM('super','deputy') | NOT NULL | 管理级别 |
| created_at | DATETIME | DEFAULT NOW() | |

---

## 2. 餐饮系统

### tables (桌位表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| table_number | VARCHAR(20) | UNIQUE, NOT NULL | 桌位编号 |
| qr_code | VARCHAR(255) | | 二维码标识 |
| capacity | INT | DEFAULT 4 | 容纳人数 |
| status | ENUM('idle','reserved','dining') | DEFAULT 'idle' | 状态 |
| created_at | DATETIME | DEFAULT NOW() | |

### dishes (菜品表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | 菜品名称 |
| image_url | VARCHAR(255) | | 菜品图片 |
| description | TEXT | | 菜品简介 |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| daily_stock | INT | DEFAULT -1 | 每日库存(-1=无限) |
| remaining_stock | INT | DEFAULT -1 | 当日剩余 |
| is_available | BOOLEAN | DEFAULT TRUE | 是否上架 |
| created_at | DATETIME | DEFAULT NOW() | |

### table_reservations (桌位预约表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK -> users.id | 预约用户 |
| table_id | INT | FK -> tables.id | 预约桌位 |
| reservation_date | DATE | NOT NULL | 预约日期 |
| time_slot | ENUM('lunch','dinner') | NOT NULL | 时段 |
| status | ENUM('pending','checked_in','cancelled','no_show') | DEFAULT 'pending' | 状态 |
| cancelled_by | ENUM('customer','staff') | | 取消方 |
| is_late_cancel | BOOLEAN | DEFAULT FALSE | 是否超时取消 |
| created_at | DATETIME | DEFAULT NOW() | |

### orders (订单表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| table_id | INT | FK -> tables.id | 桌位 |
| user_id | INT | FK -> users.id | 下单用户 |
| status | ENUM('active','settled') | DEFAULT 'active' | 状态 |
| total_amount | DECIMAL(10,2) | DEFAULT 0 | 总金额 |
| settled_at | DATETIME | | 结账时间 |
| created_at | DATETIME | DEFAULT NOW() | |

### order_items (订单明细表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| order_id | INT | FK -> orders.id | 订单 |
| dish_id | INT | FK -> dishes.id | 菜品 |
| quantity | INT | NOT NULL, DEFAULT 1 | 数量 |
| price | DECIMAL(10,2) | NOT NULL | 单价(快照) |
| status | ENUM('pending','served','refunded') | DEFAULT 'pending' | 状态 |
| created_at | DATETIME | DEFAULT NOW() | |

---

## 3. 产品售卖系统

### products (产品表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | 产品名称 |
| type | ENUM('physical','service') | NOT NULL | 类型(实体/服务) |
| image_url | VARCHAR(255) | | 产品图片 |
| description | TEXT | | 产品简介 |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| member_price | DECIMAL(10,2) | | 会员价 |
| daily_quota | INT | DEFAULT -1 | 每日兑换限额(-1=无限) |
| remaining_quota | INT | DEFAULT -1 | 当日剩余 |
| is_available | BOOLEAN | DEFAULT TRUE | 是否上架 |
| created_at | DATETIME | DEFAULT NOW() | |

### coupons (卡券表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| code | VARCHAR(12) | UNIQUE, NOT NULL | 12位唯一标识码 |
| product_id | INT | FK -> products.id | 关联产品 |
| user_id | INT | FK -> users.id | 持有用户 |
| source | ENUM('purchase','gift','membership') | NOT NULL | 来源 |
| status | ENUM('available','locked','used','expired') | DEFAULT 'available' | 状态 |
| original_user_id | INT | | 原始用户(转赠时) |
| qr_code_data | VARCHAR(255) | | 二维码数据 |
| created_at | DATETIME | DEFAULT NOW() | |
| used_at | DATETIME | | 使用时间 |

### service_reservations (服务预约表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK -> users.id | 预约用户 |
| coupon_id | INT | FK -> coupons.id | 关联卡券 |
| product_id | INT | FK -> products.id | 关联产品 |
| reservation_date | DATE | NOT NULL | 预约日期 |
| status | ENUM('pending','completed','cancelled','no_show') | DEFAULT 'pending' | 状态 |
| is_late_cancel | BOOLEAN | DEFAULT FALSE | 是否超时取消 |
| created_at | DATETIME | DEFAULT NOW() | |

### membership_config (会员配置表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK | |
| annual_price | DECIMAL(10,2) | NOT NULL | 年费价格 |
| discount_rate | DECIMAL(3,2) | DEFAULT 0.90 | 折扣率 |
| gift_product_ids | JSON | | 赠送产品ID列表 |
| updated_at | DATETIME | ON UPDATE NOW() | |

---

## 4. 种植体验系统

### plots (地块表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| plot_number | VARCHAR(20) | UNIQUE, NOT NULL | 地块编号 |
| name | VARCHAR(100) | | 地块名称 |
| description | TEXT | | 详细信息 |
| area | DECIMAL(10,2) | | 面积(平米) |
| status | ENUM('available','rented','maintenance') | DEFAULT 'available' | 状态 |
| renter_id | INT | FK -> users.id | 租用客户 |
| created_at | DATETIME | DEFAULT NOW() | |

### cameras (摄像头表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| camera_id | VARCHAR(50) | UNIQUE, NOT NULL | 摄像头标识 |
| name | VARCHAR(100) | | 摄像头名称 |
| ip_address | VARCHAR(50) | | IP地址 |
| status | ENUM('online','offline') | DEFAULT 'offline' | 状态 |
| max_usage_minutes | INT | DEFAULT 30 | 最长使用时间(分钟) |
| created_at | DATETIME | DEFAULT NOW() | |

### camera_plot_bindings (摄像头-地块绑定表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| camera_id | INT | FK -> cameras.id | 摄像头 |
| plot_id | INT | FK -> plots.id | 地块 |
| created_at | DATETIME | DEFAULT NOW() | |

### camera_queue (摄像头等待队列)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| camera_id | INT | FK -> cameras.id | 摄像头 |
| user_id | INT | FK -> users.id | 用户 |
| user_role | ENUM('customer','staff') | NOT NULL | 角色 |
| queue_position | INT | NOT NULL | 队列位置 |
| status | ENUM('active','waiting','expired') | DEFAULT 'waiting' | 状态 |
| started_at | DATETIME | | 开始使用时间 |
| expires_at | DATETIME | | 过期时间 |
| created_at | DATETIME | DEFAULT NOW() | |

### garden_services (种植管理服务表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | 服务名称(浇水/施肥等) |
| description | TEXT | | 服务说明 |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| is_available | BOOLEAN | DEFAULT TRUE | 是否可用 |

### garden_service_orders (种植服务订单表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK -> users.id | 下单客户 |
| plot_id | INT | FK -> plots.id | 地块 |
| service_id | INT | FK -> garden_services.id | 服务 |
| coupon_id | INT | FK -> coupons.id | 关联卡券 |
| status | ENUM('pending','in_progress','completed') | DEFAULT 'pending' | 状态 |
| assigned_staff_id | INT | FK -> users.id | 分配的园丁 |
| completed_at | DATETIME | | 完成时间 |
| created_at | DATETIME | DEFAULT NOW() | |

---

## 5. 社区系统

### journals (日志表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK -> users.id | 作者 |
| title | VARCHAR(100) | NOT NULL | 标题 |
| content | TEXT | | 内容 |
| images | JSON | | 图片URL列表 |
| is_shared | BOOLEAN | DEFAULT FALSE | 是否分享到首页 |
| created_at | DATETIME | DEFAULT NOW() | |
| updated_at | DATETIME | ON UPDATE NOW() | |

---

## 6. 状态流转图

### 桌位状态

```
idle ──预约──> reserved ──到店──> dining ──结账──> idle
  ^               │
  └──取消预约──────┘
```

### 卡券状态

```
available ──预约──> locked ──取消──> available
    │                  │
    │                  └──核销──> used
    │
    └──过期──> expired
```

### 种植服务订单状态

```
pending ──分配园丁──> in_progress ──园丁完成──> completed
```
