-- 创建游戏评价表
create table game9.gamereview (
    reviewId bigint auto_increment comment '评价ID' primary key,
    userId bigint not null comment '用户ID',
    gameId bigint not null comment '游戏ID',
    rating decimal(2, 1) not null comment '评分（1-5分，支持0.5分）',
    content text null comment '评价内容',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted tinyint default 0 not null comment '是否删除'
) comment '游戏评价表';
create index idx_createTime on game9.gamereview (createTime);
create index idx_gameId on game9.gamereview (gameId);
create index idx_userId on game9.gamereview (userId);