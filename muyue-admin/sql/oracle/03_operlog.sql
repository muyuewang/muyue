-- =========================================================
-- 沐月脚手架 Oracle 增量脚本：操作日志 + 个人中心/操作记录菜单
-- 前置：已执行 01_schema.sql 与 02_data.sql
-- 本脚本可重复执行
-- =========================================================

-- ----------------------------
-- 1、操作日志表
-- ----------------------------
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1) INTO v_count FROM user_tables WHERE table_name = 'SYS_OPER_LOG';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE sys_oper_log (
        oper_id        NUMBER(20)    NOT NULL,
        title          VARCHAR2(100) DEFAULT '''',
        business_type  NUMBER(2)     DEFAULT 0,
        method         VARCHAR2(200) DEFAULT '''',
        request_method VARCHAR2(10)  DEFAULT '''',
        operator_type  NUMBER(1)     DEFAULT 0,
        oper_name      VARCHAR2(50)  DEFAULT '''',
        dept_name      VARCHAR2(50)  DEFAULT '''',
        oper_url       VARCHAR2(255) DEFAULT '''',
        oper_ip        VARCHAR2(128) DEFAULT '''',
        oper_location  VARCHAR2(255) DEFAULT '''',
        oper_param     VARCHAR2(2000) DEFAULT '''',
        json_result    VARCHAR2(2000) DEFAULT '''',
        status         NUMBER(1)     DEFAULT 0,
        error_msg      VARCHAR2(2000) DEFAULT '''',
        oper_time      TIMESTAMP(6)  DEFAULT CURRENT_TIMESTAMP,
        cost_time      NUMBER(10)    DEFAULT 0,
        PRIMARY KEY (oper_id)
      )';
    EXECUTE IMMEDIATE 'COMMENT ON TABLE  sys_oper_log IS ''操作日志记录''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_id IS ''日志主键''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.title IS ''操作模块''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.business_type IS ''业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空）''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.method IS ''方法名称''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.request_method IS ''请求方式''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.operator_type IS ''操作类别（0其它 1后台用户 2手机端用户）''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_name IS ''操作人员''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.dept_name IS ''部门名称''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_url IS ''请求URL''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_ip IS ''主机地址''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_param IS ''请求参数''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.json_result IS ''返回结果''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.status IS ''操作状态（0正常 1异常）''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.error_msg IS ''错误消息''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.oper_time IS ''操作时间''';
    EXECUTE IMMEDIATE 'COMMENT ON COLUMN sys_oper_log.cost_time IS ''消耗时间（毫秒）''';
    EXECUTE IMMEDIATE 'CREATE INDEX idx_sys_oper_log_time ON sys_oper_log (oper_time)';
    EXECUTE IMMEDIATE 'CREATE INDEX idx_sys_oper_log_name ON sys_oper_log (oper_name)';
  END IF;
END;
/

-- ----------------------------
-- 2、菜单：操作记录（系统管理目录下）
-- ----------------------------
DELETE FROM sys_role_menu WHERE menu_id IN (105, 1051, 1052);
DELETE FROM sys_menu WHERE menu_id IN (105, 1051, 1052);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES (105, '操作记录', 1, 5, 'operlog', 'monitor/operlog/index', '1', '0', 'C', '0', '0', 'system:operlog:list', 'Document', 'admin', CURRENT_TIMESTAMP, '操作记录菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES (1051, '日志删除', 105, 1, '', NULL, '1', '0', 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', CURRENT_TIMESTAMP, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES (1052, '查看全部日志', 105, 2, '', NULL, '1', '0', 'F', '1', '0', 'monitor:operlog:list', '#', 'admin', CURRENT_TIMESTAMP, '');

-- ----------------------------
-- 3、授权：超级管理员（全部），普通角色（仅查看自己的操作记录）
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 105);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1051);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1052);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 105);

COMMIT;
