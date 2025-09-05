-- 为news表添加自定义标签字段
ALTER TABLE news ADD COLUMN newsCustomTags varchar(1024) comment '自定义标签（JSON格式存储多个标签，如：["#人工智能", "#Java"]' AFTER newsGameTagName;

-- 添加索引以提高查询性能
CREATE INDEX idx_news_custom_tags ON news (newsCustomTags(255));