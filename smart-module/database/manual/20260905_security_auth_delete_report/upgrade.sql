-- 保密区权限自动删除报表 Oracle 增量升级脚本。
-- 用法：@upgrade.sql EXPECTED_SCHEMA
-- 执行前必须先停用相关自动调度并完成 precheck.sql；本脚本不回填历史、不修改配置/权限/任务数据。
-- Oracle DDL 会隐式提交，脚本失败时 ROLLBACK 不能撤销已经成功创建的对象；请按输出复核后幂等重跑。
SET SERVEROUTPUT ON SIZE UNLIMITED
SET SQLBLANKLINES ON
SET VERIFY OFF
SET FEEDBACK ON
SET DEFINE ON
WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DEFINE EXPECTED_SCHEMA = &1
DEFINE VALIDATION_MODE = REPORT_EXISTING

-- 先只读核对基础表及已有新增结构；类型或约束漂移在任何 DDL 前明确终止。
@@validation.sql

PROMPT
PROMPT 开始补齐 SMT_SECURITY_AUTH_DELETE.DRY_RUN 及报表表结构。

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_DELETE'
       AND COLUMN_NAME = 'DRY_RUN';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE SMT_SECURITY_AUTH_DELETE ADD (DRY_RUN NUMBER(1) DEFAULT 0)';
        DBMS_OUTPUT.PUT_LINE('已新增列 SMT_SECURITY_AUTH_DELETE.DRY_RUN。');
    ELSE
        DBMS_OUTPUT.PUT_LINE('列 SMT_SECURITY_AUTH_DELETE.DRY_RUN 已存在，跳过新增。');
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TABLES
     WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_DELETE_LOG';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE q'[
            CREATE TABLE SMT_SECURITY_AUTH_DELETE_LOG (
                ID NUMBER(19) NOT NULL,
                PARK_ID NUMBER(10) NOT NULL,
                EXEC_TIME TIMESTAMP NOT NULL,
                STAFF_ID NUMBER(19),
                STAFF_BADGE VARCHAR2(64 CHAR),
                STAFF_NAME VARCHAR2(128 CHAR),
                DEPARTMENT VARCHAR2(256 CHAR),
                AUTH_ID NUMBER(10),
                AUTH_NAME VARCHAR2(256 CHAR),
                LAST_SNAP_TIME TIMESTAMP,
                TRIGGER_REASON VARCHAR2(256 CHAR),
                RESULT VARCHAR2(32 CHAR) NOT NULL,
                REMARK VARCHAR2(1000 CHAR),
                CREATE_TIME TIMESTAMP NOT NULL,
                CONSTRAINT PK_SEC_AUTH_DELETE_LOG PRIMARY KEY (ID)
            )
        ]';
        DBMS_OUTPUT.PUT_LINE('已创建表 SMT_SECURITY_AUTH_DELETE_LOG。');
    ELSE
        DBMS_OUTPUT.PUT_LINE('表 SMT_SECURITY_AUTH_DELETE_LOG 已存在，保留原表及数据。');
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TABLES
     WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_DELETE_TASK';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE q'[
            CREATE TABLE SMT_SECURITY_AUTH_DELETE_TASK (
                ID NUMBER(19) NOT NULL,
                LOG_ID NUMBER(19) NOT NULL,
                TASK_SOURCE VARCHAR2(16 CHAR) NOT NULL,
                TASK_ID NUMBER(19) NOT NULL,
                DEVICE_CODE VARCHAR2(128 CHAR),
                ACTION NUMBER(10),
                CONSTRAINT PK_SEC_AUTH_DELETE_TASK PRIMARY KEY (ID),
                CONSTRAINT UK_SEC_AUTH_DELETE_TASK UNIQUE (LOG_ID, TASK_SOURCE, TASK_ID),
                CONSTRAINT CK_SEC_AUTH_TASK_SOURCE CHECK (TASK_SOURCE IN ('NORMAL', 'ISC'))
            )
        ]';
        DBMS_OUTPUT.PUT_LINE('已创建表 SMT_SECURITY_AUTH_DELETE_TASK。');
    ELSE
        DBMS_OUTPUT.PUT_LINE('表 SMT_SECURITY_AUTH_DELETE_TASK 已存在，保留原表及数据。');
    END IF;
END;
/

