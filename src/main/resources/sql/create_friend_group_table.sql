-- 创建好友分组表
CREATE TABLE friendGroup (
    groupId BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    groupName VARCHAR(50) NOT NULL COMMENT '分组名称',
    groupOrder INT DEFAULT 0 COMMENT '分组排序',
    groupCreateTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    groupUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    groupIsDelete TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除 1-已删除)',
    PRIMARY KEY (groupId),
    UNIQUE KEY `uk_user_group_name` (`userId`, `groupName`, `groupIsDelete`),
    KEY `idx_user_id` (`userId`),
    KEY `idx_group_order` (`groupOrder`),
    CONSTRAINT `fk_friend_group_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友分组表';

-- 为好友关系表添加分组ID字段和索引
ALTER TABLE `friendRelationship` 
ADD COLUMN `groupId` BIGINT NULL COMMENT '好友分组ID' AFTER `friendRemark`,
ADD KEY `idx_group_id` (`groupId`),
ADD KEY `idx_user_friend_status` (`userId`, `friendId`, `friendStatus`),
ADD KEY `idx_user_status` (`userId`, `friendStatus`);

-- 为用户表添加在线状态相关字段和索引
ALTER TABLE `user` 
ADD COLUMN `isOnline` BOOLEAN DEFAULT FALSE COMMENT '是否在线' AFTER `userNickname`,
ADD COLUMN `lastOnlineTime` DATETIME NULL COMMENT '最后在线时间' AFTER `isOnline`,
ADD KEY `idx_online_status` (`isOnline`, `lastOnlineTime`);

-- 插入默认分组数据（可选）
INSERT INTO `friendGroup` (`userId`, `groupName`, `groupOrder`) 
SELECT DISTINCT `userId`, '默认分组', 1 
FROM `user` 
WHERE `userIsDelete` = 0
ON DUPLICATE KEY UPDATE `groupName` = VALUES(`groupName`);