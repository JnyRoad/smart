-- 回滚脚本：恢复保密权限微信推送正文模板为升级前内容。
-- 目标模式：tech_platform。
-- ！！旧内容以升级脚本执行时 DBMS_OUTPUT 打印并记录在变更单里的值为准，
-- 执行前把下方 V_OLD_CONTENT 的占位值替换为变更单记录的原文。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	-- TODO(执行人)：替换为变更单记录的升级前模板原文
	V_OLD_CONTENT SMT_MSG_TEMPLATE.TEMP_CONTENT%TYPE := '<替换为变更单记录的升级前模板原文>';
BEGIN
	IF V_OLD_CONTENT = '<替换为变更单记录的升级前模板原文>' THEN
		DBMS_OUTPUT.PUT_LINE('未替换旧内容占位值，拒绝执行：请先从变更单取回升级前模板原文');
		RETURN;
	END IF;

	UPDATE SMT_MSG_TEMPLATE
	SET TEMP_CONTENT = V_OLD_CONTENT
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('已恢复模板内容，影响行数：' || SQL%ROWCOUNT);

	COMMIT;
END;