-- 对已存在但不完整的兼容表只补缺失列；已有数据时不强行回填 NOT NULL 列。
DECLARE
    c_table CONSTANT VARCHAR2(64) := 'SMT_SECURITY_AUTH_DELETE_LOG';
    v_count NUMBER;
    v_rows  NUMBER;

    -- 在目标表为空时补齐缺失列；固定定义不接受外部列名或 SQL 输入。
    PROCEDURE add_column(p_name VARCHAR2, p_definition VARCHAR2, p_not_null BOOLEAN) IS
    BEGIN
        SELECT COUNT(*)
          INTO v_count
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = c_table
           AND COLUMN_NAME = UPPER(p_name);
        IF v_count = 0 THEN
            IF p_not_null THEN
                EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || c_table INTO v_rows;
                IF v_rows > 0 THEN
                    RAISE_APPLICATION_ERROR(-20911,
                        '表 ' || c_table || ' 已有数据，无法无回填新增 NOT NULL 列 ' || p_name || '。');
                END IF;
            END IF;
            EXECUTE IMMEDIATE 'ALTER TABLE ' || c_table || ' ADD (' || p_definition || ')';
            DBMS_OUTPUT.PUT_LINE('已补齐列：' || c_table || '.' || p_name);
        END IF;
    END;
BEGIN
    add_column('ID', 'ID NUMBER(19) NOT NULL', TRUE);
    add_column('PARK_ID', 'PARK_ID NUMBER(10) NOT NULL', TRUE);
    add_column('EXEC_TIME', 'EXEC_TIME TIMESTAMP NOT NULL', TRUE);
    add_column('STAFF_ID', 'STAFF_ID NUMBER(19)', FALSE);
    add_column('STAFF_BADGE', 'STAFF_BADGE VARCHAR2(64 CHAR)', FALSE);
    add_column('STAFF_NAME', 'STAFF_NAME VARCHAR2(128 CHAR)', FALSE);
    add_column('DEPARTMENT', 'DEPARTMENT VARCHAR2(256 CHAR)', FALSE);
    add_column('AUTH_ID', 'AUTH_ID NUMBER(10)', FALSE);
    add_column('AUTH_NAME', 'AUTH_NAME VARCHAR2(256 CHAR)', FALSE);
    add_column('LAST_SNAP_TIME', 'LAST_SNAP_TIME TIMESTAMP', FALSE);
    add_column('TRIGGER_REASON', 'TRIGGER_REASON VARCHAR2(256 CHAR)', FALSE);
    add_column('RESULT', 'RESULT VARCHAR2(32 CHAR) NOT NULL', TRUE);
    add_column('REMARK', 'REMARK VARCHAR2(1000 CHAR)', FALSE);
    add_column('CREATE_TIME', 'CREATE_TIME TIMESTAMP NOT NULL', TRUE);
END;
/

DECLARE
    c_table CONSTANT VARCHAR2(64) := 'SMT_SECURITY_AUTH_DELETE_TASK';
    v_count NUMBER;
    v_rows  NUMBER;

    -- 在目标表为空时补齐缺失列；固定定义不接受外部列名或 SQL 输入。
    PROCEDURE add_column(p_name VARCHAR2, p_definition VARCHAR2, p_not_null BOOLEAN) IS
    BEGIN
        SELECT COUNT(*)
          INTO v_count
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = c_table
           AND COLUMN_NAME = UPPER(p_name);
        IF v_count = 0 THEN
            IF p_not_null THEN
                EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || c_table INTO v_rows;
                IF v_rows > 0 THEN
                    RAISE_APPLICATION_ERROR(-20912,
                        '表 ' || c_table || ' 已有数据，无法无回填新增 NOT NULL 列 ' || p_name || '。');
                END IF;
            END IF;
            EXECUTE IMMEDIATE 'ALTER TABLE ' || c_table || ' ADD (' || p_definition || ')';
            DBMS_OUTPUT.PUT_LINE('已补齐列：' || c_table || '.' || p_name);
        END IF;
    END;
BEGIN
    add_column('ID', 'ID NUMBER(19) NOT NULL', TRUE);
    add_column('LOG_ID', 'LOG_ID NUMBER(19) NOT NULL', TRUE);
    add_column('TASK_SOURCE', 'TASK_SOURCE VARCHAR2(16 CHAR) NOT NULL', TRUE);
    add_column('TASK_ID', 'TASK_ID NUMBER(19) NOT NULL', TRUE);
    add_column('DEVICE_CODE', 'DEVICE_CODE VARCHAR2(128 CHAR)', FALSE);
    add_column('ACTION', 'ACTION NUMBER(10)', FALSE);
END;
/

