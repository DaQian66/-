-- ============================================
-- 员工考勤管理系统 - PostgreSQL 数据库初始化
-- 端口: 1234, 密码: 3119043808
-- ============================================


-- CREATE DATABASE attendance_db;

-- 连接数据库后执行以下内容
-- \c attendance_db

-- ============================================
-- 1. 部门表 departments
-- ============================================
CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INTEGER,
    manager_id INTEGER,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE departments IS '部门表';
COMMENT ON COLUMN departments.name IS '部门名称';
COMMENT ON COLUMN departments.parent_id IS '父部门ID';
COMMENT ON COLUMN departments.manager_id IS '部门负责人ID';

-- ============================================
-- 2. 角色表 roles
-- ============================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE roles IS '角色表';
COMMENT ON COLUMN roles.code IS '角色编码(ROLE_ADMIN/ROLE_MANAGER/ROLE_EMPLOYEE)';

-- ============================================
-- 3. 用户表 users
-- ============================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    department_id INTEGER REFERENCES departments(id),
    status SMALLINT DEFAULT 1,
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.status IS '1-正常 0-禁用';
COMMENT ON COLUMN users.role_id IS '角色ID';

-- ============================================
-- 4. 员工表 employees
-- ============================================
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    employee_no VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(50) NOT NULL,
    gender VARCHAR(4),
    birthday DATE,
    id_card VARCHAR(18),
    phone VARCHAR(20),
    email VARCHAR(100),
    department_id INTEGER REFERENCES departments(id),
    position VARCHAR(50),
    entry_date DATE,
    status SMALLINT DEFAULT 1,
    face_image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE employees IS '员工档案表';
COMMENT ON COLUMN employees.status IS '1-在职 0-离职';

