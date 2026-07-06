-- 手工数据库变更：保密权限微信推送正文模板压缩到 20 字内（微信 thing 字段截断上限）。
-- 目标模式：tech_platform。
-- ！！执行窗口约束：必须与新代码同窗口生效（新旧代码占位符集合不同：
-- 旧={申请人}{OA单标题}{失败数量}{总数量}，新={成功数量}{总数量}），
-- 建议顺序：停 supplierAuthMsg 任务开关 → 发布应用 → 执行本脚本 → 开开关。
-- 脚本会先打印旧内容，请把输出记录到变更单，回滚时按记录值恢复。
-- ！！执行前必做第 0 步：先手工执行
--     SELECT TEMP_CONTENT FROM SMT_MSG_TEMPLATE WHERE TEMP_CODE = '11101';
--   把结果原文记入变更单（回滚唯一依据，勿依赖本脚本的 DBMS_OUTPUT）。
-- ！！DBeaver/DataGrip/SQL Developer 默认不显示 DBMS_OUTPUT，需先开启 server output
--   才能看到旧内容打印；未开启时本脚本的 PUT_LINE 静默无输出，故回滚留底以第 0 步为准。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_OLD_CONTENT SMT_MSG_TEMPLATE.TEMP_CONTENT%TYPE;
	V_COUNT NUMBER;
BEGIN
	SELECT COUNT(1) INTO V_COUNT
	FROM SMT_MSG_TEMPLATE
	WHERE TEMP_CODE = '11101';
	-- 快速失败：用 RAISE_APPLICATION_ERROR 而非 PUT_LINE+RETURN。
	-- 工具未开 server output 时 PUT_LINE 无输出，配 RETURN 会让脚本「成功完成」，
	-- 造成静默假成功（人以为已改，实则没找到目标行）。抛错才能硬中断并冒泡到执行人。
	IF V_COUNT = 0 THEN
		RAISE_APPLICATION_ERROR(-20001, '未找到 TEMP_CODE=11101（保密区门禁权限申请结果通知）的模板，请人工核实后再执行');
	END IF;
	-- 多行防呆：该 code 理论唯一，出现多行说明有脏数据，人工核实前拒绝执行
	-- （否则下方 SELECT INTO 会抛 ORA-01422 中断，报错信息易被误判为环境问题）
	IF V_COUNT > 1 THEN
		RAISE_APPLICATION_ERROR(-20002, 'TEMP_CODE=11101 存在 ' || V_COUNT || ' 行，疑似脏数据，请人工核实后再执行');
	END IF;

	-- 先打印旧内容供回滚记录（双保险，主依据仍是执行前第 0 步的手工 SELECT）。
	-- 注意：本脚本幂等重复执行时，此处 SELECT 到的已是新值，打印不再代表升级前原文，
	-- 故回滚一律以第 0 步记入变更单的原文为准。
	SELECT TEMP_CONTENT INTO V_OLD_CONTENT
	FROM SMT_MSG_TEMPLATE
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('旧模板内容（回滚依据，请记录到变更单）：' || V_OLD_CONTENT);

	UPDATE SMT_MSG_TEMPLATE
	SET TEMP_CONTENT = '保密权限下发完成 成功{成功数量}/共{总数量}'
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('已更新模板内容，影响行数：' || SQL%ROWCOUNT);

	COMMIT;
END;
