-- 员工通关权限有效期应用回滚前置检查
-- 执行条件：仅在需要回退本次应用版本时执行；由 DBA 在目标 Oracle schema 执行。
-- 影响表：只读检查 SMT_STAFF_DEVICE_AUTH，不删除日期字段或业务数据。
-- 回滚策略：旧版本不读取 START_TIME、END_TIME，字段保留以确保已经下发的有效期可在重新升级后恢复展示。

DECLARE
    v_table_count NUMBER;
    v_start_column_count NUMBER;
    v_end_column_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_table_count
    FROM USER_TABLES
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH';

    IF v_table_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20031, '缺少 SMT_STAFF_DEVICE_AUTH 表，无法完成回滚前置检查。');
    END IF;

    SELECT COUNT(*) INTO v_start_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH'
      AND COLUMN_NAME = 'START_TIME';

    SELECT COUNT(*) INTO v_end_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_STAFF_DEVICE_AUTH'
      AND COLUMN_NAME = 'END_TIME';

    IF v_start_column_count = 0 OR v_end_column_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20032, '权限有效期字段不存在，无需回滚本次字段变更。');
    END IF;

    DBMS_OUTPUT.PUT_LINE('回滚前置检查通过：START_TIME、END_TIME 将保留，避免丢失已配置的权限有效期。');
END;
/
