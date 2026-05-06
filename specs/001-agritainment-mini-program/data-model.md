# 数据模型设计

**项目**: 农家乐小程序
**日期**: 2026-05-05
**最后更新**: 2026-05-06

> **实现说明**: 主键使用 BIGINT（非 INT），所有价格字段使用 DECIMAL(10,2)（Java 端 BigDecimal），乐观锁通过 `version` 字段实现。

---

## 1. 用户系统

### users (用户表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| phone | VARCHAR(11) | UNIQUE, NOT NULL | 手机号 |
| password | VARCHAR(255) | | 管理员密码(BCrypt) |
| identity_code | VARCHAR(12) | UNIQUE, NOT NULL | 身份码(12位数字) |
| nickname | VARCHAR(50) | | 昵称 |
| avatar_url | VARCHAR(255) | | 头像URL |
| role | ENUM('customer','staff','admin') | NOT NULL, DEFAULT 'customer' | 角色 |
| is_member | BOOLEAN | DEFAULT FALSE | 是否会员 |
| member_expire_at | DATE | | 会员过期时间 |
| no_show_count | INT | DEFAULT 0 | 爽约次数 |
| is_blacklisted | BOOLEAN | DEFAULT FALSE | 是否黑名单 |
| openid | VARCHAR(64) | UNIQUE | 微信openid(用于登录和订阅消息) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

---

## 2. 餐饮系统

### tables (桌位表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| table_number | VARCHAR(20) | UNIQUE, NOT NULL | 桌位编号 |
| qr_code | VARCHAR(255) | | 二维码标识 |
| capacity | INT | DEFAULT 4 | 容纳人数 |
| status | ENUM('idle','reserved','dining') | DEFAULT 'idle' | 状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### dishes (菜品表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 菜品名称 |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| image_url | VARCHAR(255) | | 菜品图片 |
| description | TEXT | | 菜品简介 |
| remaining_stock | INT | DEFAULT -1 | 当日剩余(-1=无限) |
| is_available | BOOLEAN | DEFAULT TRUE | 是否上架 |
| version | INT | DEFAULT 0 | 乐观锁版本号 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### table_reservations (桌位预约表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK -> users.id, NOT NULL | 预约用户 |
| table_id | BIGINT | FK -> tables.id, NOT NULL | 预约桌位 |
| reservation_date | DATE | NOT NULL | 预约日期 |
| time_slot | ENUM('lunch','dinner') | NOT NULL | 时段 |
| status | ENUM('pending','checked_in','cancelled','no_show') | DEFAULT 'pending' | 状态 |
| cancelled_by | ENUM('customer','staff') | | 取消方 |
| is_late_cancel | BOOLEAN | DEFAULT FALSE | 是否超时取消 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### orders (订单表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| table_id | BIGINT | FK -> tables.id, NOT NULL | 桌位 |
| user_id | BIGINT | FK -> users.id, NOT NULL | 下单用户 |
| total_amount | DECIMAL(10,2) | DEFAULT 0 | 总金额 |
| status | ENUM('active','settled') | DEFAULT 'active' | 状态 |
| settled_at | DATETIME | | 结账时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### order_items (订单明细表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| order_id | BIGINT | FK -> orders.id, NOT NULL | 订单 |
| dish_id | BIGINT | FK -> dishes.id, NOT NULL | 菜品 |
| quantity | INT | NOT NULL, DEFAULT 1 | 数量 |
| price | DECIMAL(10,2) | NOT NULL | 单价(快照) |
| status | ENUM('pending','served','refunded') | DEFAULT 'pending' | 状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

---

## 3. 产品售卖系统

### products (产品表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 产品名称 |
| type | ENUM('physical','service') | NOT NULL, DEFAULT 'physical' | 类型(实体/服务) |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| member_price | DECIMAL(10,2) | | 会员价 |
| image_url | VARCHAR(255) | | 产品图片 |
| description | TEXT | | 产品简介 |
| daily_quota | INT | DEFAULT -1 | 每日兑换限额(-1=无限) |
| remaining_quota | INT | DEFAULT -1 | 当日剩余(-1=无限) |
| is_available | BOOLEAN | DEFAULT TRUE | 是否上架 |
| version | INT | DEFAULT 0 | 乐观锁版本号 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### coupons (卡券表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| code | VARCHAR(12) | UNIQUE, NOT NULL | 12位唯一标识码 |
| product_id | BIGINT | FK -> products.id | 关联产品 |
| user_id | BIGINT | FK -> users.id, NOT NULL | 持有用户 |
| original_user_id | BIGINT | | 原始用户(转赠时) |
| source | ENUM('purchase','gift','membership') | NOT NULL, DEFAULT 'purchase' | 来源 |
| status | ENUM('available','locked','used','expired') | DEFAULT 'available' | 状态 |
| qr_code_data | VARCHAR(255) | | 二维码数据 |
| used_at | DATETIME | | 使用时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### service_reservations (服务预约表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK -> users.id, NOT NULL | 预约用户 |
| coupon_id | BIGINT | FK -> coupons.id | 关联卡券 |
| product_id | BIGINT | FK -> products.id | 关联产品 |
| reservation_date | DATE | NOT NULL | 预约日期 |
| status | ENUM('pending','completed','cancelled','no_show') | DEFAULT 'pending' | 状态 |
| is_late_cancel | BOOLEAN | DEFAULT FALSE | 是否超时取消 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### membership_configs (会员配置表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| annual_price | DECIMAL(10,2) | NOT NULL | 年费价格 |
| discount_rate | DECIMAL(3,2) | DEFAULT 0.90 | 折扣率 |
| gift_product_ids | JSON | | 赠送产品ID列表 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

