# OA 办公系统案例

基于 xarch 框架构建的企业办公自动化系统。

## 功能模块

### 系统管理
- [x] 用户管理 - 员工信息管理、批量导入
- [x] 角色管理 - 部门经理、项目经理、普通员工
- [x] 部门管理 - 组织架构树形管理
- [x] 岗位管理 - 职位设置

### 办公流程
- [x] 考勤管理 - 上下班打卡、请假申请
- [x] 审批流程 - 报销、请假、加班审批
- [x] 公告通知 - 公司公告、部门通知
- [x] 会议管理 - 会议室预约、会议纪要

### 日常办公
- [x] 日程管理 - 个人日程、团队日程
- [x] 通讯录 - 员工联系方式
- [x] 文档管理 - 共享文档、部门文档
- [x] 任务管理 - 项目任务分配跟踪

## 技术架构

```
backend/
├── xarch-example/           # 基于 xarch-example 模块
│   ├── controller/
│   │   ├── oa/             # OA 相关控制器
│   │   │   ├── AttendanceController.java
│   │   │   ├── LeaveController.java
│   │   │   ├── MeetingController.java
│   │   │   └── ScheduleController.java
│   │   └── ...
│   ├── service/
│   │   └── oa/
│   └── entity/
│       └── oa/
└── ...

frontend/
├── vue3-admin/             # 基于 vue3-admin
│   └── src/views/oa/
```

## 数据库模型

### 考勤记录表 (sys_attendance)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| check_in_time | datetime | 上班打卡时间 |
| check_out_time | datetime | 下班打卡时间 |
| status | varchar | 正常/迟到/早退 |
| work_date | date | 工作日期 |

### 请假申请表 (sys_leave)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 申请人 |
| leave_type | varchar | 请假类型 |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| reason | varchar | 请假原因 |
| status | int | 审批状态 |
| approver_id | bigint | 审批人 |

### 会议表 (sys_meeting)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar | 会议主题 |
| room_id | bigint | 会议室ID |
| host_id | bigint | 主持人 |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| participants | varchar | 参与人 |
| content | text | 会议内容 |

## API 示例

### 考勤打卡

```bash
# 上班打卡
POST /api/oa/attendance/check-in
{
  "latitude": 31.2304,
  "longitude": 121.4737,
  "location": "上海市浦东新区"
}

# 下班打卡
POST /api/oa/attendance/check-out

# 获取考勤记录
GET /api/oa/attendance/page?startTime=2024-01-01&endTime=2024-01-31
```

### 请假申请

```bash
# 提交请假
POST /api/oa/leave
{
  "leaveType": "annual",
  "startTime": "2024-01-15 09:00:00",
  "endTime": "2024-01-17 18:00:00",
  "reason": "家人旅游"
}

# 审批请假
PUT /api/oa/leave/1/approve
{
  "comment": "同意"
}

# 查询请假记录
GET /api/oa/leave/page?status=0
```

### 会议管理

```bash
# 创建会议
POST /api/oa/meeting
{
  "title": "Q1季度总结会议",
  "roomId": 1,
  "startTime": "2024-01-20 14:00:00",
  "endTime": "2024-01-20 16:00:00",
  "participants": "1,2,3,4,5",
  "content": "讨论Q1季度工作成果"
}

# 查询会议室可用时间
GET /api/oa/meeting/rooms/1/available?date=2024-01-20
```

## 界面预览

```
┌─────────────────────────────────────────────────────────┐
│  OA办公系统                                             │
├──────────┬──────────────────────────────────────────────┤
│ 系统管理  │ ┌─────────────────────────────────────────┐ │
│  ├用户   │ │  考勤记录          2024年01月           │ │
│  ├部门   │ ├─────────────────────────────────────────┤ │
│  └岗位   │ │  01/15 周一  ✓ 上班 09:00 下班 18:05     │ │
│          │ │  01/16 周二  ✓ 上班 08:58 下班 18:02     │ │
│ 办公流程  │ │  01/17 周三  ⚠ 上班 09:15 下班 17:50    │ │
│  ├考勤   │ │  01/18 周四  ✓ 上班 08:55 下班 18:10     │ │
│  ├请假   │ │  01/19 周五  ✓ 上班 09:00 下班 18:00     │ │
│  └会议   │ └─────────────────────────────────────────┘ │
│          │                                              │
│ 日常工作  │ ┌─────────────────────────────────────────┐ │
│  ├日程   │ │  请假申请                               │ │
│  └任务   │ │  [新建]  [审批]  [查询]                 │ │
│          │ ├─────────────────────────────────────────┤ │
│          │ │  申请人    类型    时间         状态     │ │
│          │ │  张三    年假   01/15-01/17  [待审批]   │ │
│          │ │  李四    病假   01/18-01/18  [已批准]   │ │
│          │ └─────────────────────────────────────────┘ │
└──────────┴────────────────────────────────────────────┘
```

## 扩展 xarch

如需在 xarch 基础上扩展 OA 功能，只需：

1. 创建实体类继承 BaseEntity
2. 创建 Controller 继承 BaseController
3. 创建 Service 实现业务逻辑
4. 配置路由和菜单

```java
@RestController
@RequestMapping("/api/oa/leave")
public class LeaveController extends BaseController<Leave> {

    @Autowired
    private LeaveService leaveService;

    // 请假申请逻辑
    @PostMapping
    @XarchLog(value = "申请请假", type = "CREATE")
    public ApiResult<Void> apply(@RequestBody Leave leave) {
        leaveService.applyLeave(leave);
        return ApiResult.ok();
    }

    // 审批流程
    @PutMapping("/{id}/approve")
    @XarchLog(value = "审批请假", type = "UPDATE")
    public ApiResult<Void> approve(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        leaveService.approve(id, request);
        return ApiResult.ok();
    }
}
```