-- =========================================================
-- 沐月脚手架 Oracle 表结构
-- 执行顺序：先执行本文件，再执行 02_data.sql
-- 说明：主键使用雪花 ID（MyBatis-Plus ASSIGN_ID），无需序列
-- =========================================================

-- ----------------------------
-- 1、部门表
-- ----------------------------
CREATE TABLE sys_dept (
  dept_id     VARCHAR2(20)     NOT NULL,
  parent_id   VARCHAR2(20)     DEFAULT 0,
  ancestors   VARCHAR2(500)  DEFAULT '',
  dept_name   VARCHAR2(30)   DEFAULT '',
  order_num   NUMBER(4)      DEFAULT 0,
  leader      VARCHAR2(20),
  phone       VARCHAR2(11),
  email       VARCHAR2(50),
  status      CHAR(1)        DEFAULT '0',
  del_flag    CHAR(1)        DEFAULT '0',
  create_by   VARCHAR2(64)   DEFAULT '',
  create_time TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  update_by   VARCHAR2(64)   DEFAULT '',
  update_time TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  remark      VARCHAR2(500)  DEFAULT '',
  PRIMARY KEY (dept_id)
);
COMMENT ON TABLE  sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.dept_id IS '部门ID';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.order_num IS '显示顺序';
COMMENT ON COLUMN sys_dept.leader IS '负责人';
COMMENT ON COLUMN sys_dept.phone IS '联系电话';
COMMENT ON COLUMN sys_dept.email IS '邮箱';
COMMENT ON COLUMN sys_dept.status IS '部门状态（0正常 1停用）';
COMMENT ON COLUMN sys_dept.del_flag IS '删除标志（0存在 2删除）';

-- ----------------------------
-- 2、用户表
-- ----------------------------
CREATE TABLE sys_user (
  user_id     VARCHAR2(20)     NOT NULL,
  dept_id     VARCHAR2(20),
  user_name   VARCHAR2(30)   NOT NULL,
  nick_name   VARCHAR2(30)   DEFAULT '',
  user_type   VARCHAR2(2)    DEFAULT '00',
  email       VARCHAR2(50)   DEFAULT '',
  phonenumber VARCHAR2(11)   DEFAULT '',
  sex         CHAR(1)        DEFAULT '0',
  avatar      VARCHAR2(255)  DEFAULT '',
  password    VARCHAR2(100)  DEFAULT '',
  status      CHAR(1)        DEFAULT '0',
  del_flag    CHAR(1)        DEFAULT '0',
  login_ip    VARCHAR2(128)  DEFAULT '',
  login_date  TIMESTAMP(6),
  create_by   VARCHAR2(64)   DEFAULT '',
  create_time TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  update_by   VARCHAR2(64)   DEFAULT '',
  update_time TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  remark      VARCHAR2(500),
  PRIMARY KEY (user_id)
);
COMMENT ON TABLE  sys_user IS '用户表';
COMMENT ON COLUMN sys_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.user_name IS '登录账号';
COMMENT ON COLUMN sys_user.nick_name IS '用户昵称';
COMMENT ON COLUMN sys_user.user_type IS '用户类型（00系统用户）';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phonenumber IS '手机号';
COMMENT ON COLUMN sys_user.sex IS '性别（0男 1女 2未知）';
COMMENT ON COLUMN sys_user.avatar IS '头像';
COMMENT ON COLUMN sys_user.password IS '密码（BCrypt）';
COMMENT ON COLUMN sys_user.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN sys_user.del_flag IS '删除标志（0存在 2删除）';
COMMENT ON COLUMN sys_user.login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.login_date IS '最后登录时间';

CREATE INDEX idx_sys_user_dept ON sys_user (dept_id);
CREATE INDEX idx_sys_user_name ON sys_user (user_name);

