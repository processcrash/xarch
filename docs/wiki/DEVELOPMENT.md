# 开发规范

## 代码规范

### 包命名

```
com.xarch.starter.*      # 框架 Starter 模块
com.xarch.cloud.*        # Spring Cloud 模块
com.xarch.mcp.*          # MCP Server 模块
com.xarch.example.*      # 业务应用模块
```

### 分层架构

```
controller/    # REST 控制器层
service/       # 业务服务层（接口 + 实现）
mapper/        # 数据访问层（MyBatis）
entity/        # 领域实体层
dto/           # 数据传输对象
vo/            # 视图对象
```

---

## 命名约定

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `XxxController` | `UserController` |
| Service 接口 | `IXxxService` | `IUserService` |
| Service 实现 | `XxxServiceImpl` | `UserServiceImpl` |
| Mapper | `XxxMapper` | `UserMapper` |
| Entity | `Xxx` | `User` |
| REST API | `/xxx` | `/user`, `/role` |
| 表名 | `sys_xxx` | `sys_user`, `sys_role` |
| 变量 | camelCase | `userName`, `createTime` |

---

## Controller 规范

```java
@RestController
@RequestMapping("/system/user")
public class UserController {

    @GetMapping("/page")
    public ApiResult<PageResult<UserVO>> page(UserQuery query) {
        // 分页查询
    }

    @GetMapping("/{id}")
    public ApiResult<UserVO> get(@PathVariable Long id) {
        // 单条查询
    }

    @PostMapping
    public ApiResult<Void> create(@RequestBody @Valid UserDTO dto) {
        // 创建
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody @Valid UserDTO dto) {
        // 更新
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        // 删除
    }
}
```

---

## Service 规范

```java
public interface IUserService {
    PageResult<UserVO> page(UserQuery query);
    UserVO getById(Long id);
    void create(UserDTO dto);
    void update(UserDTO dto);
    void delete(Long id);
}

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;

    @Override
    public PageResult<UserVO> page(UserQuery query) {
        // 实现
    }
}
```

---

## Mapper 规范

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    Page<UserVO> selectPage(PageParam<UserQuery> query);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectById(Long id);
}
```

---

## Entity 规范

```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer status;
}
```

---

## DTO/VO 规范

```java
// DTO - 数据传输对象
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String email;
    private String phone;
}

// VO - 视图对象
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String status;
    private String createTime;
}
```

---

## API 设计规范

### 请求格式

- `GET` - 查询，参数通过 Query String
- `POST` - 创建，参数通过 JSON Body
- `PUT` - 更新，参数通过 JSON Body
- `DELETE` - 删除，参数通过 Path Variable

### 响应格式

```java
// 成功
ApiResult.ok(data);
ApiResult.fail("错误信息");

// 分页响应
ApiResult.ok(PageResult.of(list, total));
```

### 状态码

| 场景 | 响应码 |
|------|--------|
| 成功 | `0000` |
| 参数错误 | `1001` |
| 业务异常 | `1002` |
| 认证失败 | `1003` |
| 资源未找到 | `1004` |
| 系统错误 | `1005` |

---

## Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式（不影响功能）
refactor: 重构
test: 测试相关
chore: 构建/工具
```

示例：
```
feat: 添加用户管理模块
fix: 修复登录超时问题
docs: 更新 API 文档
```

---

## 测试规范

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void page() throws Exception {
        mockMvc.perform(get("/system/user/page"))
            .andExpect(status().isOk());
    }
}
```

---

## 代码审查清单

- [ ] 命名是否符合规范
- [ ] 是否有必要的注释
- [ ] 是否有单元测试
- [ ] API 是否符合 REST 风格
- [ ] 是否有参数校验
- [ ] 异常处理是否完善
- [ ] 日志是否适当

---

## 扩展阅读

- [安装指南](INSTALL.md)
- [架构设计](ARCHITECTURE.md)
- [API 参考](API.md)