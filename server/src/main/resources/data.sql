INSERT IGNORE INTO users (phone, password, identity_code, nickname, role, is_member, member_expire_at, no_show_count, is_blacklisted) VALUES
('13800000001', '$2a$10$hEoP7V/rXVhYy4K9npBrreBFtEkQENVS5g0AHVVLUeDhva5TqHEaS', '100000000001', '超级管理员', 'admin', FALSE, NULL, 0, FALSE),
('13800000002', '$2a$10$hEoP7V/rXVhYy4K9npBrreBFtEkQENVS5g0AHVVLUeDhva5TqHEaS', '100000000002', '副管理员', 'admin', FALSE, NULL, 0, FALSE),
('13800000010', NULL, '100000000010', '张园丁', 'staff', FALSE, NULL, 0, FALSE),
('13800000011', NULL, '100000000011', '李服务员', 'staff', FALSE, NULL, 0, FALSE),
('13900000001', NULL, '100000000101', '王先生', 'customer', TRUE, '2027-05-06', 0, FALSE),
('13900000002', NULL, '100000000102', '赵女士', 'customer', FALSE, NULL, 0, FALSE),
('13900000003', NULL, '100000000103', '陈同学', 'customer', FALSE, NULL, 1, FALSE);

INSERT IGNORE INTO `tables` (table_number, qr_code, capacity, status) VALUES
('A1', 'QR_A1', 4, 'idle'),
('A2', 'QR_A2', 4, 'idle'),
('A3', 'QR_A3', 6, 'idle'),
('B1', 'QR_B1', 8, 'idle'),
('B2', 'QR_B2', 8, 'idle'),
('C1', 'QR_C1', 10, 'idle'),
('C2', 'QR_C2', 12, 'idle'),
('D1', 'QR_D1', 6, 'idle'),
('D2', 'QR_D2', 4, 'idle'),
('VIP1', 'QR_VIP1', 10, 'idle');

INSERT IGNORE INTO dishes (name, price, image_url, description, remaining_stock, is_available) VALUES
('农家土鸡', 68.00, '/images/dishes/chicken.jpg', '散养土鸡，柴火慢炖', 20, TRUE),
('红烧肉', 58.00, '/images/dishes/pork.jpg', '土猪五花肉，秘制酱汁', 15, TRUE),
('清蒸鲈鱼', 78.00, '/images/dishes/fish.jpg', '水库鲜活鲈鱼', 10, TRUE),
('时令蔬菜', 28.00, '/images/dishes/veggie.jpg', '自家菜地新鲜采摘', 30, TRUE),
('土鸡汤', 48.00, '/images/dishes/soup.jpg', '老母鸡炖汤，滋补养生', 15, TRUE),
('农家小炒肉', 38.00, '/images/dishes/stirfry.jpg', '土猪里脊配青椒', 20, TRUE),
('香煎豆腐', 22.00, '/images/dishes/tofu.jpg', '手工石磨豆腐', 25, TRUE),
('凉拌黄瓜', 16.00, '/images/dishes/cucumber.jpg', '自家种的黄瓜', 30, TRUE),
('米饭', 3.00, '/images/dishes/rice.jpg', '新米现蒸', -1, TRUE),
('酸梅汤', 8.00, '/images/dishes/plum.jpg', '自制酸梅汤', -1, TRUE);

INSERT IGNORE INTO products (name, type, price, member_price, image_url, description, daily_quota, remaining_quota, is_available) VALUES
('采摘体验券', 'service', 58.00, 48.00, '/images/products/pick.jpg', '时令水果采摘，含2斤带走', 50, 50, TRUE),
('钓鱼体验券', 'service', 38.00, 30.00, '/images/products/fishing.jpg', '水库垂钓2小时，含鱼竿', 30, 30, TRUE),
('农家土鸡蛋', 'physical', 28.00, 22.00, '/images/products/eggs.jpg', '散养土鸡蛋30枚装', 20, 20, TRUE),
('自制腊肉', 'physical', 68.00, 55.00, '/images/products/bacon.jpg', '传统工艺烟熏腊肉500g', 15, 15, TRUE),
('时令水果礼盒', 'physical', 88.00, 70.00, '/images/products/fruit.jpg', '当季水果精选礼盒', 10, 10, TRUE),
('BBQ烧烤套餐', 'service', 128.00, 108.00, '/images/products/bbq.jpg', '含食材+烤炉+碳火', 20, 20, TRUE);

INSERT IGNORE INTO membership_configs (annual_price, discount_rate, gift_product_ids) VALUES
(199.00, 0.85, '[1, 2]');

INSERT IGNORE INTO plots (plot_number, name, area, description, status) VALUES
('P-001', '春晓园A区', 20.00, '阳光充足，适合种植番茄、辣椒', 'available'),
('P-002', '春晓园B区', 25.00, '土壤肥沃，适合种植叶菜类', 'available'),
('P-003', '夏荫园A区', 30.00, '半阴环境，适合种植瓜果类', 'available'),
('P-004', '夏荫园B区', 20.00, '排水良好，适合种植根茎类', 'available'),
('P-005', '秋实园A区', 35.00, '面积最大，适合家庭共同种植', 'available'),
('P-006', '秋实园B区', 15.00, '小巧精致，适合新手体验', 'available'),
('P-007', '冬暖园A区', 20.00, '温室地块，四季可种', 'available'),
('P-008', '冬暖园B区', 25.00, '温室地块，配备自动灌溉', 'available');

INSERT IGNORE INTO cameras (identifier, name, ip_address, status) VALUES
('CAM-001', '春晓园摄像头', '192.168.1.101', 'online'),
('CAM-002', '夏荫园摄像头', '192.168.1.102', 'online'),
('CAM-003', '秋实园摄像头', '192.168.1.103', 'offline'),
('CAM-004', '冬暖园摄像头', '192.168.1.104', 'online');

INSERT IGNORE INTO camera_plot_bindings (camera_id, plot_id) VALUES
(1, 1), (1, 2), (2, 3), (2, 4), (3, 5), (3, 6), (4, 7), (4, 8);

INSERT IGNORE INTO garden_services (name, price, description, is_available) VALUES
('浇水服务', 10.00, '专业园丁代为浇水，保证植物水分充足', TRUE),
('施肥服务', 20.00, '使用有机肥料，促进植物健康生长', TRUE),
('除草服务', 15.00, '清除杂草，保持地块整洁', TRUE),
('病虫害防治', 30.00, '专业检测+绿色防治方案', TRUE),
('采收服务', 15.00, '成熟后代为采收并妥善保存', TRUE),
('全托管服务', 80.00, '包含浇水+施肥+除草+病虫害防治+采收', TRUE);
