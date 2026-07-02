-- 回滚脚本：剥离本次迁移写入的 '{noop}' 前缀，还原为迁移前的明文存储。
-- 注意：只处理 '{noop}' 前缀的行；迁移之后如果业务又新增/重置了 '{bcrypt}' 编码的 client_secret，
--      这些行本身就没有可还原的明文，本脚本不做处理，避免破坏新数据。
-- 执行方式：与本目录其它脚本一致，整段执行，不要按分号逐句执行。
-- 执行前可先运行下面的 SELECT 预览影响范围：
-- SELECT client_id, client_secret FROM sys_oauth_client_details WHERE client_secret LIKE '{noop}%';

DECLARE
	V_COUNT NUMBER;
BEGIN
	UPDATE sys_oauth_client_details
	SET client_secret = SUBSTR(client_secret, LENGTH('{noop}') + 1)
	WHERE client_secret LIKE '{noop}%';

	V_COUNT := SQL%ROWCOUNT;
	DBMS_OUTPUT.PUT_LINE('Rolled back {noop}-prefixed oauth client secrets: ' || V_COUNT);
	COMMIT;
END;
