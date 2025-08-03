-- auto-generated definition
create table wishlist
(
    wishlistId          bigint auto_increment comment '愿望单id'
        primary key,
    userId      bigint                             not null comment '用户id',
    gameId      bigint                             not null comment '游戏id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted  tinyint  default 0                 not null comment '是否删除',
    constraint uni_userId_gameId
        unique (userId, gameId)
)
    comment '愿望单';
