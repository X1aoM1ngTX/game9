-- =============================================
-- Game9 游戏商城模拟支付系统数据库表结构
-- 创建时间：2025-09-09
-- 描述：钱包、订单、交易记录相关表
-- =============================================

-- 设置数据库字符集
USE xmgame;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 钱包表
-- =============================================
DROP TABLE IF EXISTS wallet;
CREATE TABLE wallet (
    walletId BIGINT AUTO_INCREMENT COMMENT '钱包ID' PRIMARY KEY,
    userId BIGINT NOT NULL COMMENT '用户ID',
    walletBalance DECIMAL(12, 2) DEFAULT 0.00 COMMENT '钱包余额',
    walletStatus TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-冻结',
    createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT wallet_user_userId_fk FOREIGN KEY (userId) REFERENCES user (userId),
    CONSTRAINT wallet_userId_unique UNIQUE (userId)
) COMMENT '钱包表';

-- =============================================
-- 2. 钱包交易记录表
-- =============================================
DROP TABLE IF EXISTS walletTransaction;
CREATE TABLE walletTransaction (
    transactionId BIGINT AUTO_INCREMENT COMMENT '交易ID' PRIMARY KEY,
    userId BIGINT NOT NULL COMMENT '用户ID',
    transactionType TINYINT NOT NULL COMMENT '交易类型：1-充值 2-消费 3-退款 4-赠送',
    transactionAmount DECIMAL(12, 2) NOT NULL COMMENT '交易金额（正数表示收入，负数表示支出）',
    balanceAfter DECIMAL(12, 2) NOT NULL COMMENT '交易后余额',
    transactionDescription VARCHAR(255) COMMENT '交易描述',
    orderId BIGINT COMMENT '关联订单ID',
    transactionStatus TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-成功 2-失败 0-处理中',
    paymentMethod VARCHAR(50) COMMENT '支付方式：模拟支付-支付宝-微信-银行卡',
    thirdPartyTransactionId VARCHAR(100) COMMENT '第三方交易号（模拟）',
    createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT wallet_transaction_user_userId_fk FOREIGN KEY (userId) REFERENCES user (userId),
    CONSTRAINT wallet_transaction_order_orderId_fk FOREIGN KEY (orderId) REFERENCES orders (orderId)
) COMMENT '钱包交易记录表';

-- =============================================
-- 3. 订单表
-- =============================================
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    orderId BIGINT AUTO_INCREMENT COMMENT '订单ID' PRIMARY KEY,
    orderNo VARCHAR(32) NOT NULL COMMENT '订单号',
    userId BIGINT NOT NULL COMMENT '用户ID',
    gameId BIGINT NOT NULL COMMENT '游戏ID',
    gameName VARCHAR(100) NOT NULL COMMENT '游戏名称',
    originalPrice DECIMAL(10, 2) NOT NULL COMMENT '原价',
    finalPrice DECIMAL(10, 2) NOT NULL COMMENT '最终价格',
    discountAmount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
    orderStatus TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待支付 2-已支付 3-已取消 4-已退款 5-已发货',
    paymentMethod VARCHAR(50) COMMENT '支付方式',
    paymentTime TIMESTAMP NULL COMMENT '支付时间',
    cancelReason VARCHAR(255) COMMENT '取消原因',
    refundReason VARCHAR(255) COMMENT '退款原因',
    refundTime TIMESTAMP NULL COMMENT '退款时间',
    remark TEXT COMMENT '备注',
    createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT orders_user_userId_fk FOREIGN KEY (userId) REFERENCES user (userId),
    CONSTRAINT orders_game_gameId_fk FOREIGN KEY (gameId) REFERENCES game (gameId),
    CONSTRAINT order_no_unique UNIQUE (orderNo)
) COMMENT '订单表';

-- =============================================
-- 4. 创建索引（优化查询性能）
-- =============================================

-- 钱包表索引
CREATE INDEX idx_wallet_userId ON wallet(userId);
CREATE INDEX idx_wallet_walletStatus ON wallet(walletStatus);

