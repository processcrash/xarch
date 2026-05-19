# CRM 客户管理系统案例

基于 xarch 框架构建的客户关系管理系统。

## 功能模块

### 客户管理
- [x] 客户档案 - 基本信息、联系方式、公司信息
- [x] 客户分类 - A/B/C类客户分类管理
- [x] 客户跟进 - 跟进记录、跟进计划
- [x] 客户共享 - 团队客户共享与转移

### 销售管理
- [x] 销售机会 - 机会录入、阶段管理
- [x] 报价管理 - 产品报价、报价历史
- [x] 合同管理 - 合同签订、履行跟踪
- [x] 回款管理 - 回款计划、回款记录

### 市场营销
- [x] 营销活动 - 活动计划、执行跟踪
- [x] 潜在客户 - 线索获取、线索转换
- [x] 数据分析 - 销售漏斗、业绩报表

### 服务管理
- [x] 客户服务 - 客户反馈、问题处理
- [x] 满意度调查 - 调查问卷、结果分析
- [x] 知识库 - 常见问题、产品知识

## 技术架构

```
backend/
├── xarch-example/
│   ├── controller/
│   │   ├── crm/              # CRM 相关控制器
│   │   │   ├── CustomerController.java
│   │   │   ├── ContactController.java
│   │   │   ├── OpportunityController.java
│   │   │   ├── ContractController.java
│   │   │   └── ServiceController.java
│   │   └── ...
│   └── entity/
│       └── crm/
└── ...

frontend/
├── vue3-admin/
│   └── src/views/crm/
```

## 数据库模型

### 客户表 (crm_customer)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| customer_name | varchar | 客户名称 |
| customer_type | varchar | 客户类型 |
| industry | varchar | 所属行业 |
| level | varchar | 客户级别(A/B/C) |
| source | varchar | 客户来源 |
| website | varchar | 公司网站 |
| employee_count | int | 员工规模 |
| annual_revenue | decimal | 年营业额 |
| owner_id | bigint | 负责人 |
| created_time | datetime | 创建时间 |

### 联系人表 (crm_contact)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| customer_id | bigint | 客户ID |
| name | varchar | 姓名 |
| position | varchar | 职位 |
| phone | varchar | 电话 |
| email | varchar | 邮箱 |
| is_primary | int | 是否主联系人 |

### 销售机会表 (crm_opportunity)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| opportunity_name | varchar | 机会名称 |
| customer_id | bigint | 客户ID |
| amount | decimal | 预计金额 |
| stage | varchar | 销售阶段 |
| expected_date | date | 预计成交日期 |
| probability | int | 赢单概率 |
| status | int | 状态 |

### 合同表 (crm_contract)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| contract_no | varchar | 合同编号 |
| customer_id | bigint | 客户ID |
| opportunity_id | bigint | 机会ID |
| amount | decimal | 合同金额 |
| sign_date | date | 签订日期 |
| start_date | date | 开始日期 |
| end_date | date | 结束日期 |
| status | int | 状态 |

## API 示例

### 客户管理

```bash
# 创建客户
POST /api/crm/customer
{
  "customerName": "上海科技有限公司",
  "customerType": "enterprise",
  "industry": "IT",
  "level": "A",
  "source": "网络推广",
  "employeeCount": 100,
  "annualRevenue": 10000000,
  "ownerId": 1
}

# 查询客户列表
GET /api/crm/customer/page?level=A&industry=IT

# 客户跟进
POST /api/crm/customer/1/follow
{
  "followType": "visit",
  "content": "拜访了解需求",
  "nextTime": "2024-01-20",
  "nextContent": "提供方案"
}
```

### 销售机会

```bash
# 创建销售机会
POST /api/crm/opportunity
{
  "opportunityName": "上海科技CRM采购项目",
  "customerId": 1,
  "amount": 500000,
  "stage": "qualification",
  "expectedDate": "2024-03-01",
  "probability": 30
}

# 推进销售阶段
PUT /api/crm/opportunity/1/stage
{
  "stage": "proposal",
  "probability": 50,
  "comment": "已提交方案"
}

# 获取销售漏斗
GET /api/crm/opportunity/funnel
```

### 合同管理

```bash
# 创建合同
POST /api/crm/contract
{
  "contractNo": "HT202401001",
  "customerId": 1,
  "opportunityId": 1,
  "amount": 500000,
  "signDate": "2024-01-15",
  "startDate": "2024-01-15",
  "endDate": "2025-01-14"
}

# 回款记录
POST /api/crm/contract/1/payment
{
  "amount": 150000,
  "paymentDate": "2024-01-20",
  "paymentMethod": "bank_transfer",
  "remark": "首付款"
}
```

## 界面预览

```
┌─────────────────────────────────────────────────────────────┐
│  CRM客户管理系统                                             │
├──────────┬──────────────────────────────────────────────────┤
│ 客户管理  │ ┌──────────────────────────────────────────────┐│
│  ├客户   │ │  销售漏斗                      2024年Q1       ││
│  ├联系人 │ ├──────────────────────────────────────────────┤│
│  └跟进   │ │                                              ││
│          │ │    线索    →   方案    →   谈判    →   成单   ││
│ 销售管理  │ │    120      80       45        25            ││
│  ├机会   │ │   (30%)    (40%)    (60%)     (80%)           ││
│  ├报价   │ │                                              ││
│  ├合同   │ │  预计收入: ¥2,450,000                         ││
│  └回款   │ │  实际收入: ¥1,250,000                         ││
│          │ └──────────────────────────────────────────────┘│
│ 服务管理  │                                                │
│  ├反馈   │ ┌──────────────────────────────────────────────┐│
│  └知识库 │ │  最近跟进记录                                 ││
│          │ ├──────────────────────────────────────────────┤│
│          │ │  01/15 张三  拜访  上海科技  了解采购需求      ││
│          │ │  01/14 李四  电话  成都实业  提交技术方案     ││
│          │ │  01/13 王五  邮件  深圳网络  商务谈判中       ││
│          │ └──────────────────────────────────────────────┘│
└──────────┴────────────────────────────────────────────────┘
```

## 扩展 xarch

在 xarch 基础上扩展 CRM 功能：

```java
@RestController
@RequestMapping("/api/crm/opportunity")
public class OpportunityController extends BaseController<Opportunity> {

    @Autowired
    private OpportunityService opportunityService;

    // 创建销售机会
    @PostMapping
    @XarchLog(value = "创建销售机会", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Opportunity opportunity) {
        opportunityService.createOpportunity(opportunity);
        return ApiResult.ok();
    }

    // 推进销售阶段
    @PutMapping("/{id}/stage")
    @XarchLog(value = "推进销售阶段", type = "UPDATE")
    public ApiResult<Void> advanceStage(@PathVariable Long id, @RequestBody StageUpdateRequest request) {
        opportunityService.advanceStage(id, request);
        return ApiResult.ok();
    }

    // 获取销售漏斗数据
    @GetMapping("/funnel")
    public ApiResult<FunnelVO> getFunnel() {
        return ApiResult.ok(opportunityService.getFunnelData());
    }
}
```