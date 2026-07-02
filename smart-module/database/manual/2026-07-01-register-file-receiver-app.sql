-- 注册许昌 FileReceiver 开放应用（App ID: file-receiver-xc）
-- 部署后必须立即通过管理页「重置 App Secret」生成正式凭证
-- 占位符 <许昌园区ID> 执行前由运维查 smt_park 表确认填入
-- 警告：占位符必须替换为合法 JSON 数值；若未替换，脚本会主动报错终止（这是保护，不是故障）

DECLARE
	V_EXISTING_COUNT NUMBER;
	V_ADDITIONAL_INFO VARCHAR2(200) := '{"allowedParkIds":[<许昌园区ID>]}';
BEGIN
	-- 硬校验：占位符未替换时直接报错终止，防止写入非法 JSON 导致该应用换 token 全部失败
	IF INSTR(V_ADDITIONAL_INFO, '<') > 0 THEN
		RAISE_APPLICATION_ERROR(-20001, '占位符<许昌园区ID>未替换：请先查 smt_park 表填入真实园区ID后再执行本脚本');
	END IF;

	-- 检查应用是否已存在
	SELECT COUNT(1) INTO V_EXISTING_COUNT
	FROM sys_oauth_client_details
	WHERE client_id = 'file-receiver-xc';

	IF V_EXISTING_COUNT > 0 THEN
		DBMS_OUTPUT.PUT_LINE('应用 file-receiver-xc 已存在，跳过注册');
	ELSE
		INSERT INTO sys_oauth_client_details
		  (client_id, client_secret, scope, authorized_grant_types,
		   access_token_validity, additional_information)
		VALUES
		  ('file-receiver-xc', '{noop}CHANGE-ME-ON-DEPLOY', 'open:admittance:photo:read',
		   'client_credentials', 43200, V_ADDITIONAL_INFO);

		DBMS_OUTPUT.PUT_LINE('应用 file-receiver-xc 注册成功');
	END IF;

	COMMIT;
END;
