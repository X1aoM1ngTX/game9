CREATE TABLE `gamereview`  (
  `reviewId` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `userId` bigint NOT NULL COMMENT '用户ID',
  `gameId` bigint NOT NULL COMMENT '游戏ID',
  `gameReviewRating` decimal(2, 1) NOT NULL COMMENT '评分（1-5分，支持0.5分）',
  `gameReviewContent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `gameReviewCreateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gameReviewUpdateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `gameReviewIsDeleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`reviewId`) USING BTREE,
  INDEX `idx_gameReviewCreateTime`(`gameReviewCreateTime`) USING BTREE,
  INDEX `idx_gameReviewGameId`(`gameId`) USING BTREE,
  INDEX `idx_gameReviewUserId`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏评价表' ROW_FORMAT = Dynamic;
