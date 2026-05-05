CREATE DATABASE IF NOT EXISTS agritainment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agritainment;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(11) UNIQUE NOT NULL,
    password VARCHAR(255),
    identity_code VARCHAR(12) UNIQUE NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    role ENUM('customer','staff','admin') NOT NULL DEFAULT 'customer',
    is_member BOOLEAN DEFAULT FALSE,
    member_expire_at DATE,
    no_show_count INT DEFAULT 0,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 桌位表
CREATE TABLE IF NOT EXISTS `tables` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(20) UNIQUE NOT NULL,
    qr_code VARCHAR(255),
    capacity INT DEFAULT 4,
    status ENUM('idle','reserved','dining') DEFAULT 'idle',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 菜品表
CREATE TABLE IF NOT EXISTS dishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    remaining_stock INT DEFAULT -1,
    is_available BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 桌位预约表
CREATE TABLE IF NOT EXISTS table_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    time_slot ENUM('lunch','dinner') NOT NULL,
    status ENUM('pending','checked_in','cancelled','no_show') DEFAULT 'pending',
    cancelled_by ENUM('customer','staff'),
    is_late_cancel BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (table_id) REFERENCES `tables`(id)
);

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status ENUM('active','settled') DEFAULT 'active',
    settled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES `tables`(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('pending','served','refunded') DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `orders`(id),
    FOREIGN KEY (dish_id) REFERENCES dishes(id)
);

-- 产品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    `type` ENUM('physical','service') NOT NULL DEFAULT 'physical',
    price DECIMAL(10,2) NOT NULL,
    member_price DECIMAL(10,2),
    image_url VARCHAR(255),
    description TEXT,
    daily_quota INT DEFAULT -1,
    remaining_quota INT DEFAULT -1,
    is_available BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 卡券表
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(12) UNIQUE NOT NULL,
    product_id BIGINT,
    user_id BIGINT NOT NULL,
    original_user_id BIGINT,
    source ENUM('purchase','gift','membership') NOT NULL DEFAULT 'purchase',
    status ENUM('available','locked','used','expired') DEFAULT 'available',
    qr_code_data VARCHAR(255),
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 服务预约表
CREATE TABLE IF NOT EXISTS service_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT,
    product_id BIGINT,
    reservation_date DATE NOT NULL,
    status ENUM('pending','completed','cancelled','no_show') DEFAULT 'pending',
    is_late_cancel BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 会员配置表
CREATE TABLE IF NOT EXISTS membership_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    annual_price DECIMAL(10,2) NOT NULL,
    discount_rate DECIMAL(3,2) DEFAULT 0.90,
    gift_product_ids JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 地块表
CREATE TABLE IF NOT EXISTS plots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100),
    area DECIMAL(10,2),
    description TEXT,
    renter_id BIGINT,
    status ENUM('available','rented','maintenance') DEFAULT 'available',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (renter_id) REFERENCES users(id)
);

-- 摄像头表
CREATE TABLE IF NOT EXISTS cameras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100),
    ip_address VARCHAR(50),
    status ENUM('online','offline') DEFAULT 'offline',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 摄像头-地块绑定表
CREATE TABLE IF NOT EXISTS camera_plot_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_id BIGINT NOT NULL,
    plot_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (camera_id) REFERENCES cameras(id),
    FOREIGN KEY (plot_id) REFERENCES plots(id)
);

-- 摄像头等待队列
CREATE TABLE IF NOT EXISTS camera_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    queue_position INT NOT NULL,
    status ENUM('active','waiting','expired') DEFAULT 'waiting',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (camera_id) REFERENCES cameras(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 种植管理服务表
CREATE TABLE IF NOT EXISTS garden_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 种植服务订单表
CREATE TABLE IF NOT EXISTS garden_service_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plot_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    coupon_id BIGINT,
    status ENUM('pending','in_progress','completed') DEFAULT 'pending',
    assigned_staff_id BIGINT,
    completed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plot_id) REFERENCES plots(id),
    FOREIGN KEY (service_id) REFERENCES garden_services(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);

-- 种植日记表
CREATE TABLE IF NOT EXISTS journals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    images JSON,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 索引
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_reservations_user_date ON table_reservations(user_id, reservation_date);
CREATE INDEX idx_reservations_table_date ON table_reservations(table_id, reservation_date, time_slot);
CREATE INDEX idx_orders_table_status ON `orders`(table_id, status);
CREATE INDEX idx_coupons_user_status ON coupons(user_id, status);
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_plots_status ON plots(status);
CREATE INDEX idx_journals_user ON journals(user_id);
