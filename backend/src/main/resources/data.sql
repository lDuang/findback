SET NAMES utf8mb4;

-- 用户数据
INSERT INTO users (username, password, role) VALUES
    ('admin', 'admin', 'ADMIN'),
    ('user', 'password', 'USER'),
    ('student1', 'student123', 'USER');

-- 失物招领数据（图片指向 /api/uploads，构建镜像时拷贝同名文件即可直接展示）
INSERT INTO lost_items (user_id, title, description, status, location, image_url, created_at) VALUES
    (2, '蓝色棒球帽', 'Nike 品牌，蓝白撞色，帽檐有轻微磨损，在篮球场看台上丢失。', 'OPEN', '篮球场东侧看台第三排', '/api/uploads/campus_cap.jpg', '2025-12-16 14:30:00'),
    (2, '黑色保温杯', '480ml 不锈钢保温杯，杯盖有星巴克 Logo，杯身有划痕。', 'OPEN', '图书馆二楼自习区 A 区', '/api/uploads/thermos_black.jpg', '2025-12-17 09:15:00'),
    (3, '蓝色钥匙串', '包含 3 把钥匙，钥匙扣是蓝色小兔子造型，可提供购买记录证明。', 'CLAIMED', '食堂门口长椅', '/api/uploads/keychain_blue.jpg', '2025-12-18 17:45:00'),
    (3, '无线耳机充电盒', '白色 AirPods 充电盒，底部有灰色贴纸，盒内无耳机。', 'OPEN', '宿舍楼下小卖部收银台', '/api/uploads/airpods_case.jpg', '2025-12-18 18:40:00'),
    (2, '灰色连帽衫', '浅灰色连帽卫衣，左袖口有浅色油渍，尺码 M。', 'RESOLVED', '操场看台', '/api/uploads/hoodie_gray.jpg', '2025-12-19 11:22:00');

-- 认领记录
INSERT INTO claims (item_id, user_id, status, reason, created_at) VALUES
    (3, 3, 'APPROVED', '钥匙串上的蓝色兔子钥匙扣是礼物，可提供购买记录证明。', '2025-12-18 09:30:00'),
    (1, 2, 'PENDING', '帽子内侧有姓名缩写 L.H.，请核实后归还。', '2025-12-17 16:20:00'),
    (4, 2, 'PENDING', '盒子底部有贴纸，和我的一致，请管理员核对。', '2025-12-19 09:05:00');

-- 公告
INSERT INTO announcements (title, content, created_at) VALUES
    ('校园失物招领平台正式启用', '欢迎使用校园失物招领系统！如有拾获或遗失，请及时登记。', '2025-12-16 09:00:00'),
    ('贵重物品提醒', '近期遗失事件较多，请妥善保管证件和电子产品，离开教室前检查。', '2025-12-18 14:30:00');

-- 横幅（image_url 需要在构建时放置对应文件，或改为公网地址）
INSERT INTO banners (title, image_url, link_url, description, created_at) VALUES
    ('开学季失物集中点', '/api/uploads/banner_hall.jpg', '/items', '礼堂前服务台设置失物集中领取点。', '2025-12-17 10:00:00'),
    ('雨伞临时存放', '/api/uploads/banner_umbrella.jpg', '/items', '教学楼一层门卫可协助存放雨伞，请登记后领取。', '2025-12-16 08:30:00'),
    ('常见失物榜单', '/api/uploads/banner_toplist.jpg', '/items', '查看最近高频遗失物品，方便快速比对。', '2025-12-18 12:15:00');

-- 媒体文件记录（与 uploads 目录下的示例文件名对应）
INSERT INTO media_files (url, filename, uploader_id, created_at) VALUES
    ('/api/uploads/campus_cap.jpg', 'campus_cap.jpg', 2, '2025-12-16 14:35:00'),
    ('/api/uploads/thermos_black.jpg', 'thermos_black.jpg', 2, '2025-12-17 09:20:00'),
    ('/api/uploads/keychain_blue.jpg', 'keychain_blue.jpg', 3, '2025-12-18 17:50:00'),
    ('/api/uploads/airpods_case.jpg', 'airpods_case.jpg', 2, '2025-12-18 18:45:00'),
    ('/api/uploads/hoodie_gray.jpg', 'hoodie_gray.jpg', 2, '2025-12-19 11:26:00'),
    ('/api/uploads/banner_hall.jpg', 'banner_hall.jpg', 1, '2025-12-17 10:00:00'),
    ('/api/uploads/banner_umbrella.jpg', 'banner_umbrella.jpg', 1, '2025-12-16 08:30:00'),
    ('/api/uploads/banner_toplist.jpg', 'banner_toplist.jpg', 1, '2025-12-18 12:15:00');