-- ----------------------------
-- 3、角色表
-- ----------------------------
CREATE TABLE sys_role (
  role_id            VARCHAR2(20)   NOT NULL,
  role_name          VARCHAR2(30) NOT NULL,
  role_key           VARCHAR2(100) NOT NULL,
  role_sort          NUMBER(4)    DEFAULT 0,
  data_scope         CHAR(1)      DEFAULT '1',
  menu_check_strictly NUMBER(1)   DEFAULT 1,
  dept_check_strictly NUMBER(1)   DEFAULT 1,
  status             CHAR(1)      DEFAULT '0',
  del_flag           CHAR(1)      DEFAULT '0',
  create_by          VARCHAR2(64) DEFAULT '',
  create_time        TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
  update_by          VARCHAR2(64) DEFAULT '',
  update_time        TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
  remark             VARCHAR2(500),
  PRIMARY KEY (role_id)
);
COMMENT ON TABLE  sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_key IS '角色权限字符串';
COMMENT ON COLUMN sys_role.role_sort IS '显示顺序';
COMMENT ON COLUMN sys_role.data_scope IS '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）';
COMMENT ON COLUMN sys_role.status IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN sys_role.del_flag IS '删除标志（0存在 2删除）';

-- ----------------------------
-- 4、用户角色关联表
-- ----------------------------
CREATE TABLE sys_user_role (
  user_id VARCHAR2(20) NOT NULL,
  role_id VARCHAR2(20) NOT NULL,
  PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE  sys_user_role IS '用户与角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
CREATE TABLE sys_menu (
  menu_id    VARCHAR2(20)     NOT NULL,
  menu_name  VARCHAR2(50)   NOT NULL,
  parent_id  VARCHAR2(20)     DEFAULT 0,
  order_num  NUMBER(4)      DEFAULT 0,
  path       VARCHAR2(200)  DEFAULT '',
  component  VARCHAR2(255),
  query      VARCHAR2(255),
  is_frame   CHAR(1)        DEFAULT '1',
  is_cache   CHAR(1)        DEFAULT '0',
  menu_type  CHAR(1)        DEFAULT '',
  visible    CHAR(1)        DEFAULT '0',
  status     CHAR(1)        DEFAULT '0',
  perms      VARCHAR2(100),
  icon       VARCHAR2(100)  DEFAULT '#',
  create_by  VARCHAR2(64)   DEFAULT '',
  create_time TIMESTAMP(6)  DEFAULT CURRENT_TIMESTAMP,
  update_by  VARCHAR2(64)   DEFAULT '',
  update_time TIMESTAMP(6)  DEFAULT CURRENT_TIMESTAMP,
  remark     VARCHAR2(500)  DEFAULT '',
  PRIMARY KEY (menu_id)
);
COMMENT ON TABLE  sys_menu IS '菜单权限表';
COMMENT ON COLUMN sys_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.order_num IS '显示顺序';
COMMENT ON COLUMN sys_menu.path IS '路由地址';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.query IS '路由参数';
COMMENT ON COLUMN sys_menu.is_frame IS '是否外链（0是 1否）';
COMMENT ON COLUMN sys_menu.is_cache IS '是否缓存（0缓存 1不缓存）';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型（M目录 C菜单 F按钮）';
COMMENT ON COLUMN sys_menu.visible IS '显示状态（0显示 1隐藏）';
COMMENT ON COLUMN sys_menu.status IS '菜单状态（0正常 1停用）';
COMMENT ON COLUMN sys_menu.perms IS '权限标识';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';

-- ----------------------------
-- 6、角色菜单关联表
-- ----------------------------
CREATE TABLE sys_role_menu (
  role_id VARCHAR2(20) NOT NULL,
  menu_id VARCHAR2(20) NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);
COMMENT ON TABLE  sys_role_menu IS '角色与菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

-- ----------------------------
-- 订单主表
-- ----------------------------
CREATE TABLE biz_order (
  order_id     VARCHAR2(20)     NOT NULL,
  order_no     VARCHAR2(50)   DEFAULT '',
  user_name    VARCHAR2(64)   DEFAULT '',
  total_amount NUMBER(12, 2)  DEFAULT 0,
  status       CHAR(1)        DEFAULT '0',
  pay_type     CHAR(1)        DEFAULT '0',
  receiver     VARCHAR2(64)   DEFAULT '',
  phone        VARCHAR2(20)   DEFAULT '',
  address      VARCHAR2(255)  DEFAULT '',
  create_by    VARCHAR2(64)   DEFAULT '',
  create_time  TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  update_by    VARCHAR2(64)   DEFAULT '',
  update_time  TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  remark       VARCHAR2(500)  DEFAULT '',
  PRIMARY KEY (order_id)
);
COMMENT ON TABLE  biz_order IS '订单主表';
COMMENT ON COLUMN biz_order.order_no IS '订单编号';
COMMENT ON COLUMN biz_order.user_name IS '买家名称';
COMMENT ON COLUMN biz_order.total_amount IS '订单总额';
COMMENT ON COLUMN biz_order.status IS '订单状态（0待付款 1已付款 2已发货 3已完成 4已取消）';
COMMENT ON COLUMN biz_order.pay_type IS '支付方式（0支付宝 1微信 2货到付款）';
COMMENT ON COLUMN biz_order.receiver IS '收货人';
COMMENT ON COLUMN biz_order.phone IS '联系电话';
COMMENT ON COLUMN biz_order.address IS '收货地址';

-- ----------------------------
-- 订单明细表
-- ----------------------------
CREATE TABLE biz_order_item (
  item_id      VARCHAR2(20)     NOT NULL,
  order_id     VARCHAR2(20)     NOT NULL,
  product_name VARCHAR2(128)  DEFAULT '',
  price        NUMBER(12, 2)  DEFAULT 0,
  quantity     NUMBER(6)      DEFAULT 1,
  total_price  NUMBER(12, 2)  DEFAULT 0,
  PRIMARY KEY (item_id)
);
COMMENT ON TABLE  biz_order_item IS '订单明细表';
COMMENT ON COLUMN biz_order_item.order_id IS '所属订单ID';
COMMENT ON COLUMN biz_order_item.product_name IS '商品名称';
COMMENT ON COLUMN biz_order_item.price IS '商品单价';
COMMENT ON COLUMN biz_order_item.quantity IS '购买数量';
COMMENT ON COLUMN biz_order_item.total_price IS '小计金额';

-- ----------------------------
-- 通知公告表
-- ----------------------------
CREATE TABLE sys_notice (
  notice_id      VARCHAR2(20)     NOT NULL,
  notice_title   VARCHAR2(100)  DEFAULT '',
  notice_type    CHAR(1)        DEFAULT '1',
  notice_content CLOB,
  status         CHAR(1)        DEFAULT '0',
  create_by      VARCHAR2(64)   DEFAULT '',
  create_time    TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  update_by      VARCHAR2(64)   DEFAULT '',
  update_time    TIMESTAMP(6)   DEFAULT CURRENT_TIMESTAMP,
  remark         VARCHAR2(500)  DEFAULT '',
  PRIMARY KEY (notice_id)
);
COMMENT ON TABLE  sys_notice IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_title IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_type IS '公告类型（1通知 2公告）';
COMMENT ON COLUMN sys_notice.notice_content IS '公告内容';
COMMENT ON COLUMN sys_notice.status IS '公告状态（0正常 1关闭）';

-- ----------------------------
-- 公告已读记录表
-- ----------------------------
CREATE TABLE sys_notice_read (
  user_id   VARCHAR2(20)   NOT NULL,
  notice_id VARCHAR2(20)   NOT NULL,
  read_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, notice_id)
);
COMMENT ON TABLE sys_notice_read IS '公告已读记录表';

