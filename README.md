# 沐月（muyue）后台管理系统

一套类似若依的后台管理系统脚手架：RBAC 权限 + 动态菜单 + 电商订单（主从表）+ 代码生成 + 消息通知（未读体系）+ 系统监控 + 数据大屏。
**一套后端，两套前端**（Vue 版与 React 版功能对齐，可独立使用、交叉对照学习）。

| 端 | 技术栈 | 目录 | 端口 |
| --- | --- | --- | --- |
| 后端 | JDK 21 · Spring Boot 3.3 · MyBatis-Plus 3.5 · Spring Security 6 · JWT · **SQLite / Oracle 双库可切换** | `muyue-admin` | 8080 |
| 前端 A | Vue 3 · Vite 5 · Element Plus · Pinia | `muyue-ui` | 1024 |
| 前端 B | React 18 · Vite 5 · antd 5 · Ant Design Pro Components | `muyue-react-ui` | 2024 |

默认账号：`admin / admin123`（超级管理员）；`muyue / admin123`（普通用户，只读演示）。

---

## 一、功能清单

### 基础能力
- 登录认证：BCrypt 密码校验、JWT 无状态令牌（12h）、图形验证码（算术式，可关闭）、登录日志
- 动态菜单：登录后按角色拉取 `/getRouters`，前端组件注册表映射页面
- RBAC：用户 / 角色 / 菜单（目录-菜单-按钮三级）/ 部门 / 岗位，接口级 `@PreAuthorize` + 按钮级指令（Vue `v-hasPermi` / React `hasPermi` 过滤）
- 字典管理：类型 + 数据两级维护，业务下拉/回显
- 个人中心：资料维护、修改密码、头像上传
- 消息通知：公告发布 / 修改 / 删除，**按用户记录已读**（`sys_notice_read`），顶栏铃铛未读角标（读一条少一条，60s 轮询新消息）

### 业务模块
- **订单管理**：`biz_order` + `biz_order_item` 主从表，搜索分页、明细查看、明细行内编辑（自动汇总金额）、删除级联；订单号留空自动生成
- **代码生成**：读取库表元数据（SQLite / Oracle），预览生成 9 个文件（Entity/Mapper/XML/Service/Controller/api/页面/菜单 SQL），打包 zip 下载
- **附件工具**：上传 / 在线预览 / 下载 / 删除
- **邮箱工具**：SMTP 配置维护 + 富文本测试邮件
- **数据大屏**：汇总指标、月度订单趋势、状态 / 支付分布、分公司人数排行、最近订单（30s 自动刷新）

### 系统监控
- 在线用户（强退）、服务器监控（CPU / 内存 / JVM / 磁盘）、操作记录（`@Log` 注解自动采集，参数脱敏，普通用户只看自己）

### 前端体验
- Vue 版：页签栏 TagsView（刷新 / 关闭 / 关闭其他）、暗色模式、玻璃拟态登录页
- React 版：ProLayout 布局、ProTable 全功能表格、消息铃铛 + 未读联动总线、深色数据大屏

---

## 二、目录结构

```
muyue
├── muye-admin（muyue-admin）        # 后端（单模块）
│   ├── sql/oracle                  # Oracle 建表与初始化脚本（01~04 幂等）
│   ├── src/main/resources
│   │   ├── db/sqlite               # schema.sql / data.sql（启动自动执行，幂等）
│   │   └── mapper                  # MyBatis XML
│   └── src/main/java/com/muyue
│       ├── common                  # 统一返回/异常/JWT/Security/Jackson 配置/缓存抽象/工具
│       ├── system                  # 系统业务（mapper / service / 实体）
│       ├── order                   # 订单业务
│       ├── tool                    # 代码生成 / 附件 / 邮箱
│       ├── screen                  # 大屏统计
│       └── web                     # 控制器 / 登录服务 / DataInitializer
├── muyue-ui                        # Vue 3 版前端
└── muyue-react-ui                  # React 18 版前端
```

---

## 三、快速开始

### 1. 启动后端（默认内嵌 SQLite，零配置）

```bash
cd muyue-admin
mvn spring-boot:run
# 或：mvn -DskipTests package && java -jar target/muyue-admin.jar
```

- 启动自动建库建表并写入演示数据（`data/muyue.db`）；重置数据：删除 `muyue-admin/data` 目录后重启
- 切换 Oracle：执行 `sql/oracle/01~04.sql`，改 `application-oracle.yml` 连接，以 `oracle` profile 启动
- 8080 被占用时先清理：`Get-NetTCPConnection -LocalPort 8080 -State Listen | % { Stop-Process -Id $_.OwningProcess -Force }`

### 2. 启动前端（两版任选，可同时跑）

```bash
# Vue 版（http://localhost:1024）
cd muyue-ui && npm install && npm run dev

# React 版（http://localhost:2024）
cd muyue-react-ui && npm install && npm run dev
```

接口均通过 Vite 代理转发到后端：前缀 `/dev-api` → `http://localhost:8080`（见各自 `.env.development`）。

---