-- 交易记录表索引
CREATE INDEX idx_walletTransaction_userId ON walletTransaction(userId);
CREATE INDEX idx_walletTransaction_transactionType ON walletTransaction(transactionType);
CREATE INDEX idx_walletTransaction_transactionStatus ON walletTransaction(transactionStatus);
CREATE INDEX idx_walletTransaction_createdTime ON walletTransaction(createdTime);

-- 订单表索引
CREATE INDEX idx_orders_userId ON orders(userId);
CREATE INDEX idx_orders_gameId ON orders(gameId);
CREATE INDEX idx_orders_orderStatus ON orders(orderStatus);
CREATE INDEX idx_orders_orderNo ON orders(orderNo);
CREATE INDEX idx_orders_createdTime ON orders(createdTime);

-- =============================================
-- 5. 插入初始数据（可选）
-- =============================================

-- 为现有用户创建钱包（如果已有用户数据）
INSERT INTO wallet (userId, walletBalance, walletStatus)
SELECT userId, 0.00, 1 
FROM user 
WHERE userId NOT IN (SELECT userId FROM wallet);

-- 创建一个测试订单（可选）
-- INSERT INTO orders (orderNo, userId, gameId, gameName, originalPrice, finalPrice, orderStatus)
-- VALUES ('TEST001', 1, 1, '测试游戏', 99.00, 99.00, 1);

-- =============================================
-- 6. 创建视图（便于查询）
-- =============================================

-- 用户钱包视图
DROP VIEW IF EXISTS v_user_wallet;
CREATE VIEW v_user_wallet AS
SELECT 
    w.walletId,
    w.userId,
    u.userName,
    w.walletBalance,
    w.walletStatus,
    w.createdTime,
    w.updateTime
FROM wallet w
LEFT JOIN user u ON w.userId = u.userId;

-- 用户交易记录视图
DROP VIEW IF EXISTS v_user_transaction;
CREATE VIEW v_user_transaction AS
SELECT 
    wt.transactionId,
    wt.userId,
    u.userName,
    wt.transactionType,
    CASE wt.transactionType
        WHEN 1 THEN '充值'
        WHEN 2 THEN '消费'
        WHEN 3 THEN '退款'
        WHEN 4 THEN '赠送'
        ELSE '未知'
    END AS typeName,
    wt.transactionAmount,
    wt.balanceAfter,
    wt.transactionDescription,
    wt.orderId,
    wt.transactionStatus,
    CASE wt.transactionStatus
        WHEN 0 THEN '处理中'
        WHEN 1 THEN '成功'
        WHEN 2 THEN '失败'
        ELSE '未知'
    END AS statusName,
    wt.paymentMethod,
    wt.thirdPartyTransactionId,
    wt.createdTime,
    wt.updateTime
FROM walletTransaction wt
LEFT JOIN user u ON wt.userId = u.userId;

-- 用户订单视图
DROP VIEW IF EXISTS v_user_order;
CREATE VIEW v_user_order AS
SELECT 
    o.orderId,
    o.orderNo,
    o.userId,
    u.userName,
    o.gameId,
    o.gameName,
    o.originalPrice,
    o.finalPrice,
    o.discountAmount,
    o.orderStatus,
    CASE o.orderStatus
        WHEN 1 THEN '待支付'
        WHEN 2 THEN '已支付'
        WHEN 3 THEN '已取消'
        WHEN 4 THEN '已退款'
        WHEN 5 THEN '已发货'
        ELSE '未知'
    END AS statusName,
    o.paymentMethod,
    o.paymentTime,
    o.cancelReason,
    o.refundReason,
    o.refundTime,
    o.remark,
    o.createdTime,
    o.updateTime
FROM orders o
LEFT JOIN user u ON o.userId = u.userId;

-- =============================================
-- 7. 添加注释说明
-- =============================================

-- 钱包表字段注释
ALTER TABLE wallet 
    MODIFY COLUMN walletId BIGINT AUTO_INCREMENT COMMENT '钱包ID',
    MODIFY COLUMN userId BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    MODIFY COLUMN walletBalance DECIMAL(12, 2) DEFAULT 0.00 COMMENT '钱包余额（精确到分）',
    MODIFY COLUMN walletStatus TINYINT DEFAULT 1 COMMENT '钱包状态：1-正常 0-冻结',
    MODIFY COLUMN createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 交易记录表字段注释