---

## 4. 种植体验系统

### plots (地块表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| plot_number | VARCHAR(20) | UNIQUE, NOT NULL | 地块编号 |
| name | VARCHAR(100) | | 地块名称 |
| area | DECIMAL(10,2) | | 面积(平米) |
| description | TEXT | | 详细信息 |
| renter_id | BIGINT | FK -> users.id | 租用客户 |
| status | ENUM('available','rented','maintenance') | DEFAULT 'available' | 状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### cameras (摄像头表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| identifier | VARCHAR(50) | UNIQUE, NOT NULL | 摄像头标识 |
| name | VARCHAR(100) | | 摄像头名称 |
| ip_address | VARCHAR(50) | | IP地址 |
| status | ENUM('online','offline') | DEFAULT 'offline' | 状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### camera_plot_bindings (摄像头-地块绑定表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| camera_id | BIGINT | FK -> cameras.id, NOT NULL | 摄像头 |
| plot_id | BIGINT | FK -> plots.id, NOT NULL | 地块 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### camera_queue (摄像头等待队列)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| camera_id | BIGINT | FK -> cameras.id, NOT NULL | 摄像头 |
| user_id | BIGINT | FK -> users.id, NOT NULL | 用户 |
| queue_position | INT | NOT NULL | 队列位置 |
| status | ENUM('active','waiting','expired') | DEFAULT 'waiting' | 状态 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### garden_services (种植管理服务表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 服务名称(浇水/施肥等) |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| description | TEXT | | 服务说明 |
| is_available | BOOLEAN | DEFAULT TRUE | 是否可用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

### garden_service_orders (种植服务订单表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK -> users.id, NOT NULL | 下单客户 |
| plot_id | BIGINT | FK -> plots.id, NOT NULL | 地块 |
| service_id | BIGINT | FK -> garden_services.id, NOT NULL | 服务 |
| coupon_id | BIGINT | FK -> coupons.id | 关联卡券 |
| status | ENUM('pending','in_progress','completed') | DEFAULT 'pending' | 状态 |
| assigned_staff_id | BIGINT | FK -> users.id | 分配的园丁 |
| completed_at | DATETIME | | 完成时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

---

## 5. 社区系统

### journals (日志表)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK -> users.id, NOT NULL | 作者 |
| title | VARCHAR(100) | NOT NULL | 标题 |
| content | TEXT | | 内容 |
| images | JSON | | 图片URL列表 |
| is_shared | BOOLEAN | DEFAULT FALSE | 是否分享到首页 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | |

---

## 6. 索引

> 索引通过 IndexInitializer.java 在应用启动时自动创建（如不存在）。

| 索引名 | 表 | 列 |
|--------|-----|-----|
| idx_users_phone | users | (phone) |
| idx_users_role | users | (role) |
| idx_reservations_user_date | table_reservations | (user_id, reservation_date) |
| idx_reservations_table_date | table_reservations | (table_id, reservation_date, time_slot) |
| idx_orders_table_status | orders | (table_id, status) |
| idx_coupons_user_status | coupons | (user_id, status) |
| idx_coupons_code | coupons | (code) |
| idx_plots_status | plots | (status) |
| idx_journals_user | journals | (user_id) |

---

## 7. 与原设计的差异

| 项目 | 原设计 | 实际实现 | 原因 |
|------|--------|----------|------|
| 主键类型 | INT | BIGINT | MyBatis-Plus 默认雪花算法兼容 |
| dishes.daily_stock | 存在 | 保留 | 每日库存重置需要 daily_stock 作为基准值，ScheduledTaskService 依赖 |
| dishes.name | NOT NULL | UNIQUE, NOT NULL | 防止种子数据重复 |
| dishes.version | 无 | DEFAULT 0 | 乐观锁 |
| products.name | NOT NULL | UNIQUE, NOT NULL | 防止种子数据重复 |
| products.version | 无 | DEFAULT 0 | 乐观锁 |
| garden_services.name | NOT NULL | UNIQUE, NOT NULL | 防止种子数据重复 |
| cameras.camera_id | VARCHAR(50) | identifier VARCHAR(50) | 字段命名统一 |
| camera_queue.user_role | 存在 | 移除 | 简化，角色从 users 表获取 |
| camera_queue.max_usage_minutes | cameras 表 | 移除 | 暂不实现时长限制 |
| admin_config 表 | 存在 | 未创建 | 管理员通过 role 字段区分，无需额外表 |
| users.password | 无 | VARCHAR(255) | 管理员密码登录需要 |
| membership_config | 表名 | membership_configs | Spring Boot 约定复数 |

---

## 8. 状态流转图

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
