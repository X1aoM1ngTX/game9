CREATE DATABASE gamehub;

CREATE TABLE user
(
    userId          bigint auto_increment comment '用户ID'
        primary key,
    userName        varchar(256)                         not null comment '用户名',
    userEmail       varchar(256)                         null comment '用户邮箱',
    userPhone       varchar(128)                         null comment '用户电话',
    userPassword    varchar(256)                         not null comment '用户密码',
    userCreatedTime timestamp  default CURRENT_TIMESTAMP not null comment '用户创建时间',
    userIsDelete    tinyint(1) default 0                 not null comment '用户是否已删除',
    userIsAdmin     tinyint(1) default 0                 not null comment '用户是否为管理员',
    userProfile     varchar(512)                         null comment '用户简介',
    userAvatar      varchar(255)                         null comment '用户头像URL',
    userNickname    varchar(255)                         null comment '用户昵称',
    constraint userEmail
        unique (userEmail),
    constraint userName
        unique (userName)
)
    comment '用户表';

CREATE TABLE game
(
    gameId               bigint auto_increment comment '游戏ID'
        primary key,
    gameName             varchar(256)                                                  not null comment '游戏名',
    gameDescription      text                                                          null comment '游戏描述',
    gamePrice            decimal(10, 2)                                                not null comment '游戏原始价格',
    gameStock            int                                                           not null comment '游戏库存',
    gameCreatedTime      timestamp      default CURRENT_TIMESTAMP                      not null comment '游戏创建时间',
    gameReleaseDate      date                                                          null comment '游戏发行时间',
    gameDev              varchar(256)                                                  null comment '游戏开发商',
    gamePub              varchar(256)                                                  null comment '游戏发行商',
    gameIsRemoved        tinyint(1)     default 0                                      not null comment '游戏是否下架',
    gameDiscountedPrices decimal(10, 2) default ((`gamePrice` * (1 - `gameDiscount`))) null comment '游戏打折价格',
    gameOnSale           tinyint(1)     default 0                                      not null comment '游戏是否正在打折',
    gameSaleStartTime    datetime                                                      null comment '游戏折扣开始时间',
    gameSaleEndTime      datetime                                                      null comment '游戏折扣结束时间',
    gameDiscount         decimal(5, 2)                                                 null comment '游戏折扣',
    gameCover            varchar(512)                                                  null comment '游戏封面'
)
    comment '游戏表';

CREATE TABLE userlibrary
(
    userLibraryId bigint auto_increment comment '用户游戏库ID'
        primary key,
    userId        bigint not null comment '用户ID',
    gameId        bigint not null comment '游戏ID',
    constraint userLibrary_game_gameId_fk
        foreign key (gameId) references game (gameId)
            on update cascade,
    constraint userLibrary_user_userId_fk
        foreign key (userId) references user (userId)
            on update cascade
)
    comment '用户游戏库';

