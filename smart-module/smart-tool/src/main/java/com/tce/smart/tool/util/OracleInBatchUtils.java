package com.tce.smart.tool.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Oracle IN 子句分批查询工具。
 *
 * <p>背景：Oracle 对单个 IN 列表的表达式个数硬性上限为 1000，超过即抛
 * ORA-01795，若查询处于事务中会导致整个事务回滚。生产中权限组关联人员、
 * 批量授权工号等列表实证会超过 1000，所有对这类"无上界列表"的 IN 查询都
 * 必须分批执行后合并结果。
 *
 * <p>使用约定（务必阅读）：
 * <ul>
 * <li>结果按批次顺序原样拼接，<b>不去重、不排序</b>，语义等价于一次完整的
 * IN 查询把结果按参数分段返回；</li>
 * <li>单条 IN 查询对列表内的重复参数天然只返回一份结果，但分批后同一参数
 * 若落在不同批次会被重复查询、重复返回——<b>调用方必须在调用前自行对参数
 * 列表去重</b>（例如 stream().distinct()），否则语义会和单条 IN 不一致；</li>
 * <li>空列表 / null 直接返回空结果且绝不调用查询函数。此前有实现用
 * Hutool CollUtil.split 分批，它对空集合会返回 [[]]（含一个空子列表），
 * 空批进 in() 会生成非法 SQL，本工具从入口处规避了这个坑；</li>
 * <li>任一批次查询抛出的异常原样上抛（快速失败），不吞错、不返回半截结果，
 * 以便外层 @Transactional 正常回滚。</li>
 * </ul>
 *
 * @author lvtu
 * @since 2026-07-07
 */
public final class OracleInBatchUtils {

	/**
	 * Oracle IN 列表单批参数上限：超过 1000 个表达式即 ORA-01795。
	 * 直接取上限值分批（而不是留安全余量），批次数最少、行为最贴近数据库真实约束。
	 */
	public static final int ORACLE_IN_MAX_EXPRESSIONS = 1000;

	private OracleInBatchUtils() {
		// 纯静态工具类，禁止实例化
	}

	/**
	 * 把参数列表按 Oracle IN 上限（1000）分批执行查询，并把各批结果按批次顺序合并返回。
	 *
	 * <p>典型用法（MyBatis-Plus）：
	 * <pre>{@code
	 * List<SmtStaff> staffList = OracleInBatchUtils.listInBatches(staffIds,
	 *         batchIds -> staffService.list(Wrappers.<SmtStaff>lambdaQuery()
	 *                 .in(SmtStaff::getId, batchIds)));
	 * }</pre>
	 *
	 * @param params     待放入 IN 子句的完整参数列表，允许为 null / 空（直接返回空结果）；
	 *                   注意重复参数需调用方先去重，见类注释
	 * @param batchQuery 单批查询函数，入参为一批参数（1 ~ 1000 个，绝不为空），
	 *                   返回该批的查询结果（约定不返回 null，MyBatis-Plus 的 list() 满足该约定）
	 * @param <P>        IN 子句参数类型
	 * @param <R>        查询结果行类型
	 * @return 各批查询结果按批次顺序合并后的列表，永不为 null
	 */
	public static <P, R> List<R> listInBatches(List<P> params, Function<List<P>, List<R>> batchQuery) {
		Objects.requireNonNull(batchQuery, "batchQuery 查询函数不能为空");
		// 边界显式处理：空入参直接返回空结果，绝不带着空 IN 列表去查库
		if (params == null || params.isEmpty()) {
			return new ArrayList<>();
		}
		List<R> mergedRows = new ArrayList<>();
		for (int start = 0; start < params.size(); start += ORACLE_IN_MAX_EXPRESSIONS) {
			int end = Math.min(start + ORACLE_IN_MAX_EXPRESSIONS, params.size());
			// subList 是原列表的视图，包一层不可变视图防止查询函数意外改动原列表
			List<P> batch = Collections.unmodifiableList(params.subList(start, end));
			// 查询异常直接上抛（快速失败），让外层事务回滚
			mergedRows.addAll(batchQuery.apply(batch));
		}
		return mergedRows;
	}
}
