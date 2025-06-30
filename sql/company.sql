create table company
(
    id      bigint auto_increment comment '企业ID'
        primary key,
    name    varchar(100) not null comment '企业名称',
    contact varchar(50)  not null comment '联系方式',
    license varchar(100) not null comment '营业执照',
    constraint uniq_name
        unique (name)
)
    comment '企业表' row_format = DYNAMIC;

