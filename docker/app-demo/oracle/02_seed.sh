#!/bin/sh
set -eu

app_demo_user_hash="$(printf '%s' "${SMART_APP_DEMO_USER_PASSWORD_BCRYPT}" | sed -e 's/\$\$/\$/g' -e 's/^\$2y\$/\$2a$/')"
case "${app_demo_user_hash}" in
  '$2a$'*) ;;
  *) echo 'SMART_APP_DEMO_USER_PASSWORD_BCRYPT 不是 BCrypt 哈希' >&2; exit 1 ;;
esac
[ "${#app_demo_user_hash}" -eq 60 ] || { echo 'SMART_APP_DEMO_USER_PASSWORD_BCRYPT 长度无效' >&2; exit 1; }

sqlplus -s /nolog <<SQL
whenever oserror exit failure rollback
whenever sqlerror exit sql.sqlcode
CONNECT ${APP_USER}/"${APP_USER_PASSWORD}"@//localhost:1521/FREEPDB1
INSERT INTO sys_oauth_client_details (client_id, client_secret, resource_ids, scope, authorized_grant_types, access_token_validity, refresh_token_validity, autoapprove)
VALUES ('smart-app-demo', '{noop}${SMART_APP_DEMO_OAUTH_CLIENT_SECRET}', 'server', 'server', 'password,refresh_token', 3600, 86400, 'true');

INSERT INTO sys_role (role_id, role_name, role_code, role_desc, ds_type, create_time, del_flag) VALUES (201, '物品放行申请人', 'APP_ITEM_APPLY', '虚构演示角色', 1, SYSTIMESTAMP, '0');
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, ds_type, create_time, del_flag) VALUES (202, '物品放行审批人', 'APP_ITEM_APPROVE', '虚构演示角色', 1, SYSTIMESTAMP, '0');
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, ds_type, create_time, del_flag) VALUES (203, '安检执行人员', 'APP_SECURITY', '虚构演示角色', 1, SYSTIMESTAMP, '0');
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, ds_type, create_time, del_flag) VALUES (204, '押运人员', 'APP_ESCORT', '虚构演示角色', 1, SYSTIMESTAMP, '0');

INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (301, '申请物品放行', 'item-pass:apply', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (302, '审批物品放行', 'item-pass:approve', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (303, '执行物品放行', 'item-pass:execute', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (304, '东门岗位', 'item-pass:post:security-east', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (305, '西门岗位', 'item-pass:post:security-west', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (306, '核验供应商厂牌', 'supplier:execute', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (307, '供应商通行记录', 'supplier:read', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (308, '供应商东门岗位', 'supplier:post:security-east', 0, 1, '1', SYSTIMESTAMP, '0');
INSERT INTO sys_menu (menu_id, name, permission, parent_id, sort, type, create_time, del_flag) VALUES (309, '供应商西门岗位', 'supplier:post:security-west', 0, 1, '1', SYSTIMESTAMP, '0');

INSERT INTO sys_role_menu VALUES (201, 301);
INSERT INTO sys_role_menu VALUES (202, 302);
INSERT INTO sys_role_menu VALUES (203, 303);
INSERT INTO sys_role_menu VALUES (203, 304);
INSERT INTO sys_role_menu VALUES (203, 305);
INSERT INTO sys_role_menu VALUES (203, 306);
INSERT INTO sys_role_menu VALUES (203, 307);
INSERT INTO sys_role_menu VALUES (203, 308);
INSERT INTO sys_role_menu VALUES (203, 309);

INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (101, 'APP_EMPLOYEE', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示正式员工');
INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (102, 'APP_OUTSOURCE', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示外包人员');
INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (103, 'APP_DISPATCH', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示派遣人员');
INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (104, 'APP_SUPERVISOR', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示主管');
INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (105, 'APP_SECURITY', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示安检员');
INSERT INTO sys_user (user_id, username, password, create_time, del_flag, lock_flag, full_name) VALUES (106, 'APP_ESCORT', '${app_demo_user_hash}', SYSTIMESTAMP, '0', '0', '演示押运人');

INSERT INTO sys_user_role VALUES (101, 201);
INSERT INTO sys_user_role VALUES (102, 201);
INSERT INTO sys_user_role VALUES (103, 203);
INSERT INTO sys_user_role VALUES (104, 202);
INSERT INTO sys_user_role VALUES (105, 203);
INSERT INTO sys_user_role VALUES (106, 204);
INSERT INTO sys_user_park VALUES (101, 1, SYSTIMESTAMP);
INSERT INTO sys_user_park VALUES (102, 1, SYSTIMESTAMP);
INSERT INTO sys_user_park VALUES (103, 1, SYSTIMESTAMP);
INSERT INTO sys_user_park VALUES (104, 1, SYSTIMESTAMP);
INSERT INTO sys_user_park VALUES (105, 1, SYSTIMESTAMP);
INSERT INTO sys_user_park VALUES (106, 1, SYSTIMESTAMP);

INSERT INTO smt_organize_relation (id, comp_name, park_id, comp_id, create_time, comp_type) VALUES (1001, '演示外包单位', 1, '1001', SYSTIMESTAMP, 1);
INSERT INTO smt_organize_relation (id, comp_name, park_id, comp_id, create_time, comp_type) VALUES (1002, '演示派遣单位', 1, '1002', SYSTIMESTAMP, 2);
INSERT INTO smt_staff (id, name, badge, comp_name, dep_name, status, emp_type, create_time) VALUES (10001, '演示正式员工', 'APP_EMPLOYEE', '演示公司', '研发部', 1, 1, SYSTIMESTAMP);
INSERT INTO smt_staff (id, name, badge, comp_id, comp_name, dep_name, status, emp_type, create_time) VALUES (10002, '演示外包人员', 'APP_OUTSOURCE', '1001', '演示外包单位', '外包组', 4, 10, SYSTIMESTAMP);
INSERT INTO smt_staff (id, name, badge, comp_id, comp_name, dep_name, status, emp_type, create_time) VALUES (10003, '演示派遣人员', 'APP_DISPATCH', '1002', '演示派遣单位', '派遣组', 4, 9, SYSTIMESTAMP);
INSERT INTO smt_staff (id, name, badge, comp_name, dep_name, status, emp_type, create_time) VALUES (10004, '演示主管', 'APP_SUPERVISOR', '演示公司', '保密管理部', 1, 1, SYSTIMESTAMP);
INSERT INTO smt_staff (id, name, badge, comp_id, comp_name, dep_name, status, emp_type, create_time) VALUES (10005, '演示安检员', 'APP_SECURITY', '1001', '演示外包单位', '安检组', 4, 10, SYSTIMESTAMP);
INSERT INTO smt_staff (id, name, badge, comp_id, comp_name, dep_name, status, emp_type, create_time) VALUES (10006, '演示押运人', 'APP_ESCORT', '1001', '演示外包单位', '安检组', 4, 10, SYSTIMESTAMP);

INSERT INTO smt_admittance_apply (id, park_id, visitor_name, visitor_phone, cert_no, status, start_time, end_time, receptionist_badge, receptionist_name, receptionist_phone, create_time, sms_code, company, person_type, cause, thing, area_type, apply_type)
-- 业务资格按 Asia/Shanghai 解释无时区的 TIMESTAMP；显式转为上海本地时刻，避免宿主/Oracle 时区改变有效期结果。
VALUES (810000001, 1, '演示供应商访客', '13800000000', '11010519491231002X', 0,
        CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Shanghai' AS TIMESTAMP) - INTERVAL '1' DAY,
        CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Shanghai' AS TIMESTAMP) + INTERVAL '1' DAY,
        'APP_EMPLOYEE', '演示正式员工', '13900000000', SYSTIMESTAMP, '123456', '演示供应商', 3, 1, 0, '1', 1);
INSERT INTO smt_admittance_fellow (id, visitor_id, fellow_name, cert_no, cert_type, is_main)
VALUES (900000001, 810000001, '演示供应商访客', '11010519491231002X', 0, 1);
COMMIT;
EXIT;
SQL
