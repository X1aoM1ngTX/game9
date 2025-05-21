-- 创建游戏评价表
CREATE TABLE IF NOT EXISTS `gameReview` (
    `reviewId` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `userId` bigint NOT NULL COMMENT '用户ID',
    `gameId` bigint NOT NULL COMMENT '游戏ID',
    `rating` decimal(2,1) NOT NULL COMMENT '评分（1-5分，支持0.5分）',
    `content` text COMMENT '评价内容',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`reviewId`),
    KEY `idx_userId` (`userId`),
    KEY `idx_gameId` (`gameId`),
    KEY `idx_createTime` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏评价表'; 