-- ----------------------------
-- 邮箱配置表（单行）
-- ----------------------------
CREATE TABLE sys_mail_config (
  config_id   VARCHAR2(20)    NOT NULL,
  host        VARCHAR2(128) DEFAULT '',
  port        NUMBER(6)     DEFAULT 465,
  username    VARCHAR2(128) DEFAULT '',
  password    VARCHAR2(128) DEFAULT '',
  from_addr   VARCHAR2(128) DEFAULT '',
  nickname    VARCHAR2(64)  DEFAULT '',
  ssl_enabled CHAR(1)       DEFAULT '1',
  update_by   VARCHAR2(64)  DEFAULT '',
  update_time TIMESTAMP(6)  DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (config_id)
);
COMMENT ON TABLE sys_mail_config IS '邮箱发送配置表';

-- ----------------------------
-- 附件表
-- ----------------------------
CREATE TABLE sys_file (
  file_id    VARCHAR2(20)    NOT NULL,
  file_name  VARCHAR2(255) DEFAULT '',
  file_path  VARCHAR2(500) DEFAULT '',
  file_size  NUMBER(20)    DEFAULT 0,
  file_type  VARCHAR2(32)  DEFAULT '',
  create_by  VARCHAR2(64)  DEFAULT '',
  create_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (file_id)
);
COMMENT ON TABLE sys_file IS '附件表';
