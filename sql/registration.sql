CREATE TABLE meeting_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    unit VARCHAR(100) NOT NULL COMMENT '单位',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    email VARCHAR(50) COMMENT '邮箱',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);