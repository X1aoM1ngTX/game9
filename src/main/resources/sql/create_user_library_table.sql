-- 用户游戏库表
create table game9.userlibrary (
    userLibraryId bigint auto_increment comment '用户游戏库ID' primary key,
    userId bigint not null comment '用户ID',
    gameId bigint not null comment '游戏ID',
    constraint userLibrary_game_gameId_fk foreign key (gameId) references game9.game (gameId) on update cascade,
    constraint userLibrary_user_userId_fk foreign key (userId) references game9.user (userId) on update cascade
) comment '用户游戏库';