## 四、核心接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| GET | `/captchaImage` | 验证码（返回开关、key、base64 图） |
| POST | `/login` / POST `/logout` | 登录 / 退出 |
| GET | `/getInfo` / `/getRouters` | 用户信息+权限 / 动态路由 |
| GET/POST/PUT/DELETE | `/system/user` `/system/role` `/system/menu` `/system/dept` `/system/post` `/system/dict/type` `/system/dict/data` `/system/notice` | 系统管理 CRUD |
| GET/PUT | `/system/user/profile`（`/updatePwd`、`/avatar`） | 个人中心 |
| GET/POST/PUT/DELETE | `/order`（`/order/{id}` 返回含 `items` 明细） | 订单主从表 |
| GET | `/tool/gen/db/list`、`/tool/gen/preview/{table}`、`/tool/gen/download/{table}` | 代码生成 |
| GET/POST/DELETE | `/tool/file`（`/upload` `/preview/{id}` `/download/{id}`） | 附件 |
| GET/PUT | `/tool/mail/config`、POST `/tool/mail/send` | 邮箱 |
| GET | `/monitor/online/list`、DELETE `/{token}` | 在线用户 / 强退 |
| GET | `/monitor/server` | 服务器监控 |
| GET/DELETE | `/monitor/operlog`、`/monitor/logininfor` | 操作 / 登录日志 |
| GET | `/screen/stats` | 大屏统计（登录即可访问） |

### 统一响应约定（两版前端均已适配）

```jsonc
// R<T>：业务数据在 data 里
{ "code": 200, "msg": "操作成功", "data": {} }

// TableDataInfo：分页，rows/total 在顶层
{ "code": 200, "msg": "查询成功", "rows": [], "total": 0 }
```

- 超出 JS 安全整数的 Long（雪花 ID）自动序列化为字符串（`JacksonLongConfig`）
- 时间统一输出 `yyyy-MM-dd HH:mm:ss`（`JacksonTimeConfig`）
- 401 由前端拦截器统一处理（弹窗引导重新登录）

---

## 五、常用配置（application.yml）

| 配置 | 说明 |
| --- | --- |
| `spring.profiles.active` | `sqlite`（默认内嵌库）/ `oracle` |
| `muyue.jwt.secret` / `expire-time` | JWT 密钥（**生产必须改**）与有效期（分钟） |
| `muyue.captcha.enabled` / `type` | 验证码开关与类型（math / char） |
| `muyue.cache.type` | `memory` 单机 / `redis` 多机共享（验证码、登录会话、在线用户） |
| `muyue.user.default-password` | 新增 / 重置用户的默认密码 |
| `muyue.file.profile` / `max-file-size` | 上传根目录与单文件上限 |
| `muyue.db-type` | 方言 `sqlite` / `oracle`（分页、取一条等差异已封装） |

生产 Nginx 参考：转发接口前缀到 8080，并放行 `/profile/**`（头像等静态资源）。

---

## 六、双前端对照

| 能力 | Vue 版 | React 版 |
| --- | --- | --- |
| 全部系统管理页面 | ✅ | ✅ |
| 订单主从表 CRUD | ✅ | ✅ |
| 通知公告 + 未读角标联动 | ✅（Pinia store） | ✅（noticeBus 总线） |
| 代码生成（预览 / 下载） | ✅ | ✅ |
| 数据大屏 | ✅ 独立路由 | ✅ 深色仪表盘 |
| 页签栏 TagsView | ✅ | 待补 |
| 暗色模式 | ✅ | 待补 |
| 按钮权限 | `v-hasPermi` 指令 | `v-hasPermi` 式条件渲染 |

新增页面：后端菜单「组件路径」填 `xxx/yyy/index`，Vue 版放 `src/views` 对应文件；React 版在 `src/App.jsx` 的 `componentMap` 登记组件即可，未登记的菜单显示「建设中」兜底页。

---

## 七、数据库兼容说明

- 分页方言由 `DbTypeHolder` 动态选择（SQLite / Oracle）；单条查询差异封装 `DbTypeHolder.limitOne()`
- 关联表统一单条插入（避免 Oracle `INSERT ALL`）；`LIKE` 统一 `'%' || #{x} || '%'`
- 时间范围等方言分支使用 MyBatis `_databaseId`；新代码**避免在业务层写 `isSqlite()` 分支**，优先收口到公共方言工具
- SQLite profile 使用单连接池（`maximum-pool-size: 1`）规避 `SQLITE_BUSY`：**禁止在持有连接的代码块内再获取连接**（工具类已做防嵌套 + 方言缓存）

## 八、测试与约定

```bash
# SQLite 全流程冒烟（建表初始化 → 登录 → 分页 → 路由 → 用户 → 日志）
cd muyue-admin && mvn test -Dtest=SqliteSmokeTest
```

- 按钮权限三件套：菜单表配 `system:xxx:yyy` → 控制器 `@PreAuthorize("@ps.hasPermi('...')")` → 前端按钮级过滤
- 操作日志：控制器方法加 `@Log(title, businessType)`，切面自动采集（密码字段脱敏）
- 初始化脚本保持幂等（SQLite `INSERT OR IGNORE` / Oracle 先 DELETE 后 INSERT）
