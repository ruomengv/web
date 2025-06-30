create table user
(
    uid         bigint auto_increment comment '用户ID'
        primary key,
    username    varchar(50)       not null comment '用户名',
    password    varchar(100)      not null comment '密码',
    nickname    varchar(50)       null comment '昵称',
    telephone   varchar(20)       null comment '电话',
    email       varchar(50)       null comment '邮箱',
    gender      tinyint default 0 null comment '性别(0-未知 1-男 2-女)',
    status      tinyint default 1 null comment '状态(0-禁用 1-启用)',
    role        tinyint           not null comment '角色(0-超级管理员 1-企业用户)',
    company_id  bigint            null comment '企业ID',
    create_time datetime          not null comment '创建时间',
    update_time datetime          null comment '更新时间',
    constraint uniq_username
        unique (username),
    constraint fk_user_company
        foreign key (company_id) references company (id)
)
    comment '用户表' row_format = DYNAMIC;

create index idx_company
    on user (company_id);

