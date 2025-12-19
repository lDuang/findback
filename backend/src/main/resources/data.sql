SET NAMES utf8mb4;

-- 插入用户数据
INSERT INTO users (username, password, role) VALUES
    ('admin', 'admin', 'ADMIN'),
    ('user', 'password', 'USER'),
    ('student1', 'student123', 'USER');

-- 插入失物招领物品数据（只添加有图片的）
INSERT INTO lost_items (user_id, title, description, status, location, image_url, created_at) VALUES
    (2, '蓝色棒球帽', 'Nike品牌，蓝色为主色，白色Logo。帽檐有轻微磨损。在篮球场看台上丢失。', 'OPEN', '篮球场东侧看台第三排', 'https://cbu01.alicdn.com/img/ibank/O1CN011NFpEf1W7QOwZqfY0_!!2209475782741-0-cib.jpg', '2024-03-10 14:30:00'),
    (2, '保温杯', '黑色不锈钢保温杯，容量480ml，杯身有几处划痕。杯盖有星巴克Logo，内有茶渍。', 'OPEN', '图书馆二楼自习区A区', 'https://cbu01.alicdn.com/img/ibank/11649001350_1315753233.jpg', '2024-03-12 09:15:00'),
    (3, '钥匙串', '包含3把钥匙：1把大门钥匙（银色），2把办公室钥匙（铜色）。钥匙扣是蓝色小兔子造型。', 'CLAIMED', '食堂门口长椅', 'http://image2.sina.com.cn/qc/photo/zfltmdspq/U515P33T148D117172F2100DT20060103193445.jpg', '2024-03-05 17:45:00');

-- 插入认领记录数据（不添加图片）
INSERT INTO claims (item_id, user_id, status, reason, created_at) VALUES
    (3, 3, 'APPROVED', '钥匙串上的蓝色兔子钥匙扣是我女朋友送的生日礼物，可以提供购买记录证明。', '2024-03-06 09:30:00'),
    (1, 2, 'PENDING', '我上周在篮球场打比赛时丢失了这顶帽子，帽子内侧有我名字的缩写"L.H"。', '2024-03-11 16:20:00');

-- 插入公告数据（不添加图片）
INSERT INTO announcements (title, content, created_at) VALUES
    ('校园失物招领平台正式启用', '欢迎使用校园失物招领系统！请同学们妥善保管个人物品，如有丢失或拾获，请及时在本平台登记。', '2024-03-01 09:00:00'),
    ('关于贵重物品保管的重要提醒', '近期校园内发生多起物品遗失事件，特别提醒同学们贵重物品请随身携带，离开教室时请检查随身物品。', '2024-03-08 14:30:00');

-- 插入横幅数据（只使用示例图片或留空）
INSERT INTO banners (title, image_url, link_url) VALUES
    ('失物招领指南', NULL, '/help/how-to-claim'),
    ('常见失物排行', NULL, '/statistics/top-lost-items');

-- 插入媒体文件数据（只记录已有的图片）
INSERT INTO media_files (url, filename, uploader_id, created_at) VALUES
    ('https://cbu01.alicdn.com/img/ibank/O1CN011NFpEf1W7QOwZqfY0_!!2209475782741-0-cib.jpg', 'blue_cap.jpg', 2, '2024-03-10 14:35:00'),
    ('https://cbu01.alicdn.com/img/ibank/11649001350_1315753233.jpg', 'starbucks_cup.jpg', 2, '2024-03-12 09:20:00'),
    ('http://image2.sina.com.cn/qc/photo/zfltmdspq/U515P33T148D117172F2100DT20060103193445.jpg', 'key_chain.jpg', 3, '2024-03-05 17:50:00');