ALTER TABLE walletTransaction
    MODIFY COLUMN transactionId BIGINT AUTO_INCREMENT COMMENT '交易ID',
    MODIFY COLUMN userId BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    MODIFY COLUMN transactionType TINYINT NOT NULL COMMENT '交易类型：1-充值 2-消费 3-退款 4-赠送',
    MODIFY COLUMN transactionAmount DECIMAL(12, 2) NOT NULL COMMENT '交易金额（正数为收入，负数为支出）',
    MODIFY COLUMN balanceAfter DECIMAL(12, 2) NOT NULL COMMENT '交易后的钱包余额',
    MODIFY COLUMN transactionDescription VARCHAR(255) COMMENT '交易描述信息',
    MODIFY COLUMN orderId BIGINT COMMENT '关联的订单ID（外键关联orders表）',
    MODIFY COLUMN transactionStatus TINYINT NOT NULL DEFAULT 1 COMMENT '交易状态：0-处理中 1-成功 2-失败',
    MODIFY COLUMN paymentMethod VARCHAR(50) COMMENT '支付方式：模拟支付、支付宝、微信、银行卡等',
    MODIFY COLUMN thirdPartyTransactionId VARCHAR(100) COMMENT '第三方支付平台交易号（模拟使用）',
    MODIFY COLUMN createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
    MODIFY COLUMN updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '交易更新时间';

-- 订单表字段注释
ALTER TABLE orders
    MODIFY COLUMN orderId BIGINT AUTO_INCREMENT COMMENT '订单ID',
    MODIFY COLUMN orderNo VARCHAR(32) NOT NULL COMMENT '订单编号（唯一）',
    MODIFY COLUMN userId BIGINT NOT NULL COMMENT '用户ID（外键关联user表）',
    MODIFY COLUMN gameId BIGINT NOT NULL COMMENT '游戏ID（外键关联game表）',
    MODIFY COLUMN gameName VARCHAR(100) NOT NULL COMMENT '游戏名称',
    MODIFY COLUMN originalPrice DECIMAL(10, 2) NOT NULL COMMENT '商品原价',
    MODIFY COLUMN finalPrice DECIMAL(10, 2) NOT NULL COMMENT '实际支付价格',
    MODIFY COLUMN discountAmount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
    MODIFY COLUMN orderStatus TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付 2-已支付 3-已取消 4-已退款 5-已发货',
    MODIFY COLUMN paymentMethod VARCHAR(50) COMMENT '支付方式',
    MODIFY COLUMN paymentTime TIMESTAMP NULL COMMENT '支付完成时间',
    MODIFY COLUMN cancelReason VARCHAR(255) COMMENT '订单取消原因',
    MODIFY COLUMN refundReason VARCHAR(255) COMMENT '退款原因',
    MODIFY COLUMN refundTime TIMESTAMP NULL COMMENT '退款处理时间',
    MODIFY COLUMN remark TEXT COMMENT '订单备注',
    MODIFY COLUMN createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    MODIFY COLUMN updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 完成！
-- =============================================

-- 表结构说明：
-- 1. wallet: 用户钱包表，存储用户余额和状态
-- 2. walletTransaction: 交易记录表，记录所有资金变动
-- 3. orders: 订单表，管理游戏购买订单

-- 使用示例：
-- 1. 为新用户创建钱包：INSERT INTO wallet (userId, walletBalance) VALUES (用户ID, 0.00);
-- 2. 用户充值：更新钱包余额 + 插入交易记录
-- 3. 创建订单：插入订单记录，状态为待支付
-- 4. 支付订单：更新订单状态 + 扣减钱包余额 + 插入交易记录 + 更新用户游戏库

-- 悲观锁使用示例：
-- 1. 查询并锁定钱包：SELECT * FROM wallet WHERE userId = ? FOR UPDATE
-- 2. 在事务中完成：扣减余额 + 记录流水 + 更新订单状态
-- 3. 提交事务自动释放锁

-- 注意事项：
-- 1. 所有金额操作必须在事务中完成
-- 2. 使用悲观锁（SELECT ... FOR UPDATE）防止并发问题
-- 3. 金额字段使用DECIMAL类型避免精度问题
-- 4. 订单号需要保证唯一性