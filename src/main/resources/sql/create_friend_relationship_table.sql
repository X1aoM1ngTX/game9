-- 创建好友关系表
CREATE TABLE friendRelationship (
    relationshipId BIGINT AUTO_INCREMENT COMMENT '关系ID' PRIMARY KEY,
    userId BIGINT NOT NULL COMMENT '用户ID',
    friendId BIGINT NOT NULL COMMENT '好友ID',
    friendStatus TINYINT NOT NULL DEFAULT 0 COMMENT '关系状态：0-待确认 1-已确认 2-已拒绝 3-已删除',
    friendRemark VARCHAR(255) NULL COMMENT '好友备注',
    friendCreateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    friendUpdateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    friendIsDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    CONSTRAINT uk_user_friend UNIQUE (userId, friendId),
    CONSTRAINT friendRelationship_user_userId_fk FOREIGN KEY (userId) REFERENCES user (userId) ON UPDATE CASCADE,
    CONSTRAINT friendRelationship_user_friendId_fk FOREIGN KEY (friendId) REFERENCES user (userId) ON UPDATE CASCADE
) COMMENT '好友关系表';
-- 创建索引
CREATE INDEX idx_userId ON friendRelationship (userId);
CREATE INDEX idx_friendId ON friendRelationship (friendId);
CREATE INDEX idx_friendStatus ON friendRelationship (friendStatus);