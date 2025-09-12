-- 创建好友关系表
CREATE TABLE `friendrelationship`  (
  `relationshipId` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `userId` bigint NOT NULL COMMENT '用户ID',
  `friendId` bigint NOT NULL COMMENT '好友ID',
  `friendStatus` tinyint NOT NULL DEFAULT 0 COMMENT '关系状态：0-待确认 1-已确认 2-已拒绝 3-已删除',
  `friendRemark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '好友备注',
  `friendCreateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `friendUpdateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `friendIsDeleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`relationshipId`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`userId`, `friendId`) USING BTREE,
  INDEX `idx_userId`(`userId`) USING BTREE,
  INDEX `idx_friendId`(`friendId`) USING BTREE,
  INDEX `idx_friendStatus`(`friendStatus`) USING BTREE,
  INDEX `idx_user_friend_status`(`userId`, `friendId`, `friendStatus`) USING BTREE,
  INDEX `idx_user_status`(`userId`, `friendStatus`) USING BTREE,
  CONSTRAINT `friendRelationship_user_friendId_fk` FOREIGN KEY (`friendId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `friendRelationship_user_userId_fk` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;
