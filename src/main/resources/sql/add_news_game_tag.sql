-- 为news表添加游戏标签字段
ALTER TABLE news ADD COLUMN newsGameTag bigint comment '游戏标签ID (关联游戏表)' AFTER newsViews;
ALTER TABLE news ADD COLUMN newsGameTagName varchar(256) comment '游戏标签名称' AFTER newsGameTag;

-- 添加外键约束（可选）
ALTER TABLE news ADD CONSTRAINT newsGameTagName FOREIGN KEY (newsGameTag) REFERENCES game(gameId);

-- 添加索引以提高查询性能
CREATE INDEX idx_news_game_tag ON news (newsGameTag);
CREATE INDEX idx_news_game_tag_name ON news (newsGameTagName);