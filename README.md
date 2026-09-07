# 牧曰（muyue）后台管理脚手架

一套类似若依的后台管理脚手架，包含用户 / 角色 / 权限 / 登录 / 验证码 / 动态菜单等基础能力。

| 项目 | 技术栈 | 目录 |
| --- | --- | --- |
| 后端 | JDK 21 + Spring Boot 3.3 + MyBatis-Plus 3.5 + Spring Security + JWT + **Oracle / SQLite 可切换** | `muyue-admin` |
| 前端 | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router 4 + Axios | `muyue-ui` |

## 一、功能清单

- 登录（密码 BCrypt 校验、JWT 无状态令牌）
- 图形验证码（算术表达式，本地缓存校验，可关闭）
- 按登录人角色动态拉取菜单（`/getRouters`），前端按需加载组件
- 用户管理：分页查询、新增 / 修改 / 删除、停用启用、重置密码、分配角色
- 角色管理：分页查询、新增 / 修改 / 删除、停用启用、菜单权限分配
- 菜单管理：目录 / 菜单 / 按钮三级权限、树表维护
- 部门管理：树形结构、新增 / 修改 / 删除
- **岗位管理**：分页查询、新增 / 修改 / 删除、停用启用
- **字典管理**：字典类型 + 字典数据两级维护，支持下拉 / 回显，内置性别、状态等演示数据
- **登录日志**：`sys_logininfor` 记录每次登录的账号、IP、状态、描述；登录成功 / 失败自动落库
- **个人中心**：基本资料维护、修改密码、**头像上传**（`/profile/**` 静态资源访问）
- **操作记录**：`sys_oper_log` 记录登录与增删改操作，普通用户只看自己的，管理员（拥有 `monitor:operlog:list`）可看全部，支持删除 / 清空
- 权限控制：接口级 `@PreAuthorize("@ps.hasPermi('xxx')")`，按钮级 `v-hasPermi` 指令
- **界面**：现代化卡片化设计、可切换浅色 / 暗色模式、玻璃拟态登录页、渐变欢迎横幅与统计卡片

## 二、目录结构

```
muyue
├── muyue-admin                 # 后端
│   ├── sql/oracle             # Oracle 建表与初始化数据脚本
│   └── src/main/java/com/muyue
│       ├── common             # 公共模块（统一返回、异常、JWT、Security、MP 配置、工具）
│       ├── system             # 系统业务（mapper / service）
│       └── web                # 控制器、登录服务、初始化
└── muyue-ui                    # 前端
    └── src
        ├── api                # 接口
        ├── layout             # 布局（侧边栏 / 导航 / 主区域）
        ├── router             # 静态路由 + 动态路由
        ├── store              # Pinia（user / permission / app）
        ├── directive          # v-hasPermi、v-hasRole
        ├── utils              # request / auth / tree
        └── views              # 页面
```

## 三、快速开始

### 1. 准备数据库（二选一）

#### 方案 A：SQLite 内嵌数据库（默认，零配置）

无需安装任何数据库服务。启动后端时会自动创建 `muyue-admin/data` 目录（SQLite 不会自建目录，启动类中已处理），并在 `data/muyue.db` 建库建表、写入初始化数据（脚本：`src/main/resources/db/sqlite/schema.sql`、`data.sql`，幂等可重复执行）。

- 重置数据：删除 `muyue-admin/data` 目录后重启即可
- 切换开关：`src/main/resources/application.yml` 中 `spring.profiles.active: sqlite`

#### 方案 B：Oracle

```sql
-- 1) 创建用户（示例）
CREATE USER muyue IDENTIFIED BY muyue;
GRANT CONNECT, RESOURCE, DBA TO muyue;
```

按顺序执行脚本：

1. `muyue-admin/sql/oracle/01_schema.sql`（建表）
2. `muyue-admin/sql/oracle/02_data.sql`（菜单 / 角色 / 部门 / 用户）
3. `muyue-admin/sql/oracle/03_operlog.sql`（操作日志表 + 操作记录菜单与授权，可重复执行）
4. `muyue-admin/sql/oracle/04_module.sql`（岗位 / 字典 / 登录日志表 + 菜单与授权，可重复执行）

修改 `muyue-admin/src/main/resources/application-oracle.yml` 中的连接信息，然后以 oracle profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
# 或：java -jar target/muyue-admin.jar --spring.profiles.active=oracle
```

> 表若已存在请先删除后再执行；主键使用雪花 ID（MyBatis-Plus `ASSIGN_ID`），无需建序列。

默认账号：`admin / admin123`。脚本中为明文密码，服务首次启动时会自动转换为 BCrypt 密文（见 `DataInitializer`）。

### 2. 启动后端（默认 SQLite）

```bash
cd muyue-admin
mvn spring-boot:run          # 或 mvn -DskipTests package && java -jar target/muyue-admin.jar
```

启动后监听 `http://localhost:8080`，控制台出现 `已初始化 admin 用户密码` 即表示数据就绪。

### 3. 启动前端

```bash
cd muyue-ui
npm install
npm run dev
```

访问 `http://localhost:1024`，接口通过 Vite 代理转发到后端（前缀 `/dev-api`，可在 `.env.development` 中修改）。

