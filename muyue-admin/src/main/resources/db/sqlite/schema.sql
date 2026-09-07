-- =========================================================
-- 沐月脚手架 SQLite 表结构（启动时由 spring.sql.init 自动执行）
-- 数据文件：./data/muyue.db
-- =========================================================

-- ----------------------------
-- 1、部门表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id     INTEGER PRIMARY KEY,
  parent_id   INTEGER DEFAULT 0,
  ancestors   TEXT    DEFAULT '',
  dept_name   TEXT    DEFAULT '',
  order_num   INTEGER DEFAULT 0,
  leader      TEXT,
  phone       TEXT,
  email       TEXT,
  status      TEXT    DEFAULT '0',
  del_flag    TEXT    DEFAULT '0',
  create_by   TEXT    DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by   TEXT    DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark      TEXT    DEFAULT ''
);

-- ----------------------------
-- 2、用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
  user_id     INTEGER PRIMARY KEY,
  dept_id     INTEGER,
  user_name   TEXT NOT NULL,
  nick_name   TEXT DEFAULT '',
  user_type   TEXT DEFAULT '00',
  email       TEXT DEFAULT '',
  phonenumber TEXT DEFAULT '',
  sex         TEXT DEFAULT '0',
  avatar      TEXT DEFAULT '',
  password    TEXT DEFAULT '',
  status      TEXT DEFAULT '0',
  del_flag    TEXT DEFAULT '0',
  login_ip    TEXT DEFAULT '',
  login_date  DATETIME,
  create_by   TEXT DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by   TEXT DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark      TEXT
);
CREATE INDEX IF NOT EXISTS idx_sys_user_dept ON sys_user (dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_name ON sys_user (user_name);

-- ----------------------------
-- 3、角色表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
  role_id             INTEGER PRIMARY KEY,
  role_name           TEXT NOT NULL,
  role_key            TEXT NOT NULL,
  role_sort           INTEGER DEFAULT 0,
  data_scope          TEXT DEFAULT '1',
  menu_check_strictly INTEGER DEFAULT 1,
  dept_check_strictly INTEGER DEFAULT 1,
  status              TEXT DEFAULT '0',
  del_flag            TEXT DEFAULT '0',
  create_by           TEXT DEFAULT '',
  create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by           TEXT DEFAULT '',
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark              TEXT
);

-- ----------------------------
-- 4、用户角色关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id INTEGER NOT NULL,
  role_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
  menu_id    INTEGER PRIMARY KEY,
  menu_name  TEXT NOT NULL,
  parent_id  INTEGER DEFAULT 0,
  order_num  INTEGER DEFAULT 0,
  path       TEXT DEFAULT '',
  component  TEXT,
  query      TEXT,
  is_frame   TEXT DEFAULT '1',
  is_cache   TEXT DEFAULT '0',
  menu_type  TEXT DEFAULT '',
  visible    TEXT DEFAULT '0',
  status     TEXT DEFAULT '0',
  perms      TEXT,
  icon       TEXT DEFAULT '#',
  create_by  TEXT DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by  TEXT DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark     TEXT DEFAULT ''
);

-- ----------------------------
-- 6、角色菜单关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id INTEGER NOT NULL,
  menu_id INTEGER NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);

-- ----------------------------
-- 7、操作日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id        INTEGER PRIMARY KEY,
  title          TEXT DEFAULT '',
  business_type  INTEGER DEFAULT 0,
  method         TEXT DEFAULT '',
  request_method TEXT DEFAULT '',
  operator_type  INTEGER DEFAULT 0,
  oper_name      TEXT DEFAULT '',
  dept_name      TEXT DEFAULT '',
  oper_url       TEXT DEFAULT '',
  oper_ip        TEXT DEFAULT '',
  oper_location  TEXT DEFAULT '',
  oper_param     TEXT DEFAULT '',
  json_result    TEXT DEFAULT '',
  status         INTEGER DEFAULT 0,
  error_msg      TEXT DEFAULT '',
  oper_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  cost_time      INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_time ON sys_oper_log (oper_time);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_name ON sys_oper_log (oper_name);

-- ----------------------------
-- 8、岗位表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_post (
  post_id     INTEGER PRIMARY KEY,
  post_code   TEXT    NOT NULL,
  post_name   TEXT    NOT NULL,
  post_sort   INTEGER DEFAULT 0,
  status      TEXT    DEFAULT '0',
  create_by   TEXT    DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by   TEXT    DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark      TEXT
);

