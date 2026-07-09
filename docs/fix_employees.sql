-- Fix: Insert matching employee records for all users
-- Each user needs a corresponding employee to use attendance functions

INSERT INTO employees (user_id, employee_no, real_name, gender, department_id, position, entry_date, status, phone, email) VALUES
(1, 'EMP001', '系统管理员', '男', 1, '系统管理员', '2020-01-01', 1, '13800000001', 'admin@company.com'),
(2, 'EMP002', '张三', '男', 2, '技术总监', '2020-03-01', 1, '13800000002', 'zhangsan@company.com'),
(3, 'EMP003', '李四', '男', 6, '前端工程师', '2021-06-15', 1, '13800000003', 'lisi@company.com'),
(4, 'EMP004', '王五', '女', 7, '后端工程师', '2021-07-01', 1, '13800000004', 'wangwu@company.com'),
(5, 'EMP005', '赵六', '女', 3, '人事经理', '2020-06-01', 1, '13800000005', 'zhaoliu@company.com');