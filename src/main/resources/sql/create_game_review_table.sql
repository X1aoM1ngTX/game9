-- 创建游戏评价表
create table gameReview
(
    reviewId             bigint auto_increment comment '评价ID' primary key,
    userId               bigint                             not null comment '用户ID',
    gameId               bigint                             not null comment '游戏ID',
    gameReviewRating     decimal(2, 1)                      not null comment '评分（1-5分，支持0.5分）',
    gameReviewContent    text null comment '评价内容',
    gameReviewCreateTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gameReviewUpdateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    gameReviewIsDeleted  tinyint  default 0                 not null comment '是否删除'
) comment '游戏评价表';
create index idx_gameReviewCreateTime on gameReview (gameReviewCreateTime);
create index idx_gameReviewGameId on gameReview (gameId);
create index idx_gameReviewUserId on gameReview (userId);