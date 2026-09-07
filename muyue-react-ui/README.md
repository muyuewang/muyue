# muyue-react-ui

沐月管理系统前端 **React + Ant Design Pro** 版本，与 `muyue-ui`（Vue 版）共用同一后端（接口、协议完全一致）。

## 启动

```bash
npm install
npm run dev      # http://localhost:2024，代理 /dev-api → http://localhost:8080
npm run build    # 产物输出 dist/
```

环境变量见 `.env.development`（`VITE_BACKEND_URL` 可指向任意后端地址）。

## 架构说明

- **UI**：`@ant-design/pro-components`（ProLayout / ProTable / PageContainer）+ antd 5
- **请求**：axios 封装 `src/utils/request.js`
  - `Authorization: Bearer <token>` 请求头
  - `TableDataInfo`（顶层 `rows/total`）与 `R<T>`（`data` 包裹）两种响应结构均已适配
  - 401 弹窗引导重新登录
- **动态菜单**：登录后 `getRouters` 拉取路由树 → `ProLayout` 渲染侧边栏 →
  `App.jsx` 的 `componentMap` 把菜单 `component` 字段映射到 React 页面组件，
  未登记的菜单显示「页面建设中」兜底页，**新增页面只需写组件 + 登记 key**

## 已实现页面

| 菜单 | component | 说明 |
|------|-----------|------|
| 首页 | index | 快捷入口 |
| 用户管理 | system/user/index | 部门树过滤 + 增删改查 |
| 通知公告 | system/notice/index | 发布/修改/删除/详情（查看即标记已读） |
| 订单管理 | order/order/index | 搜索分页 + 明细弹窗 + 明细行内编辑（自动汇总） |
| 操作日志 | monitor/operlog/index | 搜索/详情/删除/清空 |
| 登录日志 | monitor/logininfor/index | 搜索分页 |

其余菜单（角色/菜单/部门/岗位/字典/在线用户/服务器监控/代码生成/大屏等）
按上述模式在 `componentMap` 注册即可平滑补齐。
