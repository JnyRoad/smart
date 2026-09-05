-- 保密区权限自动删除报表应用回滚前只读检查。
-- 用法：@rollback_check.sql EXPECTED_SCHEMA
-- 本脚本绝不删除新增表/列/约束，也不修改演练配置、权限、任务或审计数据。
SET SERVEROUTPUT ON SIZE UNLIMITED
SET SQLBLANKLINES ON
SET VERIFY OFF
SET FEEDBACK ON
SET DEFINE ON
WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DEFINE EXPECTED_SCHEMA = &1
DEFINE VALIDATION_MODE = FINAL

@@validation.sql

DECLARE
    v_log_count      NUMBER;
    v_task_count     NUMBER;
    v_dry_run_count  NUMBER;
BEGIN
    -- 只读记录当前新增审计数据规模，便于发布记录核对，不建立外部账本。
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SECURITY_AUTH_DELETE_LOG'
        INTO v_log_count;
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SECURITY_AUTH_DELETE_TASK'
        INTO v_task_count;
    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM SMT_SECURITY_AUTH_DELETE WHERE DRY_RUN = 1'
        INTO v_dry_run_count;

    DBMS_OUTPUT.PUT_LINE('保留审计主表记录数：' || v_log_count);
    DBMS_OUTPUT.PUT_LINE('保留任务关联记录数：' || v_task_count);

    IF v_dry_run_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20931,
            '发现 DRY_RUN=1 的演练配置，禁止直接恢复旧版自动调度；旧版忽略演练标识，可能执行真实删权。');
    END IF;

    DBMS_OUTPUT.PUT_LINE('回滚检查通过：新增表、列、约束和已有数据均保留；仅可在确认调度停用及版本切换后回滚应用。');
END;
/

PROMPT
PROMPT 回滚检查为只读门禁，未执行 DROP、DELETE、UPDATE 或旧版调度恢复。
