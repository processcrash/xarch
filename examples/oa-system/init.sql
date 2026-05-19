-- OA 办公系统数据库初始化脚本
-- 基于 xarch 框架的 sys_* 表结构扩展

-- 考勤记录表
CREATE TABLE IF NOT EXISTS sys_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_in_time DATETIME COMMENT '上班打卡时间',
    check_out_time DATETIME COMMENT '下班打卡时间',
    check_in_location VARCHAR(255) COMMENT '上班打卡位置',
    check_out_location VARCHAR(255) COMMENT '下班打卡位置',
    status VARCHAR(20) DEFAULT 'normal' COMMENT 'normal-正常/late-迟到/early-早退',
    work_date DATE NOT NULL COMMENT '工作日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_user_date (user_id, work_date),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS sys_leave (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type VARCHAR(20) NOT NULL COMMENT 'annual-年假/sick-病假/personal-事假',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,1) DEFAULT 0 COMMENT '时长(天)',
    reason VARCHAR(500) COMMENT '请假原因',
    status INT DEFAULT 0 COMMENT '0-待审批/1-已批准/2-已拒绝/3-已取消',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_comment VARCHAR(255) COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_work_date (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 会议表
CREATE TABLE IF NOT EXISTS sys_meeting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '会议主题',
    room_id BIGINT COMMENT '会议室ID',
    host_id BIGINT NOT NULL COMMENT '主持人ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    participants VARCHAR(500) COMMENT '参与人ID列表，用逗号分隔',
    content TEXT COMMENT '会议内容/议程',
    status INT DEFAULT 1 COMMENT '1-待召开/2-进行中/3-已结束/4-已取消',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_room_time (room_id, start_time, end_time),
    INDEX idx_host (host_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议表';

-- 会议室表
CREATE TABLE IF NOT EXISTS sys_meeting_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    room_code VARCHAR(50) COMMENT '会议室编码',
    location VARCHAR(200) COMMENT '位置',
    capacity INT COMMENT '容纳人数',
    facilities VARCHAR(255) COMMENT '设施(投影仪/白板等)',
    status INT DEFAULT 1 COMMENT '1-可用/0-不可用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- 日程表
CREATE TABLE IF NOT EXISTS sys_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '日程标题',
    content VARCHAR(500) COMMENT '日程内容',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    is_all_day INT DEFAULT 0 COMMENT '是否全天',
    reminder INT DEFAULT 0 COMMENT '提醒时间(分钟),0-不提醒',
    color VARCHAR(20) COMMENT '日历颜色',
    repeat_type VARCHAR(20) COMMENT 'none-不重复/daily-每天/weekly-每周/monthly-每月',
    status INT DEFAULT 1 COMMENT '1-正常/0-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_user_time (user_id, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程表';

-- 插入测试数据
INSERT INTO sys_meeting_room (room_name, room_code, location, capacity, facilities, status) VALUES
('一号会议室', 'M001', '总部楼3层301', 20, '投影仪,白板,电视', 1),
('二号会议室', 'M002', '总部楼3层302', 10, '投影仪,白板', 1),
('视频会议室', 'M003', '总部楼4层401', 15, '视频会议设备,投影仪', 1);

INSERT INTO sys_attendance (user_id, check_in_time, check_out_time, status, work_date) VALUES
(1, '2024-01-15 09:00:00', '2024-01-15 18:00:00', 'normal', '2024-01-15'),
(1, '2024-01-16 08:55:00', '2024-01-16 18:05:00', 'normal', '2024-01-16'),
(1, '2024-01-17 09:15:00', '2024-01-17 17:50:00', 'late', '2024-01-17');

INSERT INTO sys_leave (user_id, leave_type, start_time, end_time, duration, reason, status) VALUES
(1, 'annual', '2024-01-20 09:00:00', '2024-01-21 18:00:00', 2, '家庭旅游', 0),
(2, 'sick', '2024-01-18 09:00:00', '2024-01-18 18:00:00', 1, '感冒发烧', 1);