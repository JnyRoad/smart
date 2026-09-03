package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 保密供应商软删除的 Mapper 契约测试。
 *
 * 该测试只验证实体逻辑删除配置和 MyBatis 实际解析后的 SQL，避免手写 XML
 * 因绕过 MyBatis-Plus 内置查询而重新把失效供应商展示给管理端。
 */
public class SmtSecurityAreaSupplierSoftDeleteMapperXmlTest {

	/**
	 * 验证供应商和人员实体都明确约定 0 为有效、1 为已删除。
	 *
	 * 反射读取注解，不依赖数据库；字段缺失或注解取值错误都会导致测试失败。
	 */
	@Test
	public void supplierEntitiesDeclareZeroAndOneLogicDeleteValues() throws Exception {
		assertLogicDeleteValues(SmtSecurityAreaSupplier.class);
		assertLogicDeleteValues(SmtSupplierPerson.class);
	}

	/**
	 * 验证供应商树、分页、导出前置读取和通知候选的自定义 SQL 都过滤已删除供应商。
	 *
	 * XML 查询不走 MyBatis-Plus 内置逻辑删除注入，因此每个语句必须保留显式条件。
	 */
	@Test
	public void supplierCustomReadsExcludeSoftDeletedSuppliers() throws Exception {
		Configuration configuration = parseMapper("mapper/SmtSecurityAreaSupplierMapper.xml");

		Map<String, Object> pageParameters = new HashMap<>();
		pageParameters.put("query", new SmtSecurityAreaSupplierDTO());
		pageParameters.put("park", Collections.emptyList());
		assertSqlContains(configuration, SmtSecurityAreaSupplierMapper.class.getName()
				+ ".getSecurityAreaSupplierPage", pageParameters, "SS.DEL_FLAG = 0");

		Map<String, Object> notifyParameters = new HashMap<>();
		notifyParameters.put("parkId", 1);
		notifyParameters.put("days", 30);
		assertSqlContains(configuration, SmtSecurityAreaSupplierMapper.class.getName()
				+ ".getNotifyList", notifyParameters, "DEL_FLAG = 0");

		Map<String, Object> listParameters = new HashMap<>();
		listParameters.put("compName", null);
		listParameters.put("parkId", null);
		listParameters.put("parks", Collections.emptyList());
		assertSqlContains(configuration, SmtSecurityAreaSupplierMapper.class.getName()
				+ ".getSupplierList", listParameters, "SUPPLIER.DEL_FLAG = 0");
	}

	/**
	 * 验证人员分页同时排除已删除人员和所属已删除供应商。
	 *
	 * 两侧都过滤才能阻止历史人员通过连接查询重新出现在管理端。
	 */
	@Test
	public void supplierPersonPageExcludesDeletedPersonAndSupplier() throws Exception {
		Configuration configuration = parseMapper("mapper/SmtSupplierPersonMapper.xml");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("query", new SmtSupplierPersonDTO());
		parameters.put("park", Collections.emptyList());

		assertSqlContains(configuration, SmtSupplierPersonMapper.class.getName()
				+ ".getSupplierPersonPage", parameters, "SP.DEL_FLAG = 0");
		assertSqlContains(configuration, SmtSupplierPersonMapper.class.getName()
				+ ".getSupplierPersonPage", parameters, "SS.DEL_FLAG = 0");
	}

	/**
	 * 验证服务级人员读取使用单条关联 SQL，并让删除和新增共享供应商行锁。
	 *
	 * 单次查询避免在先校验供应商、后查询人员之间被并发删除插入失效数据。
	 */
	@Test
	public void serviceReadsJoinActiveSupplierAndWritesLockSupplierRow() throws Exception {
		Configuration supplierConfiguration = parseMapper("mapper/SmtSecurityAreaSupplierMapper.xml");
		Map<String, Object> supplierParameters = new HashMap<>();
		supplierParameters.put("id", 1L);
		assertSqlContains(supplierConfiguration, SmtSecurityAreaSupplierMapper.class.getName()
				+ ".selectActiveSupplierForUpdate", supplierParameters, "DEL_FLAG = 0 FOR UPDATE");

		Configuration personConfiguration = parseMapper("mapper/SmtSupplierPersonMapper.xml");
		Map<String, Object> personParameters = new HashMap<>();
		personParameters.put("supplierId", 1L);
		personParameters.put("idCard", "440100200001010011");
		assertSqlContains(personConfiguration, SmtSupplierPersonMapper.class.getName()
				+ ".getActiveSupplierPersonList", personParameters, "SP.DEL_FLAG = 0");
		assertSqlContains(personConfiguration, SmtSupplierPersonMapper.class.getName()
				+ ".getActiveSupplierPersonList", personParameters, "SS.DEL_FLAG = 0");
		assertSqlContains(personConfiguration, SmtSupplierPersonMapper.class.getName()
				+ ".existsActiveSupplierPerson", personParameters, "SS.DEL_FLAG = 0");
	}

	/**
	 * 验证订单详情仍是左连接，但不会返回已删除供应商的当前展示信息。
	 *
	 * 订单主记录不受供应商失效影响，连接条件只负责隐藏失效供应商字段。
	 */
	@Test
	public void securityAreaOrderDetailHidesDeletedSupplierWithoutDroppingOrder() throws Exception {
		Configuration configuration = parseMapper("mapper/SmtSecurityAreaOrderMapper.xml");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", 1L);

		assertSqlContains(configuration, SmtSecurityAreaOrderMapper.class.getName()
				+ ".getSecurityAreaOrderDetail", parameters,
				"LEFT JOIN SMT_SECURITYAREA_SUPPLIER SUPPLIER ON SORD.SUPPLIER_ID=SUPPLIER.ID AND SUPPLIER.DEL_FLAG=0");
	}