-- ============================================
-- 5. 考勤记录表 attendance_records
-- ============================================
CREATE TABLE attendance_records (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employees(id),
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    check_in_type VARCHAR(20) DEFAULT 'PASSWORD',
    status VARCHAR(20) DEFAULT 'NORMAL',
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE attendance_records IS '考勤打卡记录表';
COMMENT ON COLUMN attendance_records.check_in_type IS 'PASSWORD-密码打卡 FACE-人脸打卡';
COMMENT ON COLUMN attendance_records.status IS 'NORMAL-正常 LATE-迟到 EARLY-早退 ABSENT-缺勤';

-- ============================================
-- 6. 请假申请表 leave_requests
-- ============================================
CREATE TABLE leave_requests (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time VARCHAR(10),
    end_time VARCHAR(10),
    duration FLOAT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    attachment VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    approver_id INTEGER REFERENCES users(id),
    approve_comment VARCHAR(500),
    approve_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE leave_requests IS '请假申请表';
COMMENT ON COLUMN leave_requests.leave_type IS 'SICK-病假 PERSONAL-事假 ANNUAL-年假 MARRIAGE-婚假 MATERNITY-产假 OTHER-其他';
COMMENT ON COLUMN leave_requests.status IS 'PENDING-待审批 APPROVED-已批准 REJECTED-已拒绝';

-- ============================================
-- 7. 菜单权限表 menus
-- ============================================
CREATE TABLE menus (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(50),
    type VARCHAR(10) DEFAULT 'menu',
    sort_order INTEGER DEFAULT 0,
    permission VARCHAR(100),
    visible SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE menus IS '菜单权限表';
COMMENT ON COLUMN menus.type IS 'menu-菜单 button-按钮';
COMMENT ON COLUMN menus.permission IS '权限标识';

-- ============================================
-- 8. 角色菜单关联表 role_menus
-- ============================================
CREATE TABLE role_menus (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id),
    menu_id INTEGER NOT NULL REFERENCES menus(id),
    UNIQUE(role_id, menu_id)
);

COMMENT ON TABLE role_menus IS '角色菜单关联表';

-- ============================================
-- 9. 操作日志表 operation_logs (拔高-审计日志)
-- ============================================
CREATE TABLE operation_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(10),
    params TEXT,
    ip VARCHAR(50),
    duration INTEGER,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE operation_logs IS '操作日志表';

-- ============================================
-- 创建索引
-- ============================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_employees_employee_no ON employees(employee_no);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_attendance_records_employee_date ON attendance_records(employee_id, attendance_date);
CREATE INDEX idx_attendance_records_date ON attendance_records(attendance_date);
CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_operation_logs_user ON operation_logs(user_id);
CREATE INDEX idx_operation_logs_time ON operation_logs(created_at);

-- ============================================
-- 序列（PostgreSQL auto-generates for SERIAL, but explicitly for batch insert）
-- ============================================
-- 通过SERIAL自动创建，无需手动创建序列

-- ============================================
-- 初始数据
-- ============================================

-- 角色数据
INSERT INTO roles (name, code, description) VALUES
('系统管理员', 'ROLE_ADMIN', '系统最高权限，管理所有功能'),
('部门经理', 'ROLE_MANAGER', '管理本部门员工，审批请假'),
('普通员工', 'ROLE_EMPLOYEE', '打卡签到，申请请假');

-- 部门数据
INSERT INTO departments (name, parent_id, description) VALUES
('总经办', NULL, '公司高层管理'),
('技术部', NULL, '软件开发与维护'),
('人事部', NULL, '人力资源管理与招聘'),
('财务部', NULL, '财务核算与审计'),
('市场部', NULL, '市场营销与推广'),
('前端开发组', 2, '前端技术开发'),
('后端开发组', 2, '后端技术开发');

-- 用户数据（密码均为 123456，BCrypt加密）
INSERT INTO users (username, password, real_name, email, phone, role_id, department_id, status) VALUES
('admin', '$2a$10$PjD1DwoPx89jN0oyfCziwO7ykRsS9kLeD9sh/HAgSTfc77SDouwBS', '系统管理员', 'admin@company.com', '13800000001', 1, 1, 1),
('zhangsan', '$2a$10$PjD1DwoPx89jN0oyfCziwO7ykRsS9kLeD9sh/HAgSTfc77SDouwBS', '张三', 'zhangsan@company.com', '13800000002', 2, 2, 1),
('lisi', '$2a$10$PjD1DwoPx89jN0oyfCziwO7ykRsS9kLeD9sh/HAgSTfc77SDouwBS', '李四', 'lisi@company.com', '13800000003', 3, 6, 1),
('wangwu', '$2a$10$PjD1DwoPx89jN0oyfCziwO7ykRsS9kLeD9sh/HAgSTfc77SDouwBS', '王五', 'wangwu@company.com', '13800000004', 3, 7, 1),
('zhaoliu', '$2a$10$PjD1DwoPx89jN0oyfCziwO7ykRsS9kLeD9sh/HAgSTfc77SDouwBS', '赵六', 'zhaoliu@company.com', '13800000005', 2, 3, 1);

-- 员工数据
INSERT INTO employees (user_id, employee_no, real_name, gender, birthday, phone, email, department_id, position, entry_date, status) VALUES
(1, 'EMP001', '系统管理员', '男', '1990-01-01', '13800000001', 'admin@company.com', 1, '系统管理员', '2020-01-01', 1),
(2, 'EMP002', '张三', '男', '1988-05-15', '13800000002', 'zhangsan@company.com', 2, '技术总监', '2020-03-01', 1),
(3, 'EMP003', '李四', '男', '1995-08-20', '13800000003', 'lisi@company.com', 6, '前端工程师', '2021-06-15', 1),
(4, 'EMP004', '王五', '女', '1996-03-12', '13800000004', 'wangwu@company.com', 7, '后端工程师', '2021-07-01', 1),
(5, 'EMP005', '赵六', '女', '1992-11-08', '13800000005', 'zhaoliu@company.com', 3, '人事经理', '2020-06-01', 1),
(6, 'EMP006', '孙七', '男', '1993-02-14', '13800000006', 'sunqi@company.com', 4, '财务主管', '2020-09-01', 1),
(7, 'EMP007', '周八', '女', '1997-07-25', '13800000007', 'zhouba@company.com', 5, '市场专员', '2022-01-15', 1);

-- 菜单数据
INSERT INTO menus (id, parent_id, name, path, component, icon, type, sort_order, permission) VALUES
(1, 0, '首页仪表盘', '/dashboard', 'Dashboard', 'el-icon-s-home', 'menu', 1, 'dashboard:view'),
(2, 0, '员工管理', '/employee', 'EmployeeList', 'el-icon-user', 'menu', 2, 'employee:list'),
(3, 2, '新增员工', '', '', '', 'button', 1, 'employee:add'),
(4, 2, '编辑员工', '', '', '', 'button', 2, 'employee:edit'),
(5, 2, '删除员工', '', '', '', 'button', 3, 'employee:delete'),
(6, 0, '考勤打卡', '/attendance', 'AttendanceCheck', 'el-icon-s-check', 'menu', 3, 'attendance:check'),
(7, 0, '考勤记录', '/attendance/records', 'AttendanceRecords', 'el-icon-document', 'menu', 4, 'attendance:records'),
(8, 0, '请假管理', '/leave', 'LeaveManagement', 'el-icon-s-order', 'menu', 5, 'leave:list'),
(9, 0, '请假审批', '/leave/approval', 'LeaveApproval', 'el-icon-s-claim', 'menu', 6, 'leave:approve'),
(10, 0, '部门管理', '/department', 'DepartmentManage', 'el-icon-office-building', 'menu', 7, 'department:manage'),
(11, 0, '系统管理', '/sys', '', 'el-icon-s-tools', 'menu', 8, ''),
(12, 11, '用户管理', '/sys/users', 'UserManage', 'el-icon-s-custom', 'menu', 1, 'user:manage'),
(13, 11, '角色管理', '/sys/roles', 'RoleManage', 'el-icon-s-flag', 'menu', 2, 'role:manage'),
(14, 11, '菜单管理', '/sys/menus', 'MenuManage', 'el-icon-menu', 'menu', 3, 'menu:manage'),
(15, 11, '操作日志', '/sys/logs', 'OperationLog', 'el-icon-document-copy', 'menu', 4, 'log:view'),
(16, 0, '考勤统计', '/attendance/statistics', 'AttendanceStatistics', 'el-icon-s-data', 'menu', 9, 'statistics:view');

-- 角色菜单关联（管理员拥有所有权限）
INSERT INTO role_menus (role_id, menu_id)
SELECT 1, id FROM menus;

-- 部门经理角色菜单
INSERT INTO role_menus (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 6), (2, 7), (2, 8), (2, 9), (2, 16);

-- 普通员工角色菜单
INSERT INTO role_menus (role_id, menu_id) VALUES
(3, 1), (3, 6), (3, 7), (3, 8);

-- 测试考勤数据（近30天）
INSERT INTO attendance_records (employee_id, attendance_date, check_in_time, check_out_time, check_in_type, status)
SELECT
    e.id,
    CURRENT_DATE - (n || ' days')::INTERVAL,
    '08:' || LPAD((FLOOR(RANDOM() * 30) + 30)::TEXT, 2, '0') || ':' || LPAD((FLOOR(RANDOM() * 60))::TEXT, 2, '0'),
    '17:' || LPAD((FLOOR(RANDOM() * 60) + 30)::TEXT, 2, '0') || ':' || LPAD((FLOOR(RANDOM() * 60))::TEXT, 2, '0'),
    CASE WHEN RANDOM() > 0.3 THEN 'PASSWORD' ELSE 'FACE' END,
    CASE
        WHEN RANDOM() < 0.05 THEN 'LATE'
        WHEN RANDOM() < 0.02 THEN 'EARLY'
        WHEN RANDOM() < 0.01 THEN 'ABSENT'
        ELSE 'NORMAL'
    END
FROM employees e
CROSS JOIN generate_series(0, 29) n
WHERE e.status = 1 AND RANDOM() > 0.1;

-- 测试请假数据
INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, duration, reason, status, approver_id, approve_comment, approve_time) VALUES
(3, 'SICK', CURRENT_DATE - 5, CURRENT_DATE - 4, 2, '感冒发烧，需要休息', 'APPROVED', 2, '同意，注意身体', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(4, 'PERSONAL', CURRENT_DATE - 3, CURRENT_DATE - 3, 1, '家里有急事需要处理', 'APPROVED', 2, '同意', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(5, 'ANNUAL', CURRENT_DATE + 5, CURRENT_DATE + 7, 3, '年假休假', 'PENDING', NULL, NULL, NULL),
(6, 'SICK', CURRENT_DATE - 1, CURRENT_DATE, 2, '身体不适就医', 'PENDING', NULL, NULL, NULL);

-- 操作日志示例
INSERT INTO operation_logs (user_id, username, operation, method, params, ip, duration, status) VALUES
(1, 'admin', '用户登录', 'POST', '{"username":"admin"}', '127.0.0.1', 120, 1),
(2, 'zhangsan', '查看考勤记录', 'GET', '{"page":1,"size":10}', '127.0.0.1', 45, 1),
(1, 'admin', '新增员工', 'POST', '{"employeeNo":"EMP008","realName":"新员工"}', '127.0.0.1', 89, 1);