create table course
(
    id            bigint auto_increment comment '课程ID'
        primary key,
    title         varchar(100)      not null comment '标题',
    cover         varchar(200)      null comment '封面图',
    summary       text              null comment '摘要',
    sort          int     default 0 null comment '排序',
    video_url     varchar(200)      null comment '视频地址',
    author        varchar(50)       null comment '作者',
    status        tinyint default 0 null comment '状态(0-待审核 1-已发布 2-驳回)',
    reject_reason varchar(200)      null comment '驳回原因',
    creator_id    bigint            not null comment '创建者ID',
    create_time   datetime          not null comment '创建时间',
    update_time   datetime          null comment '更新时间',
    constraint fk_course_user
        foreign key (creator_id) references user (uid)
)
    comment '课程表' row_format = DYNAMIC;

create fulltext index ft_title
    on course (title);

create index idx_creator
    on course (creator_id);

create index idx_status
    on course (status);

