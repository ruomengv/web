create table news
(
    id            bigint auto_increment comment '动态ID'
        primary key,
    title         varchar(100)      not null comment '标题',
    cover         varchar(200)      null comment '封面图',
    summary       text              null comment '摘要',
    content       text              null comment '内容',
    author        varchar(50)       null comment '作者',
    status        tinyint default 0 null comment '状态(0-待审核 1-已发布 2-驳回)',
    reject_reason varchar(200)      null comment '驳回原因',
    creator_id    bigint            not null comment '创建者ID',
    create_time   datetime          not null comment '创建时间',
    update_time   datetime          null comment '更新时间',
    constraint fk_news_user
        foreign key (creator_id) references user (uid)
)
    comment '动态表' row_format = DYNAMIC;

create fulltext index ft_content
    on news (title, summary, content);

create index idx_creator
    on news (creator_id);

create index idx_status
    on news (status);

