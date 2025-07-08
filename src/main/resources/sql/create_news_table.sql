-- 创建资讯表 (news)
create table news (
    newsId bigint auto_increment comment '资讯ID' primary key,
    newsTitle varchar(255) not null comment '资讯标题',
    newsContent text not null comment '资讯内容',
    newsAuthorId bigint not null comment '作者ID (关联用户表)',
    newsStatus int default 0 null comment '资讯状态（0-草稿，1-已发布）',
    newsCreateTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    newsUpdateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    newsPublishTime datetime null comment '发布时间',
    newsIsDelete int default 0 null comment '是否删除 (0-未删除, 1-已删除)',
    newsCoverImage varchar(1024) null comment '资讯封面图 URL',
    newsSummary varchar(500) null comment '资讯摘要',
    newsViews int default 0 null comment '浏览次数',
    constraint fk_news_author foreign key (newsAuthorId) references game9.user (userId)
) comment '资讯表';
create index idx_news_author_id on news (newsAuthorId);
create index idx_news_publish_time on news (newsPublishTime);
create index idx_news_status on news (newsStatus);