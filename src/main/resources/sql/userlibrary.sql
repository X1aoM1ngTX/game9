-- 用户游戏库表
CREATE TABLE `userlibrary`  (
  `userLibraryId` bigint NOT NULL AUTO_INCREMENT COMMENT '用户游戏库ID',
  `userId` bigint NOT NULL COMMENT '用户ID',
  `gameId` bigint NOT NULL COMMENT '游戏ID',
  PRIMARY KEY (`userLibraryId`) USING BTREE,
  INDEX `userLibrary_game_gameId_fk`(`gameId`) USING BTREE,
  INDEX `userLibrary_user_userId_fk`(`userId`) USING BTREE,
  CONSTRAINT `userLibrary_game_gameId_fk` FOREIGN KEY (`gameId`) REFERENCES `game` (`gameId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `userLibrary_user_userId_fk` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 90 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户游戏库' ROW_FORMAT = Dynamic;
