-- 公告表
create table notice
(
    noticeId          bigint auto_increment comment '公告Id' primary key,
    noticeTitle       varchar(255)                       not null comment '公告标题',
    noticeContent     text                               not null comment '公告内容',
    noticeType        tinyint  default 0                 not null comment '公告类型（0-普通，1-重要，2-系统）',
    noticeStatus      tinyint  default 0                 not null comment '公告状态（0-草稿，1-已发布）',
    noticeCreatorId   bigint null comment '创建者',
    noticeCreateTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    noticePublishTime datetime null comment '发布时间',
    noticeExpireTime  datetime null comment '过期时间',
    noticeIsDelete    tinyint  default 0                 not null comment '是否删除',
    constraint fk_notice_creator foreign key (noticeCreatorId)
        references user (userId)
        on update cascade
        on delete set null
) comment '公告表';

-- 创建索引
create index idx_expire on notice (noticeExpireTime);
create index idx_is_delete on notice (noticeIsDelete);
create index idx_status_publish on notice (noticeStatus, noticePublishTime);

-- 创建触发器设置默认过期时间
DELIMITER
//
CREATE TRIGGER set_notice_expire_time
    BEFORE INSERT
    ON notice
    FOR EACH ROW
BEGIN
    IF NEW.noticeExpireTime IS NULL THEN
        SET NEW.noticeExpireTime = DATE_ADD(NOW(), INTERVAL 30 DAY);
END IF;
END
//
DELIMITER ;