## 四、核心接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| GET | `/captchaImage` | 获取验证码（返回开关、key、base64 图片） |
| POST | `/login` | 登录，返回 token |
| GET | `/getInfo` | 当前登录人信息、角色、权限 |
| GET | `/getRouters` | 按角色返回动态路由 |
| POST | `/logout` | 退出 |
| GET/POST/PUT/DELETE | `/system/user/**` | 用户管理 |
| GET/POST/PUT/DELETE | `/system/role/**` | 角色管理 |
| GET/POST/PUT/DELETE | `/system/menu/**` | 菜单管理 |
| GET/POST/PUT/DELETE | `/system/dept/**` | 部门管理 |
| GET/POST/PUT/DELETE | `/system/post/**` | 岗位管理 |
| GET/POST/PUT/DELETE | `/system/dict/type/**` + `/system/dict/data/**` | 字典管理（类型 + 数据） |
| GET/DELETE | `/monitor/logininfor/**` | 登录日志（列表 / 删除 / 清空） |
| GET/PUT | `/system/user/profile` | 个人资料查询 / 修改 |
| PUT | `/system/user/profile/updatePwd` | 修改密码 |
| POST | `/system/user/profile/avatar` | 头像上传（multipart，字段 `avatarfile`） |
| POST | `/common/upload` | 通用文件上传 |
| GET/DELETE | `/monitor/operlog/**` | 操作日志（默认只查自己，`monitor:operlog:list` 可查全部） |

统一响应格式：

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

分页响应：`{ "code": 200, "msg": "查询成功", "rows": [], "total": 0 }`

## 五、常用配置

| 配置 | 说明 |
| --- | --- |
| `spring.profiles.active` | `sqlite`（默认，内嵌库）/ `oracle` |
| `muyue.db-type` | 数据库方言：`sqlite` / `oracle`，影响分页语法与 `LIMIT` / `ROWNUM` 拼接 |
| `spring.datasource.url` | SQLite 为 `jdbc:sqlite:./data/muyue.db`，Oracle 见 `application-oracle.yml` |
| `muyue.jwt.secret` / `expire-time` | JWT 密钥与有效期（分钟），生产必须修改密钥 |
| `muyue.captcha.enabled` | 是否开启验证码（`math` 算术 / `char` 字符） |
| `muyue.security.anonymous` | 匿名访问地址 |
| `muyue.user.default-password` | 新增/重置用户时的默认密码 |
| `muyue.file.profile` | 上传文件本地存储根目录（默认 `D:/muyue/uploadPath`，会自动创建） |
| `muyue.file.max-file-size` | 单文件大小上限（默认 5MB） |
| `muyue.file.url-prefix` | 上传文件访问前缀，默认 `/profile`，生产需由 Nginx 转发到后端 |

> 生产部署时 Nginx 需同时转发接口前缀与 `/profile`（头像等静态资源），例如：
> ```nginx
> location /prod-api/ { proxy_pass http://127.0.0.1:8080/; }
> location /profile/  { proxy_pass http://127.0.0.1:8080/profile/; }
> ```

## 七、数据库兼容说明

两套 profile 完全隔离，切换只需改 `spring.profiles.active`：

| 文件 | 说明 |
| --- | --- |
| `application.yml` | 公共配置（端口、JWT、缓存、上传、MyBatis-Plus 通用配置） |
| `application-sqlite.yml` | SQLite 数据源 + `spring.sql.init` 自动建表初始化 |
| `application-oracle.yml` | Oracle 数据源，禁用自动脚本（用 `sql/oracle/*.sql` 手动执行） |

为保证跨库兼容，代码层面已做处理：

- 分页方言由 `DbTypeHolder`（`muyue.db-type`）动态选择 `DbType.SQLITE` / `DbType.ORACLE`
- 取一条记录的方言差异封装在 `DbTypeHolder.limitOne()`（SQLite `LIMIT 1` / Oracle `ROWNUM = 1`）
- 用户角色、角色菜单关联统一改为单条插入（避免 Oracle `INSERT ALL` 与 SQLite 不兼容）
- 操作日志时间范围查询使用 MyBatis `_databaseId` 分支（Oracle `TO_TIMESTAMP`，其它直接比较）
- SQL 中避免使用数据库专有函数，`LIKE` 统一使用 `'%' || #{x} || '%'`（两者都支持）

冒烟测试（SQLite 全流程：建表初始化 → 分页 → 验证码 → 登录 → 菜单路由 → 新增用户 → 操作日志）：

```bash
cd muyue-admin
mvn test -Dtest=SqliteSmokeTest
```

## 八、操作日志使用

- 在控制器方法上加注解即可自动记录：`@Log(title = "用户管理", businessType = BusinessType.INSERT)`
- 切面 `LogAspect` 自动采集操作人、IP、URL、请求参数（密码类字段自动脱敏）、返回结果、耗时、异常信息
- 页面「系统管理 → 操作记录」：普通用户只看到自己的日志；授予 `monitor:operlog:list` 后可查看全部；`monitor:operlog:remove` 控制删除 / 清空

## 六、开发约定

- 新增菜单后，需要在「角色管理」中勾选授权，重新登录即可生效。
- 按钮权限：后端在菜单表配置 `system:xxx:yyy`，控制器加 `@PreAuthorize("@ps.hasPermi('system:xxx:yyy')")`，前端按钮加 `v-hasPermi="['system:xxx:yyy']"`。
- 前端新增页面放到 `src/views` 下，菜单「组件路径」填写相对路径，如 `system/user/index`。
