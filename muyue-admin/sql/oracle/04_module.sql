-- =========================================================
-- 沐月脚手架 Oracle 增量脚本（模块扩展：岗位 / 字典 / 登录日志）
-- 执行前提：已执行 01_schema.sql、02_data.sql、03_operlog.sql
-- 本脚本幂等：重复执行安全
-- =========================================================

-- 幂等删除扩展表（保留数据请谨慎）
BEGIN
  FOR t IN ('SYS_POST', 'SYS_USER_POST', 'SYS_DICT_TYPE', 'SYS_DICT_DATA', 'SYS_LOGININFOR') LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP TABLE ' || t || ' PURGE';
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
  END LOOP;
END;
/

-- ----------------------------
-- 岗位表
-- ----------------------------
CREATE TABLE sys_post (
  post_id     NUMBER(20) PRIMARY KEY,
  post_code   VARCHAR2(64)  NOT NULL,
  post_name   VARCHAR2(50)  NOT NULL,
  post_sort   NUMBER(4)     DEFAULT 0,
  status      CHAR(1)       DEFAULT '0',
  create_by   VARCHAR2(64)  DEFAULT '',
  create_time DATE          DEFAULT SYSDATE,
  update_by   VARCHAR2(64)  DEFAULT '',
  update_time DATE          DEFAULT SYSDATE,
  remark      VARCHAR2(500) DEFAULT ''
);

-- ----------------------------
-- 用户岗位关联表
-- ----------------------------
CREATE TABLE sys_user_post (
  user_id NUMBER(20) NOT NULL,
  post_id NUMBER(20) NOT NULL,
  CONSTRAINT pk_sys_user_post PRIMARY KEY (user_id, post_id)
);

-- ----------------------------
-- 字典类型表
-- ----------------------------
CREATE TABLE sys_dict_type (
  dict_id     NUMBER(20) PRIMARY KEY,
  dict_name   VARCHAR2(100) NOT NULL,
  dict_type   VARCHAR2(100) NOT NULL,
  status      CHAR(1)       DEFAULT '0',
  create_by   VARCHAR2(64)  DEFAULT '',
  create_time DATE          DEFAULT SYSDATE,
  update_by   VARCHAR2(64)  DEFAULT '',
  update_time DATE          DEFAULT SYSDATE,
  remark      VARCHAR2(500) DEFAULT ''
);
CREATE INDEX idx_sys_dict_type ON sys_dict_type (dict_type);

-- ----------------------------
-- 字典数据表
-- ----------------------------
CREATE TABLE sys_dict_data (
  dict_code   NUMBER(20) PRIMARY KEY,
  dict_sort   NUMBER(4)     DEFAULT 0,
  dict_label  VARCHAR2(100) NOT NULL,
  dict_value  VARCHAR2(100) NOT NULL,
  dict_type   VARCHAR2(100) NOT NULL,
  css_class   VARCHAR2(100) DEFAULT '',
  list_class  VARCHAR2(100) DEFAULT '',
  is_default  CHAR(1)       DEFAULT 'N',
  status      CHAR(1)       DEFAULT '0',
  create_by   VARCHAR2(64)  DEFAULT '',
  create_time DATE          DEFAULT SYSDATE,
  update_by   VARCHAR2(64)  DEFAULT '',
  update_time DATE          DEFAULT SYSDATE,
  remark      VARCHAR2(500) DEFAULT ''
);
CREATE INDEX idx_sys_dict_data ON sys_dict_data (dict_type);

-- ----------------------------
-- 登录日志表
-- ----------------------------
CREATE TABLE sys_logininfor (
  info_id     NUMBER(20) PRIMARY KEY,
  user_name   VARCHAR2(50)  DEFAULT '',
  ipaddr      VARCHAR2(128) DEFAULT '',
  status      CHAR(1)       DEFAULT '0',
  msg         VARCHAR2(255) DEFAULT '',
  access_time DATE          DEFAULT SYSDATE
);
CREATE INDEX idx_sys_logininfor_time ON sys_logininfor (access_time);
CREATE INDEX idx_sys_logininfor_name ON sys_logininfor (user_name);

