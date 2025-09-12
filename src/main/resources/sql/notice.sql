CREATE TABLE `notice`  (
  `noticeId` bigint NOT NULL AUTO_INCREMENT COMMENT '公告Id',
  `noticeTitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `noticeContent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `noticeType` tinyint NOT NULL DEFAULT 0 COMMENT '公告类型（0-普通，1-重要，2-系统）',
  `noticeStatus` tinyint NOT NULL DEFAULT 0 COMMENT '公告状态（0-草稿，1-已发布）',
  `noticeCreatorId` bigint NULL DEFAULT NULL COMMENT '创建者',
  `noticeCreateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `noticePublishTime` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `noticeExpireTime` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `noticeIsDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`noticeId`) USING BTREE,
  INDEX `fk_notice_creator`(`noticeCreatorId`) USING BTREE,
  INDEX `idx_expire`(`noticeExpireTime`) USING BTREE,
  INDEX `idx_is_delete`(`noticeIsDelete`) USING BTREE,
  INDEX `idx_status_publish`(`noticeStatus`, `noticePublishTime`) USING BTREE,
  CONSTRAINT `fk_notice_creator` FOREIGN KEY (`noticeCreatorId`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;
