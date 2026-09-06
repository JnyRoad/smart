#!/bin/sh
set -eu

# Oracle Free 的 .sql 初始化脚本默认以系统用户运行；演示业务表必须明确属于 APP_USER。
sqlplus -s "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/FREEPDB1" <<'SQL'
whenever sqlerror exit sql.sqlcode rollback
@/container-entrypoint-initdb.d/schema.sql.dat
exit success
SQL
