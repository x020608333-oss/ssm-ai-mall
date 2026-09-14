-- =====================================================================
-- 智选商城 ssm-ai-mall 数据库初始化脚本（MySQL 8.x）
-- 执行：mysql -h127.0.0.1 -P3306 -uroot -p < db/init.sql
-- =====================================================================
DROP DATABASE IF EXISTS ai_mall;
CREATE DATABASE ai_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ai_mall;

-- ---------------- 用户表 ----------------
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录名',
  `password`    VARCHAR(64)  NOT NULL COMMENT '密码(MD5)',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色：user普通用户 admin管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='用户表';

-- ---------------- 收货地址表 ----------------
CREATE TABLE `address` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `receiver`    VARCHAR(50)  NOT NULL COMMENT '收货人',
  `phone`       VARCHAR(20)  NOT NULL,
  `province`    VARCHAR(50)  DEFAULT '',
  `city`        VARCHAR(50)  DEFAULT '',
  `district`    VARCHAR(50)  DEFAULT '',
  `detail`      VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default`  TINYINT      NOT NULL DEFAULT 0 COMMENT '默认地址',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='收货地址表';

-- ---------------- 商品分类表 ----------------
CREATE TABLE `category` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon`       VARCHAR(10) DEFAULT '' COMMENT '图标(emoji)',
  `sort`       INT         NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status`     TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='商品分类表';

-- ---------------- 商品表 ----------------
CREATE TABLE `product` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT        NOT NULL,
  `name`        VARCHAR(200)  NOT NULL COMMENT '商品名',
  `subtitle`    VARCHAR(255)  DEFAULT '' COMMENT '副标题/卖点',
  `main_image`  VARCHAR(255)  DEFAULT '' COMMENT '主图URL',
  `price`       DECIMAL(10,2) NOT NULL COMMENT '售价',
  `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存',
  `sales`       INT           NOT NULL DEFAULT 0 COMMENT '销量',
  `detail`      TEXT          COMMENT '商品详情',
  `is_hot`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否热卖推荐',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_name` (`name`),
  KEY `idx_hot_status` (`is_hot`, `status`)
) ENGINE=InnoDB COMMENT='商品表';

-- ---------------- 购物车表 ----------------
CREATE TABLE `cart` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `product_id` BIGINT   NOT NULL,
  `quantity`   INT      NOT NULL DEFAULT 1 COMMENT '数量',
  `checked`    TINYINT  NOT NULL DEFAULT 1 COMMENT '是否勾选结算',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='购物车表';

-- ---------------- 订单表（表名用 orders，避开保留字） ----------------
CREATE TABLE `orders` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`        VARCHAR(32)   NOT NULL COMMENT '订单号',
  `user_id`         BIGINT        NOT NULL,
  `total_amount`    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付待发货 2已发货 3已完成 5已取消 6已关闭',
  `receiver`        VARCHAR(50)   NOT NULL,
  `phone`           VARCHAR(20)   NOT NULL,
  `address_snapshot` VARCHAR(300) NOT NULL COMMENT '下单时地址快照',
  `remark`          VARCHAR(200)  DEFAULT '',
  `pay_time`        DATETIME      DEFAULT NULL COMMENT '支付时间',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB COMMENT='订单主表';

-- ---------------- 订单明细表 ----------------
CREATE TABLE `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT        NOT NULL,
  `order_no`      VARCHAR(32)   NOT NULL,
  `product_id`    BIGINT        NOT NULL,
  `product_name`  VARCHAR(200)  NOT NULL COMMENT '商品名快照',
  `product_image` VARCHAR(255)  DEFAULT '',
  `price`         DECIMAL(10,2) NOT NULL COMMENT '成交单价快照',
  `quantity`      INT           NOT NULL,
  `subtotal`      DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB COMMENT='订单明细表';

-- ---------------- 用户行为表（AI推荐数据源：浏览/收藏/加购/购买） ----------------
CREATE TABLE `behavior_log` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `product_id` BIGINT   NOT NULL,
  `type`       TINYINT  NOT NULL COMMENT '1浏览 2收藏 3加购 4购买',
  `score`      INT      NOT NULL DEFAULT 1 COMMENT '行为权重分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`, `type`),
  KEY `idx_product_type` (`product_id`, `type`)
) ENGINE=InnoDB COMMENT='用户行为日志表（AI推荐数据源）';

