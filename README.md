# 员工考勤管理系统

一个基于 Vue 3 + Spring Boot 的企业员工考勤管理系统，支持员工管理、人脸识别签到、考勤记录查询、请假申请与审批、统计仪表盘、图片上传和考勤数据导出。

## 功能特性

- 登录认证：基于 JWT 的登录认证与接口访问控制。
- 角色权限：支持管理员、部门经理、普通员工等角色，不同角色拥有不同菜单和操作权限。
- 员工管理：支持员工新增、编辑、删除、分页查询、关键词筛选、部门筛选、状态筛选和批量状态变更。
- 人脸签到：调用摄像头拍照，后端使用 OpenCV 进行人脸检测和相似度比对，通过后记录签到或签退。
- 考勤记录：支持按部门、状态、日期范围筛选，普通员工只能查看自己的记录，管理员可查看全部记录。
- 请假流程：员工提交请假申请，管理员或部门经理进行审批，形成完整业务闭环。
- 数据统计：仪表盘展示今日签到、迟到、待审请假、部门出勤率和近 7 天出勤趋势。
- 文件能力：支持员工人脸照片上传，支持考勤记录 CSV 导出。
- 友好提示：前端表单校验、后端参数校验、异常统一响应、无权限访问提示。

## 技术栈

### 前端

- Vue 3
- Vue Router
- Pinia
- Element Plus
- Axios
- Vite

### 后端

- Java 17
- Spring Boot 2.7.18
- Spring Security
- Spring Data JPA
- JWT
- PostgreSQL
- OpenCV / JavaCV

## 项目结构

```text
attendance-system/
├─ backend/                         # Spring Boot 后端
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/attendance/
│     │  ├─ config/                 # 安全配置、MVC 配置、全局异常处理
│     │  ├─ controller/             # REST API 控制器
│     │  ├─ dto/                    # 请求与响应 DTO
│     │  ├─ entity/                 # JPA 实体
│     │  ├─ repository/             # 数据访问层
│     │  ├─ security/               # JWT 认证过滤器
│     │  ├─ service/                # 业务逻辑层
│     │  └─ util/                   # 工具类
│     └─ resources/
│        ├─ application.yml
│        └─ opencv/                 # OpenCV 人脸检测模型
├─ frontend/                        # Vue 前端
│  ├─ package.json
│  ├─ vite.config.js
│  └─ src/
│     ├─ api/                       # Axios 与接口封装
│     ├─ layout/                    # 主布局
│     ├─ router/                    # 路由
│     ├─ store/                     # Pinia 状态
│     ├─ styles/                    # 全局样式
│     └─ views/                     # 页面组件
├─ docs/
│  ├─ init.sql                      # 数据库建表与测试数据脚本
│  └─ fix_employees.sql
├─ uploads/                         # 上传文件目录
└─ README.md
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 12+

## 快速启动

### 1. 初始化数据库

创建 PostgreSQL 数据库：

```sql
CREATE DATABASE attendance_db;
```

执行初始化脚本：

```bash
psql -U postgres -d attendance_db -f docs/init.sql
```

如果你的 PostgreSQL 端口、用户名或密码不同，请同步修改：

```text
backend/src/main/resources/application.yml
```

当前默认配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:1234/attendance_db
    username: postgres
    password: "3119043808"
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在：

```text
http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

如果 Windows PowerShell 阻止执行 `npm.ps1`，可以使用：

```bash
npm.cmd run dev
```

前端默认运行在：

```text
http://localhost:3000
```

## 默认账号

```text
管理员账号：admin
默认密码：123456
```

普通员工账号可由管理员在“员工管理”中新增员工时创建。

## 主要页面

- 登录页：用户登录和 Token 获取。
- 仪表盘：查看考勤概览、部门出勤率、近 7 天趋势。
- 人脸签到：摄像头拍照、人脸识别签到、签退。
- 考勤记录：分页查询、条件筛选、CSV 导出。
- 请假管理：员工提交申请，管理员审批。
- 员工管理：员工档案维护、人脸照片上传、批量状态变更。

## 核心接口

### 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/login` | 用户登录 |
| GET | `/api/user/info` | 获取当前用户信息 |

### 员工

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/employees` | 员工分页查询 |
| POST | `/api/employees` | 新增员工 |
| PUT | `/api/employees/{id}` | 修改员工 |
| DELETE | `/api/employees/{id}` | 删除员工 |
| PUT | `/api/employees/batch-status` | 批量变更员工状态 |

### 考勤

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/attendance/face-check-in` | 人脸识别签到/签退 |
| POST | `/api/attendance/records` | 查询考勤记录 |
| GET | `/api/attendance/today-status` | 查询今日打卡状态 |
| GET | `/api/attendance/statistics` | 获取统计数据 |
| GET | `/api/attendance/export` | 导出考勤 CSV |

### 请假

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/leave/submit` | 提交请假申请 |
| POST | `/api/leave/approve` | 审批请假申请 |
| GET | `/api/leave/list` | 查询请假列表 |
| GET | `/api/leave/pending` | 查询待审批申请 |

### 文件

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/upload/image` | 上传图片 |
| POST | `/api/upload/face` | 上传人脸照片 |
| POST | `/api/upload/avatar` | 上传头像 |

## 人脸识别说明

系统使用 OpenCV 的 Haar Cascade 模型进行人脸检测。签到时，前端通过浏览器摄像头获取照片，将图片转为 Base64 后提交给后端。后端检测当前照片与员工档案中的人脸照片，计算相似度，达到阈值后写入考勤记录。

人脸识别结果会受到光照、角度、遮挡、摄像头清晰度等因素影响。建议上传清晰正面照，并在签到时保持面部正对摄像头。

## 权限说明

| 角色 | 菜单与权限 |
| --- | --- |
| 系统管理员 | 仪表盘、员工管理、考勤统计、考勤记录、请假审批、文件上传、数据导出 |
| 部门经理 | 查看考勤与请假数据，处理请假审批 |
| 普通员工 | 人脸签到、查看本人考勤记录、提交请假申请 |

后端通过 Spring Security、JWT 和 `@PreAuthorize` 进行接口级权限控制；前端根据当前用户角色控制菜单和按钮显示。

## 数据库表

主要数据表包括：

- `users`：系统用户
- `roles`：角色
- `employees`：员工档案
- `departments`：部门
- `attendance_records`：考勤记录
- `leave_requests`：请假申请
- `menus`：菜单权限
- `role_menus`：角色菜单关联
- `operation_logs`：操作日志

## 构建

前端生产构建：

```bash
cd frontend
npm run build
```

后端编译测试：

```bash
cd backend
mvn test
```

## 常见问题

### 前端无法访问后端接口

确认后端已启动在 `8080` 端口，并检查 `frontend/vite.config.js` 中代理配置：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

### 后端启动失败

优先检查 PostgreSQL 是否启动、数据库是否创建、`application.yml` 中数据库端口和密码是否正确。

### 摄像头无法打开

请确认浏览器允许当前站点访问摄像头。建议使用 Chrome、Edge 等现代浏览器，并通过 `http://localhost:3000` 或 `http://127.0.0.1:3000` 访问。

## License

仅用于课程设计、学习和二次开发参考。
