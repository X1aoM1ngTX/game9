CREATE TABLE `wishlist`  (
  `wishlistId` bigint NOT NULL AUTO_INCREMENT COMMENT '愿望单id',
  `userId` bigint NOT NULL COMMENT '用户id',
  `gameId` bigint NOT NULL COMMENT '游戏id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDeleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`wishlistId`) USING BTREE,
  UNIQUE INDEX `uni_userId_gameId`(`userId`, `gameId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '愿望单' ROW_FORMAT = Dynamic;
