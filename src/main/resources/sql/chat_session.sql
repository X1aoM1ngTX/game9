CREATE TABLE `chat_session`  (
  `sessionId` bigint NOT NULL AUTO_INCREMENT,
  `user1Id` bigint NOT NULL COMMENT '用户1 ID',
  `user2Id` bigint NOT NULL COMMENT '用户2 ID',
  `lastMessage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '最后一条消息',
  `lastMessageTime` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `unreadCountUser1` int NULL DEFAULT 0 COMMENT '用户1未读数',
  `unreadCountUser2` int NULL DEFAULT 0 COMMENT '用户2未读数',
  `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sessionId`) USING BTREE,
  UNIQUE INDEX `uk_users`(`user1Id`, `user2Id`) USING BTREE,
  INDEX `idx_last_message_time`(`lastMessageTime`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天会话表' ROW_FORMAT = Dynamic;
