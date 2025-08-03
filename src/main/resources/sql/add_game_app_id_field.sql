-- 为game表添加gameAppId字段
ALTER TABLE game ADD COLUMN gameAppId VARCHAR(12) COMMENT '游戏应用ID' AFTER gameCover;