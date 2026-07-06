-- 手工数据库变更：保密权限微信推送失败重试计数（spec 2026-07-06-security-auth-wechat-msg-redesign §4）。
-- 目标模式：tech_platform（smt_* 业务表统一归属，用错账号列会加到错误模式）。
-- 本脚本可先于代码发布执行：DEFAULT 0，旧代码不感知新列，无兼容风险。
-- Oracle 低版本不支持 ADD COLUMN IF NOT EXISTS，使用 PL/SQL 匿名块做存在性判断。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_COUNT NUMBER;
BEGIN
	-- 检查 smt_security_auth_apply 表中 msg_retry_count 列是否存在
	SELECT COUNT(1) INTO V_COUNT
	FROM USER_TAB_COLUMNS
	WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_APPLY'
	  AND COLUMN_NAME = 'MSG_RETRY_COUNT';
	IF V_COUNT = 0 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE SMT_SECURITY_AUTH_APPLY ADD (MSG_RETRY_COUNT NUMBER(4) DEFAULT 0 NOT NULL)';
		DBMS_OUTPUT.PUT_LINE('已添加 SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列');
	ELSE
		DBMS_OUTPUT.PUT_LINE('SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列已存在，跳过');
	END IF;

	-- 列中文备注
	EXECUTE IMMEDIATE q'[COMMENT ON COLUMN SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT IS '微信推送失败次数，达上限（3）后 is_msg 置 2 失败放弃']';

	DBMS_OUTPUT.PUT_LINE('保密权限微信推送重试计数列初始化完成');
END;
