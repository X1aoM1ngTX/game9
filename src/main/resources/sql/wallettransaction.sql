CREATE TABLE `wallettransaction`  (
  `transactionId` bigint NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `userId` bigint NOT NULL COMMENT '用户ID',
  `transactionType` tinyint NOT NULL COMMENT '交易类型：1-充值 2-消费 3-退款 4-赠送',
  `transactionAmount` decimal(12, 2) NOT NULL COMMENT '交易金额（正数表示收入，负数表示支出）',
  `balanceAfter` decimal(12, 2) NOT NULL COMMENT '交易后余额',
  `transactionDescription` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '交易描述',
  `orderId` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `transactionStatus` tinyint NULL DEFAULT 1 COMMENT '交易状态：0-处理中 1-成功 2-失败',
  `paymentMethod` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式：模拟支付-支付宝-微信-银行卡',
  `thirdPartyTransactionId` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '第三方交易号（模拟）',
  `createdTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`transactionId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '钱包交易记录表' ROW_FORMAT = Dynamic;
