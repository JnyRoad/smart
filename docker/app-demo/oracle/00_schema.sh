#!/bin/sh
set -eu

# Oracle Free 的 .sql 初始化脚本默认以系统用户运行；演示业务表必须明确属于 APP_USER。
schema_file=/container-entrypoint-initdb.d/schema.sql.dat
if [ ! -r "$schema_file" ]; then
  echo 'schema.sql.dat 不存在或不可读' >&2
  exit 1
fi

sqlplus -s /nolog <<SQL
whenever oserror exit failure rollback
whenever sqlerror exit sql.sqlcode rollback
CONNECT ${APP_USER}/"${APP_USER_PASSWORD}"@//localhost:1521/FREEPDB1
@${schema_file}
exit success
SQL
