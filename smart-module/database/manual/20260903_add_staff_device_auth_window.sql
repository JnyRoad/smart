-- 员工通关权限有效期字段
-- 执行条件：应用版本已包含 SMT_STAFF_DEVICE_AUTH 的起止日期映射及手动下发日期校验；由 DBA 在目标 Oracle schema 执行。
-- 影响表：SMT_STAFF_DEVICE_AUTH。
-- 可重复性：字段不存在时新增；已有字段时仅回填空值，并在发现倒置的历史有效期时停止执行。

DECLARE
    v_table_count NUMBER;
    v_start_column_count NUMBER;
    v_end_column_count NUMBER;
    v_invalid_window_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_table_count
    FROM USER_TABLES
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH';

    IF v_table_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20021, '缺少 SMT_STAFF_DEVICE_AUTH 表，停止执行权限有效期字段变更。');
    END IF;

    SELECT COUNT(*) INTO v_start_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH'
      AND COLUMN_NAME = 'START_TIME';

    IF v_start_column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE SMT_STAFF_DEVICE_AUTH ADD (START_TIME DATE)';
    END IF;

    SELECT COUNT(*) INTO v_end_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH'
      AND COLUMN_NAME = 'END_TIME';

    IF v_end_column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE SMT_STAFF_DEVICE_AUTH ADD (END_TIME DATE)';
    END IF;

    -- 旧关联没有有效期概念：保留创建日作为生效日，并以产品默认的 2030-12-31 作为失效日。
    -- 动态 SQL 避免匿名块在字段刚新增时于编译期触发 ORA-00904。
    EXECUTE IMMEDIATE 'UPDATE SMT_STAFF_DEVICE_AUTH '
            || 'SET START_TIME = TRUNC(NVL(CREATE_TIME, SYSDATE)) '
            || 'WHERE START_TIME IS NULL';
    EXECUTE IMMEDIATE 'UPDATE SMT_STAFF_DEVICE_AUTH '
            || 'SET END_TIME = TO_DATE(''2030-12-31 23:59:59'', ''yyyy-mm-dd hh24:mi:ss'') '
            || 'WHERE END_TIME IS NULL';

    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE END_TIME < START_TIME'
        INTO v_invalid_window_count;

    IF v_invalid_window_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20022, 'SMT_STAFF_DEVICE_AUTH 存在结束时间早于开始时间的记录，停止执行。');
    END IF;

    COMMIT;
END;
/
