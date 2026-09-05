-- 保密区权限自动删除报表迁移共用的只读结构校验。
-- 本文件只由同目录 SQLPlus/SQLcl 脚本调用，不创建持久化过程或迁移账本。
WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
SET SQLBLANKLINES ON

DECLARE
    v_mode             VARCHAR2(32) := UPPER(TRIM('&&VALIDATION_MODE'));
    v_expected_schema  VARCHAR2(128) := UPPER(TRIM('&&EXPECTED_SCHEMA'));
    v_session_user     VARCHAR2(128) := UPPER(SYS_CONTEXT('USERENV', 'SESSION_USER'));
    v_current_schema   VARCHAR2(128) := UPPER(SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'));
    v_count            NUMBER;
    v_data_type        USER_TAB_COLUMNS.DATA_TYPE%TYPE;
    v_data_length      USER_TAB_COLUMNS.DATA_LENGTH%TYPE;
    v_char_length      USER_TAB_COLUMNS.CHAR_LENGTH%TYPE;
    v_char_used        USER_TAB_COLUMNS.CHAR_USED%TYPE;
    v_precision        USER_TAB_COLUMNS.DATA_PRECISION%TYPE;
    v_scale            USER_TAB_COLUMNS.DATA_SCALE%TYPE;
    v_nullable         USER_TAB_COLUMNS.NULLABLE%TYPE;
    v_constraint_type  USER_CONSTRAINTS.CONSTRAINT_TYPE%TYPE;
    v_constraint_status USER_CONSTRAINTS.STATUS%TYPE;
    v_constraint_validated USER_CONSTRAINTS.VALIDATED%TYPE;
    -- SEARCH_CONDITION 在部分 Oracle 版本是 LONG，使用 VARCHAR2 接收，避免绑定 VC 专有列类型。
    v_condition        VARCHAR2(4000);
    v_actual_columns   VARCHAR2(4000);
    -- DATA_DEFAULT 在旧版 Oracle 是 LONG，先接收 LONG 再截取到可比较的文本。
    v_default_long     LONG;
    v_default_value    VARCHAR2(4000);

    -- 将结构漂移转换为可定位的迁移错误并立即终止当前脚本。
    PROCEDURE stop_with(p_message VARCHAR2) IS
    BEGIN
        RAISE_APPLICATION_ERROR(-20901, p_message);
    END;

    -- 判断当前目标 schema 中是否存在指定表。
    FUNCTION table_exists(p_table_name VARCHAR2) RETURN BOOLEAN IS
        v_table_count NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_table_count
          FROM USER_TABLES
         WHERE TABLE_NAME = UPPER(p_table_name);
        RETURN v_table_count = 1;
    END;

    -- 判断指定表列是否已存在，供幂等补齐和只读校验复用。
    FUNCTION column_exists(p_table_name VARCHAR2, p_column_name VARCHAR2) RETURN BOOLEAN IS
        v_column_count NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_column_count
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = UPPER(p_table_name)
           AND COLUMN_NAME = UPPER(p_column_name);
        RETURN v_column_count = 1;
    END;

    -- 返回同名对象类型，避免把视图、同义词等对象误当作待创建的新表。
    FUNCTION object_type(p_object_name VARCHAR2) RETURN VARCHAR2 IS
        v_object_type USER_OBJECTS.OBJECT_TYPE%TYPE;
    BEGIN
        SELECT MAX(OBJECT_TYPE)
          INTO v_object_type
          FROM USER_OBJECTS
         WHERE OBJECT_NAME = UPPER(p_object_name);
        RETURN v_object_type;
    END;

    -- 仅去除检查约束中的空白和标识符双引号，保留其余语义字符。
    FUNCTION normalize_condition(p_condition VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN REGEXP_REPLACE(NVL(p_condition, ''), '[[:space:]"]', '');
    END;

    -- 按约束位置读取主键或唯一约束列顺序。
    FUNCTION key_columns(p_constraint_name VARCHAR2) RETURN VARCHAR2 IS
        v_columns VARCHAR2(4000);
    BEGIN
        SELECT LISTAGG(COLUMN_NAME, ',') WITHIN GROUP (ORDER BY POSITION)
          INTO v_columns
          FROM USER_CONS_COLUMNS
         WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);
        RETURN UPPER(REPLACE(v_columns, ' ', ''));
    END;

    -- 仅接受已启用且已验证的等价主键/唯一约束，避免把失效约束当作结构完成。
    FUNCTION equivalent_key_exists(
        p_table_name       VARCHAR2,
        p_constraint_type  VARCHAR2,
        p_expected_columns VARCHAR2
    ) RETURN BOOLEAN IS
        v_columns VARCHAR2(4000);
    BEGIN
        FOR r IN (
            SELECT CONSTRAINT_NAME
              FROM USER_CONSTRAINTS
             WHERE TABLE_NAME = UPPER(p_table_name)
               AND CONSTRAINT_TYPE = p_constraint_type
               AND STATUS = 'ENABLED'
               AND VALIDATED = 'VALIDATED'
        ) LOOP
            v_columns := key_columns(r.CONSTRAINT_NAME);
            IF v_columns = UPPER(REPLACE(p_expected_columns, ' ', '')) THEN
                RETURN TRUE;
            END IF;
        END LOOP;
        RETURN FALSE;
    END;

    -- 对本版本固定检查条件做保守的精确比较，不解释或重写 SQL 表达式。
    FUNCTION condition_matches(p_condition VARCHAR2, p_expected_token VARCHAR2) RETURN BOOLEAN IS
        v_normalized VARCHAR2(4000) := normalize_condition(p_condition);
    BEGIN
        IF p_expected_token = 'DRY_RUN' THEN
            RETURN v_normalized = 'DRY_RUNIN(0,1)';
        ELSIF p_expected_token = 'TASK_SOURCE' THEN
            RETURN v_normalized = 'TASK_SOURCEIN(''NORMAL'',''ISC'')';
        END IF;
        RETURN FALSE;
    END;

    -- 查找同表中已经启用且验证通过的等价检查约束。
    FUNCTION equivalent_check_exists(
        p_table_name       VARCHAR2,
        p_expected_token   VARCHAR2
    ) RETURN BOOLEAN IS
    BEGIN
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
            IF condition_matches(v_condition, p_expected_token) THEN
                RETURN TRUE;
            END IF;
        END LOOP;
        RETURN FALSE;
    END;

    -- 校验迁移依赖的既有基础表；缺表时禁止继续执行增量 DDL。
    PROCEDURE require_table(p_table_name VARCHAR2) IS
        v_object_type VARCHAR2(128);
    BEGIN
        IF NOT table_exists(p_table_name) THEN
            v_object_type := object_type(p_table_name);
            IF v_object_type IS NULL THEN
                stop_with('缺少基础表 ' || p_table_name || '，停止迁移。');
            ELSE
                stop_with('基础对象 ' || p_table_name || ' 的类型为 ' || v_object_type || '，不是表。');
            END IF;
        END IF;
    END;

    -- 校验列的类型、长度、精度和可空性；报告模式只报告缺列，漂移始终失败。
    PROCEDURE check_column(
        p_table_name    VARCHAR2,
        p_column_name   VARCHAR2,
        p_kind          VARCHAR2,
        p_length        NUMBER,
        p_precision     NUMBER,
        p_scale         NUMBER,
        p_expected_nullable VARCHAR2,
        p_report_missing BOOLEAN
    ) IS
    BEGIN
        IF NOT column_exists(p_table_name, p_column_name) THEN
            IF p_report_missing THEN
                IF p_expected_nullable = 'N' THEN
                    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || p_table_name INTO v_count;
                    IF v_count > 0 THEN
                        stop_with('表 ' || p_table_name || ' 已有数据，缺失的 NOT NULL 列 '
                            || p_column_name || ' 不能在无回填条件下补齐。');
                    END IF;
                END IF;
                DBMS_OUTPUT.PUT_LINE('待补齐列：' || p_table_name || '.' || p_column_name);
                RETURN;
            END IF;
            stop_with('缺少列 ' || p_table_name || '.' || p_column_name || '，停止迁移。');
        END IF;

        SELECT DATA_TYPE, DATA_LENGTH, CHAR_LENGTH, CHAR_USED,
               DATA_PRECISION, DATA_SCALE, NULLABLE
          INTO v_data_type, v_data_length, v_char_length, v_char_used,
               v_precision, v_scale, v_nullable
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = UPPER(p_table_name)
           AND COLUMN_NAME = UPPER(p_column_name);

        IF p_kind = 'NUMBER' THEN
            IF v_data_type <> 'NUMBER'
               OR NVL(v_precision, -1) <> p_precision
               OR NVL(v_scale, -1) <> p_scale THEN
                stop_with('列类型漂移：' || p_table_name || '.' || p_column_name
                    || '，期望 NUMBER(' || p_precision || ',' || p_scale || ')。');
            END IF;
        ELSIF p_kind = 'TEXT' THEN
            IF v_data_type <> 'VARCHAR2'
               OR NVL(v_char_length, -1) <> p_length
               OR NVL(v_char_used, '?') <> 'C' THEN
                stop_with('列类型漂移：' || p_table_name || '.' || p_column_name
                    || '，期望 VARCHAR2(' || p_length || ' CHAR)。');
            END IF;
        ELSIF p_kind = 'TIMESTAMP' THEN
            IF v_data_type NOT IN ('TIMESTAMP', 'TIMESTAMP(6)')
               OR NVL(v_scale, 6) <> 6 THEN
                stop_with('列类型漂移：' || p_table_name || '.' || p_column_name
                    || '，期望 TIMESTAMP。');
            END IF;
        ELSIF p_kind = 'BASE_NUMBER' THEN
            IF v_data_type <> 'NUMBER' THEN
                stop_with('基础列类型不兼容：' || p_table_name || '.' || p_column_name
                    || '，期望 NUMBER。');
            END IF;
        ELSIF p_kind = 'BASE_TEXT' THEN
            IF v_data_type NOT IN ('VARCHAR2', 'CHAR', 'CLOB') THEN
                stop_with('基础列类型不兼容：' || p_table_name || '.' || p_column_name
                    || '，期望文本类型。');
            END IF;
        ELSIF p_kind = 'BASE_TIME' THEN
            IF v_data_type = 'DATE' THEN
                NULL;
            ELSIF NOT REGEXP_LIKE(v_data_type, '^TIMESTAMP') THEN
                stop_with('基础列类型不兼容：' || p_table_name || '.' || p_column_name
                    || '，期望日期时间类型。');
            END IF;
        ELSE
            stop_with('迁移脚本内部错误：未知列类型检查 ' || p_kind || '。');
        END IF;

        IF p_expected_nullable IS NOT NULL
           AND v_nullable <> p_expected_nullable THEN
            stop_with('列约束漂移：' || p_table_name || '.' || p_column_name
                || '的可空性不符合版本定义。');
        END IF;
    END;

    -- 校验必要约束及其列顺序/条件，并拒绝禁用或未验证约束。
    PROCEDURE check_constraint(
        p_table_name       VARCHAR2,
        p_constraint_name  VARCHAR2,
        p_constraint_type  VARCHAR2,
        p_expected_columns VARCHAR2,
        p_expected_token   VARCHAR2,
        p_report_missing   BOOLEAN
    ) IS
        v_constraint_count NUMBER;
        v_table_name       USER_CONSTRAINTS.TABLE_NAME%TYPE;
        v_actual_condition VARCHAR2(4000);
    BEGIN
        SELECT COUNT(*), MAX(TABLE_NAME), MAX(CONSTRAINT_TYPE)
          INTO v_constraint_count, v_table_name, v_constraint_type
          FROM USER_CONSTRAINTS
         WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);

        IF v_constraint_count = 0 THEN
            IF p_constraint_type IN ('P', 'U')
               AND equivalent_key_exists(p_table_name, p_constraint_type, p_expected_columns) THEN
                DBMS_OUTPUT.PUT_LINE('已存在等价约束，跳过命名约束：' || p_table_name || '.' || p_constraint_name);
                RETURN;
            ELSIF p_constraint_type = 'C'
                  AND equivalent_check_exists(p_table_name, p_expected_token) THEN
                DBMS_OUTPUT.PUT_LINE('已存在等价检查约束，跳过命名约束：' || p_table_name || '.' || p_constraint_name);
                RETURN;
            ELSIF p_report_missing THEN
                DBMS_OUTPUT.PUT_LINE('待补齐约束：' || p_table_name || '.' || p_constraint_name);
                RETURN;
            ELSE
                stop_with('缺少约束 ' || p_table_name || '.' || p_constraint_name || '。');
            END IF;
        END IF;

        IF v_table_name <> UPPER(p_table_name) THEN
            stop_with('约束名冲突：' || p_constraint_name || ' 已被表 ' || v_table_name || ' 占用。');
        END IF;
        IF v_constraint_type <> p_constraint_type THEN
            stop_with('约束类型漂移：' || p_table_name || '.' || p_constraint_name || '。');
        END IF;

        SELECT STATUS, VALIDATED
          INTO v_constraint_status, v_constraint_validated
          FROM USER_CONSTRAINTS
         WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);
        IF v_constraint_status <> 'ENABLED' OR v_constraint_validated <> 'VALIDATED' THEN
            stop_with('约束未启用或未验证：' || p_table_name || '.' || p_constraint_name || '。');
        END IF;

        IF p_constraint_type IN ('P', 'U') THEN
            v_actual_columns := key_columns(p_constraint_name);
            IF v_actual_columns <> UPPER(REPLACE(p_expected_columns, ' ', '')) THEN
                stop_with('约束列漂移：' || p_table_name || '.' || p_constraint_name || '。');
            END IF;
        ELSIF p_constraint_type = 'C' THEN
            SELECT SEARCH_CONDITION
              INTO v_actual_condition
              FROM USER_CONSTRAINTS
             WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);
            IF NOT condition_matches(v_actual_condition, p_expected_token) THEN
                stop_with('检查约束条件漂移：' || p_table_name || '.' || p_constraint_name || '。');
            END IF;
        END IF;
    END;

    -- 在任何新增表/列之前检查保留约束名，避免把 ORA-00955/ORA-02264 推迟到 DDL 中途。
    PROCEDURE require_constraint_name_free(p_constraint_name VARCHAR2) IS
        v_constraint_count NUMBER;
        v_table_name USER_CONSTRAINTS.TABLE_NAME%TYPE;
    BEGIN
        SELECT COUNT(*), MAX(TABLE_NAME)
          INTO v_constraint_count, v_table_name
          FROM USER_CONSTRAINTS
         WHERE CONSTRAINT_NAME = UPPER(p_constraint_name);
        IF v_constraint_count > 0 THEN
            stop_with('保留约束名 ' || p_constraint_name || ' 已被表 ' || v_table_name || ' 占用。');
        END IF;
    END;

    -- 校验报表主表或任务关联表的固定列和约束，报告模式允许缺失项待补齐。
    PROCEDURE check_report_table(p_table_name VARCHAR2, p_report_missing BOOLEAN) IS
        v_object_type VARCHAR2(128);
    BEGIN
        IF NOT table_exists(p_table_name) THEN
            v_object_type := object_type(p_table_name);
            IF v_object_type IS NOT NULL THEN
                stop_with('报表对象 ' || p_table_name || ' 的类型为 ' || v_object_type || '，不是表。');
            ELSIF p_report_missing THEN
                IF p_table_name = 'SMT_SECURITY_AUTH_DELETE_LOG' THEN
                    require_constraint_name_free('PK_SEC_AUTH_DELETE_LOG');
                ELSE
                    require_constraint_name_free('PK_SEC_AUTH_DELETE_TASK');
                    require_constraint_name_free('UK_SEC_AUTH_DELETE_TASK');
                    require_constraint_name_free('CK_SEC_AUTH_TASK_SOURCE');
                END IF;
                DBMS_OUTPUT.PUT_LINE('待创建表：' || p_table_name);
                RETURN;
            END IF;
            stop_with('缺少报表表 ' || p_table_name || '。');
        END IF;

        IF p_table_name = 'SMT_SECURITY_AUTH_DELETE_LOG' THEN
            check_column(p_table_name, 'ID', 'NUMBER', NULL, 19, 0, 'N', p_report_missing);
            check_column(p_table_name, 'PARK_ID', 'NUMBER', NULL, 10, 0, 'N', p_report_missing);
            check_column(p_table_name, 'EXEC_TIME', 'TIMESTAMP', NULL, NULL, NULL, 'N', p_report_missing);
            check_column(p_table_name, 'STAFF_ID', 'NUMBER', NULL, 19, 0, 'Y', p_report_missing);
            check_column(p_table_name, 'STAFF_BADGE', 'TEXT', 64, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'STAFF_NAME', 'TEXT', 128, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'DEPARTMENT', 'TEXT', 256, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'AUTH_ID', 'NUMBER', NULL, 10, 0, 'Y', p_report_missing);
            check_column(p_table_name, 'AUTH_NAME', 'TEXT', 256, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'LAST_SNAP_TIME', 'TIMESTAMP', NULL, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'TRIGGER_REASON', 'TEXT', 256, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'RESULT', 'TEXT', 32, NULL, NULL, 'N', p_report_missing);
            check_column(p_table_name, 'REMARK', 'TEXT', 1000, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'CREATE_TIME', 'TIMESTAMP', NULL, NULL, NULL, 'N', p_report_missing);
            check_constraint(p_table_name, 'PK_SEC_AUTH_DELETE_LOG', 'P', 'ID', NULL, p_report_missing);
        ELSE
            check_column(p_table_name, 'ID', 'NUMBER', NULL, 19, 0, 'N', p_report_missing);
            check_column(p_table_name, 'LOG_ID', 'NUMBER', NULL, 19, 0, 'N', p_report_missing);
            check_column(p_table_name, 'TASK_SOURCE', 'TEXT', 16, NULL, NULL, 'N', p_report_missing);
            check_column(p_table_name, 'TASK_ID', 'NUMBER', NULL, 19, 0, 'N', p_report_missing);
            check_column(p_table_name, 'DEVICE_CODE', 'TEXT', 128, NULL, NULL, 'Y', p_report_missing);
            check_column(p_table_name, 'ACTION', 'NUMBER', NULL, 10, 0, 'Y', p_report_missing);
            check_constraint(p_table_name, 'PK_SEC_AUTH_DELETE_TASK', 'P', 'ID', NULL, p_report_missing);
            check_constraint(p_table_name, 'UK_SEC_AUTH_DELETE_TASK', 'U', 'LOG_ID,TASK_SOURCE,TASK_ID', NULL, p_report_missing);
            check_constraint(p_table_name, 'CK_SEC_AUTH_TASK_SOURCE', 'C', NULL, 'TASK_SOURCE', p_report_missing);
        END IF;
    END;

    -- 校验 DRY_RUN 列的兼容类型、默认字面量、数据值和检查约束。
    PROCEDURE check_dry_run(p_report_missing BOOLEAN) IS
    BEGIN
        IF NOT column_exists('SMT_SECURITY_AUTH_DELETE', 'DRY_RUN') THEN
            IF p_report_missing THEN
                require_constraint_name_free('CK_SEC_AUTH_DRY_RUN');
                DBMS_OUTPUT.PUT_LINE('待补齐列：SMT_SECURITY_AUTH_DELETE.DRY_RUN');
                RETURN;
            END IF;
            stop_with('缺少列 SMT_SECURITY_AUTH_DELETE.DRY_RUN。');
        END IF;

        check_column('SMT_SECURITY_AUTH_DELETE', 'DRY_RUN', 'NUMBER', NULL, 1, 0, 'Y', FALSE);
        -- 允许历史兼容列没有默认值；一旦声明默认值，必须仍为 0，避免旧代码把新配置默认为演练。
        BEGIN
            SELECT DATA_DEFAULT
              INTO v_default_long
              FROM USER_TAB_COLUMNS
             WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_DELETE'
               AND COLUMN_NAME = 'DRY_RUN';
            v_default_value := SUBSTR(v_default_long, 1, 4000);
        EXCEPTION
            WHEN OTHERS THEN
                stop_with('无法读取 SMT_SECURITY_AUTH_DELETE.DRY_RUN 默认值，停止迁移。');
        END;
        IF v_default_value IS NOT NULL THEN
            v_default_value := REGEXP_REPLACE(v_default_value, '[[:space:]]', '');
            IF v_default_value <> '0' THEN
                stop_with('SMT_SECURITY_AUTH_DELETE.DRY_RUN 默认值不是 0，禁止继续迁移。');
            END IF;
        END IF;
        EXECUTE IMMEDIATE
            'SELECT COUNT(*) FROM SMT_SECURITY_AUTH_DELETE '
            || 'WHERE DRY_RUN IS NOT NULL AND DRY_RUN NOT IN (0, 1)'
            INTO v_count;
        IF v_count > 0 THEN
            stop_with('SMT_SECURITY_AUTH_DELETE.DRY_RUN 存在非 0/1 数据，禁止继续迁移。');
        END IF;
        check_constraint('SMT_SECURITY_AUTH_DELETE', 'CK_SEC_AUTH_DRY_RUN', 'C', NULL,
                         'DRY_RUN', p_report_missing);
    END;

    -- 校验普通/ISC 任务表及旧配置表的基础列类型，不改变既有结构。
    PROCEDURE check_base_tables IS
    BEGIN
        require_table('SMT_SECURITY_AUTH_DELETE');
        check_column('SMT_SECURITY_AUTH_DELETE', 'ID', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_SECURITY_AUTH_DELETE', 'PARK_ID', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_SECURITY_AUTH_DELETE', 'DELETE_DAY', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);

        require_table('SMT_DEVICE_TASK');
        check_column('SMT_DEVICE_TASK', 'ID', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'DEVICE_CODE', 'BASE_TEXT', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'ACTION', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'STATUS', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'CODE', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'REMARK', 'BASE_TEXT', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'CREATE_TIME', 'BASE_TIME', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_DEVICE_TASK', 'UPDATE_TIME', 'BASE_TIME', NULL, NULL, NULL, NULL, FALSE);

        require_table('SMT_ISC_DEVICE_TASK');
        check_column('SMT_ISC_DEVICE_TASK', 'ID', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'DEVICE_CODE', 'BASE_TEXT', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'ACTION', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'STATUS', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'CODE', 'BASE_NUMBER', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'REMARK', 'BASE_TEXT', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'CREATE_TIME', 'BASE_TIME', NULL, NULL, NULL, NULL, FALSE);
        check_column('SMT_ISC_DEVICE_TASK', 'UPDATE_TIME', 'BASE_TIME', NULL, NULL, NULL, NULL, FALSE);
    END;
BEGIN
    IF v_expected_schema IS NULL
       OR NOT REGEXP_LIKE(v_expected_schema, '^[A-Z][A-Z0-9_$#]*$') THEN
        stop_with('必须通过 @脚本.sql EXPECTED_SCHEMA 显式传入合法目标 schema。');
    END IF;
    IF v_expected_schema IN ('SYS', 'SYSTEM')
       OR v_session_user IN ('SYS', 'SYSTEM')
       OR v_current_schema IN ('SYS', 'SYSTEM') THEN
        stop_with('禁止在 SYS/SYSTEM 或其当前 schema 上执行本迁移。');
    END IF;
    IF v_session_user <> v_expected_schema OR v_current_schema <> v_expected_schema THEN
        stop_with('目标 schema 不匹配：SESSION_USER=' || v_session_user
            || '，CURRENT_SCHEMA=' || v_current_schema
            || '，EXPECTED_SCHEMA=' || v_expected_schema || '。');
    END IF;

    check_base_tables;

    IF v_mode = 'BASE' THEN
        DBMS_OUTPUT.PUT_LINE('目标 schema 与基础表只读检查通过：' || v_expected_schema);
    ELSIF v_mode = 'REPORT_EXISTING' THEN
        check_dry_run(TRUE);
        check_report_table('SMT_SECURITY_AUTH_DELETE_LOG', TRUE);
        check_report_table('SMT_SECURITY_AUTH_DELETE_TASK', TRUE);
        DBMS_OUTPUT.PUT_LINE('已有结构兼容性只读检查通过；缺失项将由升级脚本补齐。');
    ELSIF v_mode = 'FINAL' THEN
        check_dry_run(FALSE);
        check_report_table('SMT_SECURITY_AUTH_DELETE_LOG', FALSE);
        check_report_table('SMT_SECURITY_AUTH_DELETE_TASK', FALSE);
        DBMS_OUTPUT.PUT_LINE('最终列与约束只读检查通过：' || v_expected_schema);
    ELSE
        stop_with('迁移脚本内部错误：未知校验模式 ' || v_mode || '。');
    END IF;
END;
/
