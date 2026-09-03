-- 保密区供应商软删除应用回滚前置检查
-- 执行条件：仅在需要回退本次应用版本时执行；由 DBA 在目标 Oracle schema 执行。
-- 影响表：只读检查 SMT_SECURITYAREA_SUPPLIER、SMT_SUPPLIER_PERSON，不删除字段或数据。
-- 可重复性：可重复执行。旧版本不读取 DEL_FLAG 字段，因此字段保留可避免破坏性 DDL。
-- 回滚限制：任一表存在非 0 的 DEL_FLAG 或空值时，旧版本会重新展示失效记录，脚本将拒绝回滚。

DECLARE
    v_supplier_table_count NUMBER;
    v_person_table_count NUMBER;
    v_supplier_column_count NUMBER;
    v_person_column_count NUMBER;
    v_supplier_deleted_count NUMBER;
    v_person_deleted_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_supplier_table_count
    FROM USER_TABLES
    WHERE TABLE_NAME = 'SMT_SECURITYAREA_SUPPLIER';

    SELECT COUNT(*) INTO v_person_table_count
    FROM USER_TABLES
    WHERE TABLE_NAME = 'SMT_SUPPLIER_PERSON';

    IF v_supplier_table_count = 0 OR v_person_table_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, '缺少保密区供应商或供应商人员表，无法完成回滚前置检查。');
    END IF;

    SELECT COUNT(*) INTO v_supplier_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_SECURITYAREA_SUPPLIER'
      AND COLUMN_NAME = 'DEL_FLAG';

    SELECT COUNT(*) INTO v_person_column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'SMT_SUPPLIER_PERSON'
      AND COLUMN_NAME = 'DEL_FLAG';

    IF v_supplier_column_count = 0 OR v_person_column_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20012, '软删除字段不存在，无需回滚本次字段变更。');
    END IF;

    -- DEL_FLAG 可能不存在；字段存在性校验通过后再在运行期解析，避免 ORA-00904。
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SECURITYAREA_SUPPLIER WHERE DEL_FLAG <> 0 OR DEL_FLAG IS NULL'
        INTO v_supplier_deleted_count;

    -- 同上：不能在匿名块编译期静态引用可能不存在的 DEL_FLAG 字段。
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SUPPLIER_PERSON WHERE DEL_FLAG <> 0 OR DEL_FLAG IS NULL'
        INTO v_person_deleted_count;

    IF v_supplier_deleted_count > 0 OR v_person_deleted_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20013, '存在失效或非法逻辑删除标识数据，禁止回滚旧版本以防记录重新展示。');
    END IF;

    DBMS_OUTPUT.PUT_LINE('回滚前置检查通过：未发现软删除数据，可回退应用；DEL_FLAG 字段保留。');
END;
/