-- ----------------------------
-- 菜单与授权
-- ----------------------------
DELETE FROM sys_role_menu WHERE menu_id IN (106,107,108,1061,1062,1063,1064,1071,1072,1073,1074,1081,109,110,111,1101,1102,1111);
DELETE FROM sys_menu WHERE menu_id IN (106,107,108,1061,1062,1063,1064,1071,1072,1073,1074,1081,109,110,111,1101,1102,1111,112,113,1121,1122,1131,1132,1133,1134,114,1141,1142,1143,1144,115,116,117,1161,1162,1171,1172);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (106, '岗位管理', 1, 6, 'post', 'system/post/index', '1', '0', 'C', '0', '0', 'system:post:list', 'Postcard', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (107, '字典管理', 1, 7, 'dict', 'system/dict/index', '1', '0', 'C', '0', '0', 'system:dict:list', 'Collection', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (108, '登录日志', 1, 8, 'logininfor', 'monitor/logininfor/index', '1', '0', 'C', '0', '0', 'monitor:logininfor:list', 'List', 'admin');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1061, '岗位查询', 106, 1, '', '', '1', '0', 'F', '0', '0', 'system:post:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1062, '岗位新增', 106, 2, '', '', '1', '0', 'F', '0', '0', 'system:post:add', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1063, '岗位修改', 106, 3, '', '', '1', '0', 'F', '0', '0', 'system:post:edit', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1064, '岗位删除', 106, 4, '', '', '1', '0', 'F', '0', '0', 'system:post:remove', '#', 'admin');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1071, '字典查询', 107, 1, '', '', '1', '0', 'F', '0', '0', 'system:dict:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1072, '字典新增', 107, 2, '', '', '1', '0', 'F', '0', '0', 'system:dict:add', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1073, '字典修改', 107, 3, '', '', '1', '0', 'F', '0', '0', 'system:dict:edit', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1074, '字典删除', 107, 4, '', '', '1', '0', 'F', '0', '0', 'system:dict:remove', '#', 'admin');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1081, '登录日志删除', 108, 1, '', '', '1', '0', 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin');

-- 系统监控菜单（目录 + 在线用户 + 服务器监控）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (109, '系统监控', 1, 9, 'monitor', '', '1', '0', 'M', '0', '0', '', 'Monitor', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (110, '在线用户', 109, 1, 'online', 'monitor/online/index', '1', '0', 'C', '0', '0', 'monitor:online:list', 'Connection', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (111, '服务器监控', 109, 2, 'server', 'monitor/server/index', '1', '0', 'C', '0', '0', 'monitor:server:list', 'Cpu', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1101, '在线查询', 110, 1, '', '', '1', '0', 'F', '0', '0', 'monitor:online:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1102, '强退用户', 110, 2, '', '', '1', '0', 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1111, '服务查询', 111, 1, '', '', '1', '0', 'F', '0', '0', 'monitor:server:query', '#', 'admin');

-- 超级管理员：授予全部
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 106);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1061);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1062);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1063);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1064);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 107);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1071);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1072);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1073);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1074);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 108);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1081);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 109);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 110);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1101);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1102);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 111);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1111);

-- 普通角色：只读
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 106);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1061);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 107);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1071);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 108);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 109);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 110);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1101);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 111);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1111);

-- ----------------------------
-- 字典演示数据
-- ----------------------------
DELETE FROM sys_dict_data WHERE dict_type IN ('sys_user_sex','sys_normal_disable','sys_common_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('sys_user_sex','sys_normal_disable','sys_common_status');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, remark) VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '用户性别列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, remark) VALUES (2, '系统开关', 'sys_normal_disable', '0', 'admin', '系统开关状态');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, remark) VALUES (3, '系统状态', 'sys_common_status', '0', 'admin', '系统操作状态');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (1, 1, '男', '0', 'sys_user_sex', 'primary', 'Y', '0', 'admin');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (2, 2, '女', '1', 'sys_user_sex', 'danger', 'N', '0', 'admin');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (3, 1, '正常', '0', 'sys_normal_disable', 'primary', 'Y', '0', 'admin');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (4, 2, '停用', '1', 'sys_normal_disable', 'danger', 'N', '0', 'admin');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (5, 1, '成功', '0', 'sys_common_status', 'success', 'Y', '0', 'admin');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by)
VALUES (6, 2, '失败', '1', 'sys_common_status', 'danger', 'N', '0', 'admin');

