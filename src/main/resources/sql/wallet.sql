CREATE TABLE `wallet`  (
  `walletId` bigint NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
  `userId` bigint NOT NULL COMMENT '用户ID',
  `walletBalance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '钱包余额',
  `walletStatus` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常 0-冻结',
  `createdTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`walletId`) USING BTREE,
  UNIQUE INDEX `wallet_userId_unique`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '钱包表' ROW_FORMAT = Dynamic;
