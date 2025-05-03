N-- 创建资讯表 (news)
CREATE TABLE news (
    newsId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资讯ID',
    newsTitle VARCHAR(255) NOT NULL COMMENT '资讯标题',
    newsContent TEXT NOT NULL COMMENT '资讯内容',
    newsAuthorId BIGINT NOT NULL COMMENT '作者ID (关联用户表)',
    newsStatus INT DEFAULT 0 COMMENT '资讯状态（0-草稿，1-已发布）',
    newsCreateTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    newsUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    newsPublishTime DATETIME NULL COMMENT '发布时间',
    newsIsDelete INT DEFAULT 0 COMMENT '是否删除 (0-未删除, 1-已删除)',
    newsCoverImage VARCHAR(1024) NULL COMMENT '资讯封面图 URL',
    newsSummary VARCHAR(500) NULL COMMENT '资讯摘要',
    newsViews INT DEFAULT 0 COMMENT '浏览次数',
    INDEX idx_news_author_id (news_author_id),
    INDEX idx_news_status (news_status),
    INDEX idx_news_publish_time (news_publish_time)
) COMMENT='资讯表';
ALTER TABLE news ADD CONSTRAINT fk_news_author FOREIGN KEY (newsAuthorId) REFERENCES user(userId);