-- ---------------- AI客服会话消息表 ----------------
CREATE TABLE `chat_message` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID(浏览器Cookie)',
  `role`       VARCHAR(10) NOT NULL COMMENT 'user / assistant',
  `content`    TEXT       NOT NULL,
  `source`     VARCHAR(20) DEFAULT 'llm' COMMENT '回复来源：llm大模型 / rule规则 / db商品检索',
  `created_at` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`, `id`)
) ENGINE=InnoDB COMMENT='AI客服会话消息表';

-- =====================================================================
-- 示例数据
-- =====================================================================
-- 密码统一为 123456（MD5）
INSERT INTO `user` (`username`,`password`,`nickname`,`phone`,`email`,`role`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员',  '13800000000', 'admin@mall.com',  'admin'),
('user1', 'e10adc3949ba59abbe56e057f20f883e', '张三',    '13800000001', 'zs@mall.com',     'user'),
('user2', 'e10adc3949ba59abbe56e057f20f883e', '李四',    '13800000002', 'ls@mall.com',     'user'),
('user3', 'e10adc3949ba59abbe56e057f20f883e', '王五',    '13800000003', 'ww@mall.com',     'user'),
('user4', 'e10adc3949ba59abbe56e057f20f883e', '赵六',    '13800000004', 'zl@mall.com',     'user');

INSERT INTO `address` (`user_id`,`receiver`,`phone`,`province`,`city`,`district`,`detail`,`is_default`) VALUES
(2,'张三','13800000001','广东省','深圳市','南山区','科技园南路88号3栋501',1),
(2,'张三','13800000001','广东省','深圳市','福田区','华强北街道200号',0),
(3,'李四','13800000002','北京市','北京市','海淀区','中关村大街1号A座1102',1);

INSERT INTO `category` (`id`,`name`,`icon`,`sort`) VALUES
(1,'手机数码','📱',1),
(2,'电脑办公','💻',2),
(3,'家用电器','🏠',3),
(4,'服饰鞋包','👕',4),
(5,'美妆个护','💄',5),
(6,'食品生鲜','🍎',6);

INSERT INTO `product` (`id`,`category_id`,`name`,`subtitle`,`main_image`,`price`,`stock`,`sales`,`detail`,`is_hot`) VALUES
(1,1,'小米14 Pro 16GB+512GB 黑色','徕卡光学Summilux镜头 骁龙8Gen3','/static/img/p1.jpg',4999.00,120,856,'小米14 Pro，徕卡专业光学镜头，骁龙8Gen3旗舰处理器，2K护眼屏，48小时超长续航。',1),
(2,1,'Apple iPhone 15 128GB 蓝色','灵动岛 A16仿生芯片 4800万像素','/static/img/p2.jpg',5999.00,80,1203,'iPhone 15，灵动岛交互，A16仿生芯片，4800万像素主摄，USB-C接口。',1),
(3,1,'华为Mate 60 Pro 12GB+512GB','超可靠玄武架构 卫星通话','/static/img/p3.jpg',6999.00,50,1560,'华为Mate 60 Pro，玄武架构，支持卫星通话，全焦段超清影像。',1),
(4,1,'Redmi K70 12GB+256GB','第二代骁龙8 2K中国屏','/static/img/p4.jpg',2599.00,200,2301,'Redmi K70，第二代骁龙8，2K高端中国屏，120W秒充。',0),
(5,1,'AirPods Pro 2 (USB-C)','主动降噪 自适应通透模式','/static/img/p5.jpg',1799.00,150,990,'AirPods Pro 2，H2芯片主动降噪，自适应通透模式，USB-C充电。',0),
(6,1,'佳能EOS R50 微单相机套机','2420万像素 4K短片','/static/img/p6.jpg',5999.00,30,208,'佳能EOS R50，2420万像素APS-C画幅，4K短片，轻巧便携。',0),
(7,2,'联想拯救者Y9000P 2024 i9/RTX4060','电竞显卡旗舰游戏本','/static/img/p7.jpg',8999.00,40,645,'拯救者Y9000P，i9-14900HX处理器，RTX4060独显，2.5K 240Hz电竞屏。',1),
(8,2,'Apple MacBook Air 13 M3 8+256','轻薄本 M3芯片 18小时续航','/static/img/p8.jpg',7999.00,35,512,'MacBook Air，M3芯片，13.6英寸Liquid视网膜屏，长达18小时续航。',1),
(9,2,'罗技MX Master 3S 无线鼠标','8K DPI 静音按键 电磁滚轮','/static/img/p9.jpg',749.00,300,1500,'MX Master 3S，8000DPI精准追踪，静音微动， MagSpeed电磁滚轮。',0),
(10,2,'机械革命无界14Pro 轻薄本','标压R7 2.8K 120Hz屏','/static/img/p10.jpg',3699.00,90,388,'无界14Pro，标压R7处理器，2.8K 120Hz高刷屏，全金属机身。',0),
(11,2,'金士顿NVMe固态硬盘 1TB','PCIe 4.0 读速7000MB/s','/static/img/p11.jpg',429.00,500,2100,'金士顿NVMe固态硬盘，PCIe 4.0四通道，读速最高7000MB/s。',0),
(12,2,'得力办公用品大礼包','文具套装 开学必备','/static/img/p12.jpg',59.00,1000,5600,'得力文具大礼包，中性笔笔记本文件袋等22件套。',0),
(13,3,'美的1.5匹新一级能效变频空调','智能变频 静音节能','/static/img/p13.jpg',2799.00,60,780,'美的1.5匹变频空调，新一级能效，智能温控，静音运行。',1),
(14,3,'海尔10公斤滚筒洗衣机全自动','变频电机 除菌洗','/static/img/p14.jpg',2199.00,45,460,'海尔滚筒洗衣机，10公斤大容量，变频电机，除菌洗程序。',0),
(15,3,'格力落地扇 机械款','大风量 广角送风','/static/img/p15.jpg',199.00,400,3200,'格力落地扇，五叶大风量，广角送风，三档风速可调。',0),
(16,3,'九阳破壁机家用豆浆机','低音降噪 免滤豆浆','/static/img/p16.jpg',399.00,200,1100,'九阳破壁机，低音降噪技术，免滤豆浆，多功能料理。',0),
(17,3,'米家扫地机器人S20','4000Pa大吸力 激光导航','/static/img/p17.jpg',1299.00,70,350,'米家扫拖机器人S20，4000Pa大吸力，激光导航，扫拖一体。',1),
(18,3,'苏泊尔电饭煲4L 智能预约','球釜内胆 一键煮饭','/static/img/p18.jpg',219.00,260,1800,'苏泊尔电饭煲，4L容量球釜内胆，24小时智能预约。',0),
(19,4,'优衣库男装摇粒绒拉链外套','保暖轻便 秋冬新品','/static/img/p19.jpg',199.00,350,2900,'优衣库摇粒绒外套，轻便保暖，多色可选。',0),
(20,4,'李宁跑步鞋 赤兔7Pro','回弹缓震 专业竞速','/static/img/p20.jpg',469.00,180,760,'李宁赤兔7Pro，䨻丝面中底，回弹缓震，专业竞速跑鞋。',1),
(21,4,'波司登羽绒服 短款时尚款','90%白鸭绒 轻盈保暖','/static/img/p21.jpg',899.00,100,430,'波司登羽绒服，90%白鸭绒充绒，防钻绒工艺。',0),
(22,4,'新秀丽双肩包 商务通勤','15.6英寸电脑仓 防泼水','/static/img/p22.jpg',399.00,150,620,'新秀丽商务双肩包，15.6英寸电脑仓，防泼水面料。',0),
(23,5,'兰蔻小黑瓶精华肌底液50ml','修护屏障 透亮肌肤','/static/img/p23.jpg',1080.00,90,890,'兰蔻小黑瓶精华，第二代小黑瓶，修护肌底，焕亮肤色。',1),
(24,5,'薇诺娜舒缓保湿特护霜50g','敏感肌可用 医研共创','/static/img/p24.jpg',268.00,220,1340,'薇诺娜特护霜，敏感肌专研，舒缓保湿修护屏障。',0),
(25,5,'飞利浦电动牙刷HX6730','声波震动 三种模式','/static/img/p25.jpg',299.00,180,980,'飞利浦声波震动牙刷，31000次/分钟震动，三种洁牙模式。',0),
(26,6,'三只松鼠坚果大礼袋 1.5kg','每日坚果 8袋装','/static/img/p26.jpg',128.00,500,4300,'三只松鼠坚果大礼袋，8种坚果科学配比，新鲜锁存。',0),
(27,6,'茅台王子酒 酱香经典 53度500ml','酱香型白酒 纯粮酿造','/static/img/p27.jpg',368.00,80,350,'茅台王子酒酱香经典，53度酱香型，纯粮固态发酵。',0),
(28,6,'百草味猪肉脯200g','肉质紧实 香甜有嚼劲','/static/img/p28.jpg',29.90,800,6100,'百草味猪肉脯，精选猪后腿肉，片片饱满，香甜有嚼劲。',0);

INSERT INTO `cart` (`user_id`,`product_id`,`quantity`,`checked`) VALUES
(2,5,1,1),
(2,11,2,1),
(3,20,1,1);

-- 历史订单示例（两条为当天，便于管理端仪表盘演示）
INSERT INTO `orders` (`order_no`,`user_id`,`total_amount`,`status`,`receiver`,`phone`,`address_snapshot`,`pay_time`,`created_at`) VALUES
('2026091209301200001',2,5987.20,3,'张三','13800000001','广东省深圳市南山区科技园南路88号3栋501',DATE_SUB(NOW(),INTERVAL 20 HOUR),DATE_SUB(NOW(),INTERVAL 21 HOUR)),
('2026091215003000002',2,5099.00,1,'张三','13800000001','广东省深圳市南山区科技园南路88号3栋501',DATE_SUB(NOW(),INTERVAL 2 HOUR),DATE_SUB(NOW(),INTERVAL 3 HOUR)),
('2026091218100000003',3,469.00,0,'李四','13800000002','北京市海淀区中关村大街1号A座1102',NULL,DATE_SUB(NOW(),INTERVAL 1 HOUR));
INSERT INTO `order_item` (`order_id`,`order_no`,`product_id`,`product_name`,`product_image`,`price`,`quantity`,`subtotal`) VALUES
(1,'2026091209301200001',1,'小米14 Pro 16GB+512GB 黑色','/static/img/p1.jpg',4999.00,1,4999.00),
(1,'2026091209301200001',9,'罗技MX Master 3S 无线鼠标','/static/img/p9.jpg',749.00,1,749.00),
(1,'2026091209301200001',28,'百草味猪肉脯200g','/static/img/p28.jpg',29.90,8,239.20),
(2,'2026091215003000002',8,'Apple MacBook Air 13 M3 8+256','/static/img/p8.jpg',4999.00,1,4999.00),
(2,'2026091215003000002',12,'得力办公用品大礼包','/static/img/p12.jpg',50.00,2,100.00),
(3,'2026091218100000003',20,'李宁跑步鞋 赤兔7Pro','/static/img/p20.jpg',469.00,1,469.00);

-- 用户行为日志（AI推荐数据源）
-- 张三(id=2)：浏览了 1,2,3，加购了 1,7，购买了 1
INSERT INTO `behavior_log` (`user_id`,`product_id`,`type`,`score`,`created_at`) VALUES
(2,1,1,1,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(2,2,1,1,DATE_SUB(NOW(),INTERVAL 4 DAY)),
(2,3,1,1,DATE_SUB(NOW(),INTERVAL 4 DAY)),
(2,1,3,3,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(2,7,3,3,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(2,1,4,5,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(3,1,4,5,DATE_SUB(NOW(),INTERVAL 6 DAY)),
(3,3,4,5,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(3,7,4,5,DATE_SUB(NOW(),INTERVAL 2 DAY)),
(3,2,1,1,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(3,3,1,1,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(3,20,3,3,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(4,1,4,5,DATE_SUB(NOW(),INTERVAL 7 DAY)),
(4,2,4,5,DATE_SUB(NOW(),INTERVAL 6 DAY)),
(4,3,4,5,DATE_SUB(NOW(),INTERVAL 2 DAY)),
(4,1,1,1,DATE_SUB(NOW(),INTERVAL 4 DAY)),
(5,7,4,5,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(5,9,4,5,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(5,17,4,5,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(5,13,3,3,DATE_SUB(NOW(),INTERVAL 2 DAY));
