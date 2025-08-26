-- 创建聊天表（如果不存在）
USE xmgame;

-- 检查表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS chat_message (
    messageId BIGINT AUTO_INCREMENT PRIMARY KEY,
    senderId BIGINT NOT NULL COMMENT '发送者ID',
    receiverId BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    messageType TINYINT DEFAULT 1 COMMENT '消息类型：1-文本 2-图片 3-文件',
    messageStatus TINYINT DEFAULT 0 COMMENT '消息状态：0-已发送 1-已送达 2-已读',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDeleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    INDEX idx_sender_receiver (senderId, receiverId),
    INDEX idx_create_time (createTime),
    INDEX idx_status (messageStatus)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

CREATE TABLE IF NOT EXISTS chat_session (
    sessionId BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1Id BIGINT NOT NULL COMMENT '用户1 ID',
    user2Id BIGINT NOT NULL COMMENT '用户2 ID',
    lastMessage TEXT COMMENT '最后一条消息',
    lastMessageTime DATETIME COMMENT '最后消息时间',
    unreadCountUser1 INT DEFAULT 0 COMMENT '用户1未读数',
    unreadCountUser2 INT DEFAULT 0 COMMENT '用户2未读数',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_users (user1Id, user2Id),
    INDEX idx_last_message_time (lastMessageTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 显示创建结果
SELECT '聊天表创建完成' as message;