	/**
	 * 验证迁移脚本在同一个匿名 PL/SQL 块内新增字段后，使用动态 SQL 校验该字段。
	 *
	 * Oracle 会在编译匿名块时解析静态 SQL；若静态语句提前引用本次新增的字段，
	 * 即使 ALTER TABLE 写在前面也会报 ORA-00904。
	 */
	@Test
	public void migrationScriptDefersNewColumnValidationUntilAfterDynamicDdl() throws Exception {
		String sql = new String(Files.readAllBytes(Paths.get("..", "..", "database", "manual",
				"20260902_add_supplier_soft_delete.sql")), StandardCharsets.UTF_8).toUpperCase();
		String normalizedSql = sql.replaceAll("\\s+", " ");

		Assert.assertTrue("供应商 DEL_FLAG 校验必须通过动态 SQL 执行", normalizedSql.contains(
				"EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SECURITYAREA_SUPPLIER WHERE DEL_FLAG IS NOT NULL AND DEL_FLAG NOT IN (0, 1)' INTO V_INVALID_VALUE_COUNT;"));
		Assert.assertTrue("供应商人员 DEL_FLAG 校验必须通过动态 SQL 执行", normalizedSql.contains(
				"EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SUPPLIER_PERSON WHERE DEL_FLAG IS NOT NULL AND DEL_FLAG NOT IN (0, 1)' INTO V_INVALID_VALUE_COUNT;"));
		Assert.assertFalse("匿名块中不能静态查询刚新增的供应商 DEL_FLAG 字段", sql.contains(
				"FROM SMT_SECURITYAREA_SUPPLIER\n    WHERE DEL_FLAG IS NOT NULL"));
		Assert.assertFalse("匿名块中不能静态查询刚新增的供应商人员 DEL_FLAG 字段", sql.contains(
				"FROM SMT_SUPPLIER_PERSON\n    WHERE DEL_FLAG IS NOT NULL"));
	}

	/**
	 * 验证回滚前置检查也将 DEL_FLAG 统计延后到运行期解析。
	 *
	 * 字段存在性判断本身不足以避免编译期错误；匿名块内的静态查询仍会在判断前解析。
	 */
	@Test
	public void rollbackPreflightDefersDeleteFlagStatisticsUntilAfterColumnCheck() throws Exception {
		String sql = new String(Files.readAllBytes(Paths.get("..", "..", "database", "manual",
				"20260902_rollback_supplier_soft_delete.sql")), StandardCharsets.UTF_8).toUpperCase();
		String normalizedSql = sql.replaceAll("\\s+", " ");

		Assert.assertTrue("供应商 DEL_FLAG 统计必须通过动态 SQL 执行", normalizedSql.contains(
				"EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SECURITYAREA_SUPPLIER WHERE DEL_FLAG <> 0 OR DEL_FLAG IS NULL' INTO V_SUPPLIER_DELETED_COUNT;"));
		Assert.assertTrue("供应商人员 DEL_FLAG 统计必须通过动态 SQL 执行", normalizedSql.contains(
				"EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM SMT_SUPPLIER_PERSON WHERE DEL_FLAG <> 0 OR DEL_FLAG IS NULL' INTO V_PERSON_DELETED_COUNT;"));
		Assert.assertFalse("匿名块中不能静态统计供应商 DEL_FLAG", sql.contains(
				"FROM SMT_SECURITYAREA_SUPPLIER\n    WHERE DEL_FLAG <> 0"));
		Assert.assertFalse("匿名块中不能静态统计供应商人员 DEL_FLAG", sql.contains(
				"FROM SMT_SUPPLIER_PERSON\n    WHERE DEL_FLAG <> 0"));
	}

	/**
	 * 验证单个实体的逻辑删除字段和注解值。
	 *
	 * 字段不存在、没有注解或值不符合数据契约都会立即抛出断言失败。
	 */
	private void assertLogicDeleteValues(Class<?> entityType) throws Exception {
		Field field = entityType.getDeclaredField("delFlag");
		TableLogic tableLogic = field.getAnnotation(TableLogic.class);
		Assert.assertNotNull(entityType.getSimpleName() + " 必须声明逻辑删除注解", tableLogic);
		Assert.assertEquals("0", tableLogic.value());
		Assert.assertEquals("1", tableLogic.delval());
	}

	/**
	 * 解析 Mapper XML，取得运行时会使用的 MyBatis 配置。
	 *
	 * 资源缺失或 XML 不合法时测试失败，防止只做字符串断言而遗漏 Mapper 解析错误。
	 */
	private Configuration parseMapper(String resource) throws Exception {
		Configuration configuration = new Configuration();
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments()).parse();
		}
		return configuration;
	}

	/**
	 * 验证指定 Mapper 语句生成的 SQL 含有目标条件。
	 *
	 * 条件缺失时输出完整规范化 SQL，便于定位是哪一条手写查询漏掉逻辑删除过滤。
	 */
	private void assertSqlContains(Configuration configuration, String statementId,
			Map<String, Object> parameters, String expectedFragment) {
		MappedStatement statement = configuration.getMappedStatement(statementId);
		BoundSql boundSql = statement.getBoundSql(parameters);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim().toUpperCase();
		Assert.assertTrue(statementId + " 必须过滤逻辑删除记录，实际 SQL：" + sql,
				sql.contains(expectedFragment));
	}
}