-- ----------------------------
-- 代码生成 / 订单管理菜单
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (112, '代码生成', 1, 10, 'gen', 'tool/gen/index', '1', '0', 'C', '0', '0', 'tool:gen:list', 'MagicStick', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (113, '订单管理', 1, 11, 'order', 'order/order/index', '1', '0', 'C', '0', '0', 'order:order:list', 'ShoppingCart', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1121, '代码预览', 112, 1, '', '', '1', '0', 'F', '0', '0', 'tool:gen:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1122, '代码下载', 112, 2, '', '', '1', '0', 'F', '0', '0', 'tool:gen:code', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1131, '订单查询', 113, 1, '', '', '1', '0', 'F', '0', '0', 'order:order:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1132, '订单新增', 113, 2, '', '', '1', '0', 'F', '0', '0', 'order:order:add', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1133, '订单修改', 113, 3, '', '', '1', '0', 'F', '0', '0', 'order:order:edit', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1134, '订单删除', 113, 4, '', '', '1', '0', 'F', '0', '0', 'order:order:remove', '#', 'admin');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 112);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1121);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1122);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 113);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1131);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1132);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1133);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1134);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 112);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1121);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 113);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1131);

-- ----------------------------
-- 菜单结构调整：系统监控 / 订单管理提升为一级菜单，操作记录归入系统监控
-- ----------------------------
UPDATE sys_menu SET parent_id = 0, order_num = 2 WHERE menu_id = 109;
UPDATE sys_menu SET parent_id = 109, order_num = 3 WHERE menu_id = 105;
UPDATE sys_menu SET parent_id = 0, order_num = 3 WHERE menu_id = 113;

-- 普通角色更名为普通用户
UPDATE sys_role SET role_name = '普通用户' WHERE role_id = 2;

-- ----------------------------
-- 通知公告菜单（一级菜单，普通用户可查看）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (114, '通知公告', 0, 4, 'notice', 'system/notice/index', '1', '0', 'C', '0', '0', 'system:notice:list', 'Bell', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (115, '数据大屏', 0, 5, 'screen', 'screen/jump', '1', '0', 'C', '0', '0', 'screen:stats', 'DataBoard', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1141, '公告查询', 114, 1, '', '', '1', '0', 'F', '0', '0', 'system:notice:query', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1142, '公告新增', 114, 2, '', '', '1', '0', 'F', '0', '0', 'system:notice:add', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1143, '公告修改', 114, 3, '', '', '1', '0', 'F', '0', '0', 'system:notice:edit', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1144, '公告删除', 114, 4, '', '', '1', '0', 'F', '0', '0', 'system:notice:remove', '#', 'admin');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 114);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1141);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1142);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1143);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1144);

-- 普通用户：仅可查看通知公告
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 114);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1141);

-- 数据大屏菜单授权
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 115);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 115);

-- ----------------------------
-- 邮箱工具 / 附件工具菜单（系统管理下）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (116, '邮箱工具', 1, 12, 'mail', 'tool/mail/index', '1', '0', 'C', '0', '0', 'tool:mail:list', 'Message', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (117, '附件工具', 1, 13, 'file', 'tool/file/index', '1', '0', 'C', '0', '0', 'tool:file:list', 'FolderOpened', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1161, '邮箱配置保存', 116, 1, '', '', '1', '0', 'F', '0', '0', 'tool:mail:edit', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1162, '发送测试邮件', 116, 2, '', '', '1', '0', 'F', '0', '0', 'tool:mail:send', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1171, '附件上传', 117, 1, '', '', '1', '0', 'F', '0', '0', 'tool:file:upload', '#', 'admin');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
VALUES (1172, '附件删除', 117, 2, '', '', '1', '0', 'F', '0', '0', 'tool:file:remove', '#', 'admin');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 116);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1161);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1162);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 117);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1171);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1172);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 117);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1171);

COMMIT;
