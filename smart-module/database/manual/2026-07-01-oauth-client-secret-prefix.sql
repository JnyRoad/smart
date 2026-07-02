-- 手工数据迁移：给 sys_oauth_client_details.client_secret 补齐编码前缀。
-- 背景：历史查询语句在 SELECT 时对 client_secret 强制拼接 '{noop}'（见 SecurityConstants.CLIENT_FIELDS），
--       即数据库里存的其实是明文，编码方式被写死在代码里，无法与 BCrypt 等其它编码方式共存。
--       本次改为「前缀随数据入库」，SELECT 直接返回原始 client_secret，交给 DelegatingPasswordEncoder 按前缀选择匹配算法。
-- 影响范围：仅处理当前没有编码前缀（不以 '{' 开头）的存量行，全部补 '{noop}' 前缀，保持现有明文校验行为不变。
--          已带前缀（如后续新增/重置用的 '{bcrypt}'）的行不受影响。
-- 执行方式：与本目录其它脚本一致，整段执行，不要按分号逐句执行。
-- 执行前可先运行下面的 SELECT 预览影响范围：
-- SELECT client_id, client_secret FROM sys_oauth_client_details WHERE client_secret NOT LIKE '{%';

DECLARE
	V_COUNT NUMBER;
BEGIN
	UPDATE sys_oauth_client_details
	SET client_secret = '{noop}' || client_secret
	WHERE client_secret NOT LIKE '{%';

	V_COUNT := SQL%ROWCOUNT;
	DBMS_OUTPUT.PUT_LINE('Prefixed legacy oauth client secrets with {noop}: ' || V_COUNT);
	COMMIT;
END;