-- 只在缺失且没有等价约束时补约束；同名或同语义但定义漂移的约束直接失败。
DECLARE
    v_count       NUMBER;
    v_table_name  USER_CONSTRAINTS.TABLE_NAME%TYPE;
    v_type        USER_CONSTRAINTS.CONSTRAINT_TYPE%TYPE;
    -- SEARCH_CONDITION 在部分 Oracle 版本是 LONG，使用 VARCHAR2 接收。
    v_condition   VARCHAR2(4000);
    v_columns     VARCHAR2(4000);
    v_status      USER_CONSTRAINTS.STATUS%TYPE;
    v_validated   USER_CONSTRAINTS.VALIDATED%TYPE;

    -- 将约束漂移转换为可定位的迁移错误并立即终止当前脚本。
    PROCEDURE stop_with(p_message VARCHAR2) IS
    BEGIN
        RAISE_APPLICATION_ERROR(-20913, p_message);
    END;

    -- 仅去除空白和标识符双引号，保留运算符、括号、逗号及字符串大小写。
    FUNCTION normalize_condition(p_condition VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN REGEXP_REPLACE(NVL(p_condition, ''), '[[:space:]"]', '');
    END;

    -- 按约束位置读取主键或唯一约束列顺序。
    FUNCTION key_columns(p_constraint_name VARCHAR2) RETURN VARCHAR2 IS
        v_result VARCHAR2(4000);
    BEGIN
        SELECT LISTAGG(COLUMN_NAME, ',') WITHIN GROUP (ORDER BY POSITION)
          INTO v_result
          FROM USER_CONS_COLUMNS
         WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);
        RETURN UPPER(REPLACE(v_result, ' ', ''));
    END;

    -- 查找同表中已启用且验证通过的等价主键或唯一约束。
    FUNCTION equivalent_key(p_table_name VARCHAR2, p_type VARCHAR2, p_columns VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        FOR r IN (
            SELECT CONSTRAINT_NAME
              FROM USER_CONSTRAINTS
             WHERE TABLE_NAME = UPPER(p_table_name)
               AND CONSTRAINT_TYPE = p_type
               AND STATUS = 'ENABLED'
               AND VALIDATED = 'VALIDATED'
        ) LOOP
            IF key_columns(r.CONSTRAINT_NAME) = UPPER(REPLACE(p_columns, ' ', '')) THEN
                RETURN TRUE;
            END IF;
        END LOOP;
        RETURN FALSE;
    END;

    -- 查找同表中已启用且验证通过的等价检查约束。
    FUNCTION equivalent_check(p_table_name VARCHAR2, p_token VARCHAR2) RETURN BOOLEAN IS
        v_expected VARCHAR2(128);
    BEGIN
        IF p_token = 'DRY_RUN' THEN
            v_expected := 'DRY_RUNIN(0,1)';
        ELSE
            v_expected := 'TASK_SOURCEIN(''NORMAL'',''ISC'')';
        END IF;
        FOR r IN (
            SELECT CONSTRAINT_NAME
              FROM USER_CONSTRAINTS
             WHERE TABLE_NAME = UPPER(p_table_name)
               AND CONSTRAINT_TYPE = 'C'
               AND STATUS = 'ENABLED'
               AND VALIDATED = 'VALIDATED'
        ) LOOP
            SELECT SEARCH_CONDITION
              INTO v_condition
              FROM USER_CONSTRAINTS
             WHERE CONSTRAINT_NAME = r.CONSTRAINT_NAME;
            IF normalize_condition(v_condition) = v_expected THEN
                RETURN TRUE;
            END IF;
        END LOOP;
        RETURN FALSE;
    END;

    -- 幂等补齐主键/唯一约束；同名或同语义漂移直接失败。
    PROCEDURE ensure_key(
        p_table_name VARCHAR2,
        p_name       VARCHAR2,
        p_type       VARCHAR2,
        p_columns    VARCHAR2,
        p_ddl        VARCHAR2
    ) IS
    BEGIN
        SELECT COUNT(*), MAX(TABLE_NAME), MAX(CONSTRAINT_TYPE)
          INTO v_count, v_table_name, v_type
          FROM USER_CONSTRAINTS
         WHERE CONSTRAINT_NAME = UPPER(p_name);
        IF v_count > 0 THEN
            IF v_table_name <> UPPER(p_table_name) OR v_type <> p_type THEN
                stop_with('约束名或类型漂移：' || p_table_name || '.' || p_name || '。');
            END IF;
            SELECT STATUS, VALIDATED
              INTO v_status, v_validated
              FROM USER_CONSTRAINTS
             WHERE CONSTRAINT_NAME = UPPER(p_name);
            IF v_status <> 'ENABLED' OR v_validated <> 'VALIDATED' THEN
                stop_with('约束未启用或未验证：' || p_table_name || '.' || p_name || '。');
            END IF;
            v_columns := key_columns(p_name);
            IF v_columns <> UPPER(REPLACE(p_columns, ' ', '')) THEN
                stop_with('约束列漂移：' || p_table_name || '.' || p_name || '。');
            END IF;
            DBMS_OUTPUT.PUT_LINE('约束已存在，跳过：' || p_table_name || '.' || p_name);
        ELSIF equivalent_key(p_table_name, p_type, p_columns) THEN
            DBMS_OUTPUT.PUT_LINE('已存在等价约束，跳过：' || p_table_name || '.' || p_name);
        ELSE
            EXECUTE IMMEDIATE p_ddl;
            DBMS_OUTPUT.PUT_LINE('已新增约束：' || p_table_name || '.' || p_name);
        END IF;
    END;

    -- 幂等补齐检查约束；禁用或未验证的约束不视为可兼容结构。
    PROCEDURE ensure_check(
        p_table_name VARCHAR2,
        p_name       VARCHAR2,
        p_token      VARCHAR2,
        p_ddl        VARCHAR2
    ) IS
        v_expected VARCHAR2(128);
    BEGIN
        IF p_token = 'DRY_RUN' THEN
            v_expected := 'DRY_RUNIN(0,1)';
        ELSE
            v_expected := 'TASK_SOURCEIN(''NORMAL'',''ISC'')';
        END IF;
        SELECT COUNT(*), MAX(TABLE_NAME), MAX(CONSTRAINT_TYPE)
          INTO v_count, v_table_name, v_type
          FROM USER_CONSTRAINTS
         WHERE CONSTRAINT_NAME = UPPER(p_name);
        IF v_count > 0 THEN
            IF v_table_name <> UPPER(p_table_name) OR v_type <> 'C' THEN
                stop_with('检查约束名或类型漂移：' || p_table_name || '.' || p_name || '。');
            END IF;
            SELECT STATUS, VALIDATED
              INTO v_status, v_validated
              FROM USER_CONSTRAINTS
             WHERE CONSTRAINT_NAME = UPPER(p_name);
            IF v_status <> 'ENABLED' OR v_validated <> 'VALIDATED' THEN
                stop_with('检查约束未启用或未验证：' || p_table_name || '.' || p_name || '。');
            END IF;
            SELECT SEARCH_CONDITION
              INTO v_condition
              FROM USER_CONSTRAINTS
             WHERE CONSTRAINT_NAME = UPPER(p_name);
            IF normalize_condition(v_condition) <> v_expected THEN
                stop_with('检查约束条件漂移：' || p_table_name || '.' || p_name || '。');
            END IF;
            DBMS_OUTPUT.PUT_LINE('约束已存在，跳过：' || p_table_name || '.' || p_name);
        ELSIF equivalent_check(p_table_name, p_token) THEN
            DBMS_OUTPUT.PUT_LINE('已存在等价检查约束，跳过：' || p_table_name || '.' || p_name);
        ELSE
            EXECUTE IMMEDIATE p_ddl;
            DBMS_OUTPUT.PUT_LINE('已新增约束：' || p_table_name || '.' || p_name);
        END IF;
    END;
BEGIN
    ensure_check(
        'SMT_SECURITY_AUTH_DELETE',
        'CK_SEC_AUTH_DRY_RUN',
        'DRY_RUN',
        'ALTER TABLE SMT_SECURITY_AUTH_DELETE ADD CONSTRAINT CK_SEC_AUTH_DRY_RUN CHECK (DRY_RUN IN (0, 1))'
    );
    ensure_key(
        'SMT_SECURITY_AUTH_DELETE_LOG',
        'PK_SEC_AUTH_DELETE_LOG',
        'P',
        'ID',
        'ALTER TABLE SMT_SECURITY_AUTH_DELETE_LOG ADD CONSTRAINT PK_SEC_AUTH_DELETE_LOG PRIMARY KEY (ID)'
    );
    ensure_key(
        'SMT_SECURITY_AUTH_DELETE_TASK',
        'PK_SEC_AUTH_DELETE_TASK',
        'P',
        'ID',
        'ALTER TABLE SMT_SECURITY_AUTH_DELETE_TASK ADD CONSTRAINT PK_SEC_AUTH_DELETE_TASK PRIMARY KEY (ID)'
    );
    ensure_key(
        'SMT_SECURITY_AUTH_DELETE_TASK',
        'UK_SEC_AUTH_DELETE_TASK',
        'U',
        'LOG_ID,TASK_SOURCE,TASK_ID',
        'ALTER TABLE SMT_SECURITY_AUTH_DELETE_TASK ADD CONSTRAINT UK_SEC_AUTH_DELETE_TASK UNIQUE (LOG_ID, TASK_SOURCE, TASK_ID)'
    );
    ensure_check(
        'SMT_SECURITY_AUTH_DELETE_TASK',
        'CK_SEC_AUTH_TASK_SOURCE',
        'TASK_SOURCE',
        'ALTER TABLE SMT_SECURITY_AUTH_DELETE_TASK ADD CONSTRAINT CK_SEC_AUTH_TASK_SOURCE CHECK (TASK_SOURCE IN (''NORMAL'', ''ISC''))'
    );
END;
/

-- 最终校验再次只读核对所有列和必要约束；同一版本可安全重复执行。
@@verify.sql &&EXPECTED_SCHEMA

PROMPT
PROMPT 升级脚本完成。未执行历史数据回填、配置/权限/任务更新、候选索引或序列创建。
