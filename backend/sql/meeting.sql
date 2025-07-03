create table meeting
(
    id         bigint auto_increment
        primary key,
    content    varchar(255) null,
    end_time   datetime(6)  null,
    name       varchar(255) null,
    organizer  varchar(255) null,
    start_time datetime(6)  null,
    status     varchar(255) null,
    category   varchar(100) null
)
    collate = utf8mb4_unicode_ci;

