-- 回滚脚本：恢复保密权限微信推送正文模板为升级前内容。
-- 目标模式：tech_platform。
-- ！！旧内容以升级脚本执行时 DBMS_OUTPUT 打印并记录在变更单里的值为准，
-- 执行前把下方 V_OLD_CONTENT 的占位值替换为变更单记录的原文。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	-- TODO(执行人)：替换为变更单记录的升级前模板原文
	V_OLD_CONTENT SMT_MSG_TEMPLATE.TEMP_CONTENT%TYPE := '<替换为变更单记录的升级前模板原文>';
BEGIN
	-- 护栏：占位值未替换、或误置为空（Oracle 里空串即 NULL）时一律拒绝执行。
	-- 原写法只用 = 等值判断：V_OLD_CONTENT 为 NULL 时该比较结果为 NULL（非 TRUE），
	-- 会绕过护栏继续执行，把模板 TEMP_CONTENT 置 NULL 后 COMMIT，造成不可逆脏数据。
	-- 故补 IS NULL 分支，并改 RAISE_APPLICATION_ERROR 硬中断（未开 server output 时
	-- PUT_LINE+RETURN 会静默「成功完成」，掩盖占位值未替换的错误）。
	IF V_OLD_CONTENT IS NULL OR V_OLD_CONTENT = '<替换为变更单记录的升级前模板原文>' THEN
		RAISE_APPLICATION_ERROR(-20001, '未替换旧内容占位值（或误置为空），拒绝执行：请先从变更单取回升级前模板原文');
	END IF;

	UPDATE SMT_MSG_TEMPLATE
	SET TEMP_CONTENT = V_OLD_CONTENT
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('已恢复模板内容，影响行数：' || SQL%ROWCOUNT);

	COMMIT;
END;