-- ----------------------------
-- 9、用户岗位关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_post (
  user_id INTEGER NOT NULL,
  post_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, post_id)
);

-- ----------------------------
-- 10、字典类型表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id     INTEGER PRIMARY KEY,
  dict_name   TEXT NOT NULL,
  dict_type   TEXT NOT NULL,
  status      TEXT DEFAULT '0',
  create_by   TEXT DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by   TEXT DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark      TEXT DEFAULT ''
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_type ON sys_dict_type (dict_type);

-- ----------------------------
-- 11、字典数据表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code   INTEGER PRIMARY KEY,
  dict_sort   INTEGER DEFAULT 0,
  dict_label  TEXT NOT NULL,
  dict_value  TEXT NOT NULL,
  dict_type   TEXT NOT NULL,
  css_class   TEXT,
  list_class  TEXT,
  is_default  TEXT DEFAULT 'N',
  status      TEXT DEFAULT '0',
  create_by   TEXT DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by   TEXT DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark      TEXT DEFAULT ''
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data ON sys_dict_data (dict_type);

-- ----------------------------
-- 12、登录日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_logininfor (
  info_id     INTEGER PRIMARY KEY,
  user_name   TEXT    DEFAULT '',
  ipaddr      TEXT    DEFAULT '',
  status      TEXT    DEFAULT '0',
  msg         TEXT    DEFAULT '',
  access_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_time ON sys_logininfor (access_time);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_name ON sys_logininfor (user_name);

-- ----------------------------
-- 13、订单主表
-- ----------------------------
CREATE TABLE IF NOT EXISTS biz_order (
  order_id     INTEGER PRIMARY KEY,
  order_no     TEXT    DEFAULT '',
  user_name    TEXT    DEFAULT '',
  total_amount NUMERIC DEFAULT 0,
  status       TEXT    DEFAULT '0',
  pay_type     TEXT    DEFAULT '0',
  receiver     TEXT    DEFAULT '',
  phone        TEXT    DEFAULT '',
  address      TEXT    DEFAULT '',
  create_by    TEXT    DEFAULT '',
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by    TEXT    DEFAULT '',
  update_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark       TEXT    DEFAULT ''
);
CREATE INDEX IF NOT EXISTS idx_biz_order_no ON biz_order (order_no);

-- ----------------------------
-- 14、订单明细表
-- ----------------------------
CREATE TABLE IF NOT EXISTS biz_order_item (
  item_id      INTEGER PRIMARY KEY,
  order_id     INTEGER NOT NULL,
  product_name TEXT    DEFAULT '',
  price        NUMERIC DEFAULT 0,
  quantity     INTEGER DEFAULT 1,
  total_price  NUMERIC DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_biz_order_item_order ON biz_order_item (order_id);

-- ----------------------------
-- 15、通知公告表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_notice (
  notice_id      INTEGER PRIMARY KEY,
  notice_title   TEXT    DEFAULT '',
  notice_type    TEXT    DEFAULT '1',
  notice_content TEXT    DEFAULT '',
  status         TEXT    DEFAULT '0',
  create_by      TEXT    DEFAULT '',
  create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by      TEXT    DEFAULT '',
  update_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
  remark         TEXT    DEFAULT ''
);

-- ----------------------------
-- 公告已读记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_notice_read (
  user_id   INTEGER NOT NULL,
  notice_id INTEGER NOT NULL,
  read_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, notice_id)
);

-- ----------------------------
-- 16、邮箱配置表（单行）
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_mail_config (
  config_id   INTEGER PRIMARY KEY,
  host        TEXT    DEFAULT '',
  port        INTEGER DEFAULT 465,
  username    TEXT    DEFAULT '',
  password    TEXT    DEFAULT '',
  from_addr   TEXT    DEFAULT '',
  nickname    TEXT    DEFAULT '',
  ssl_enabled TEXT    DEFAULT '1',
  update_by   TEXT    DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
INSERT OR IGNORE INTO sys_mail_config (config_id) VALUES (1);

-- ----------------------------
-- 17、附件表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_file (
  file_id    INTEGER PRIMARY KEY,
  file_name  TEXT    DEFAULT '',
  file_path  TEXT    DEFAULT '',
  file_size  INTEGER DEFAULT 0,
  file_type  TEXT    DEFAULT '',
  create_by  TEXT    DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
