-- 手工数据库变更：将管理后台菜单 "客户端管理" 改名为 "应用管理"（开放 API 鉴权规范要求）。
-- 表 SYS_MENU，列 name；脚本可重复执行。
-- 当前平台没有数据库自动迁移机制；本脚本使用 PL/SQL 匿名块做菜单存在性判断。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_COUNT NUMBER;
	V_UPDATED NUMBER;
BEGIN
	-- 查询菜单名称为 "客户端管理" 的记录数
	SELECT COUNT(1) INTO V_COUNT FROM SYS_MENU WHERE NAME = '客户端管理' AND DEL_FLAG = '0';

	IF V_COUNT > 0 THEN
		-- 更新菜单名称为 "应用管理"
		UPDATE SYS_MENU SET NAME = '应用管理', UPDATE_TIME = SYSDATE WHERE NAME = '客户端管理' AND DEL_FLAG = '0';
		V_UPDATED := SQL%ROWCOUNT;
		DBMS_OUTPUT.PUT_LINE('已更新 ' || V_UPDATED || ' 条菜单记录（客户端管理 -> 应用管理）');
	ELSE
		-- 未找到 "客户端管理" 菜单，打印提示信息，不报错（幂等安全）
		DBMS_OUTPUT.PUT_LINE('未找到，可能已改名或菜单名不同，请人工核对 SYS_MENU 中指向 /admin/client 页面的菜单行');
	END IF;

	-- 提交事务
	COMMIT;
END;
