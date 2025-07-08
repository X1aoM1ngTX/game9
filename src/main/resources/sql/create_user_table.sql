-- 用户表
create table user (
    userId bigint auto_increment comment '用户ID' primary key,
    userName varchar(256) not null comment '用户名',
    userEmail varchar(256) null comment '用户邮箱',
    userPhone varchar(128) null comment '用户电话',
    userPassword varchar(256) not null comment '用户密码',
    userCreatedTime timestamp default CURRENT_TIMESTAMP not null comment '用户创建时间',
    userIsDelete tinyint(1) default 0 not null comment '用户是否已删除',
    userIsAdmin tinyint(1) default 0 not null comment '用户是否为管理员',
    userProfile varchar(512) null comment '用户简介',
    userAvatar varchar(255) null comment '用户头像URL',
    userNickname varchar(255) null comment '用户昵称',
    constraint userEmail unique (userEmail),
    constraint userName unique (userName)
) comment '用户表';