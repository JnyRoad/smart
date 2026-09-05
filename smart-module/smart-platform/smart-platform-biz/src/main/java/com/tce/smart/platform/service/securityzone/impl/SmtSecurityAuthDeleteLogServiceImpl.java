package com.tce.smart.platform.service.securityzone.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogTaskRespDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogPageDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogTaskDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteTask;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteLogMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteTaskMapper;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteLogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 保密区权限自动删除审计报表服务实现。
 *
 * <p>这里集中承载园区范围、参数边界、任务状态聚合和 CSV 安全处理，避免控制器绕过审计查询约束。</p>
 */
@Service
@AllArgsConstructor
public class SmtSecurityAuthDeleteLogServiceImpl implements SmtSecurityAuthDeleteLogService {

	/** 标准设备任务表来源。 */
	static final String TASK_SOURCE_NORMAL = "NORMAL";

	/** ISC 设备任务表来源。 */
	static final String TASK_SOURCE_ISC = "ISC";

	/** 导出允许的最大记录数。 */
	static final long MAX_EXPORT_ROWS = 10000L;

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;
	private static final DateTimeFormatter CSV_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final Set<String> VALID_RESULTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"SKIPPED_WHITELIST", "SKIPPED_NOT_DUE", "SKIPPED_NO_DEVICE", "SKIPPED_STAFF_MISSING",
			"DRY_RUN", "PROCESSING", "SUCCESS", "FAILED", "UNKNOWN")));

	private final SmtSecurityAuthDeleteLogMapper logMapper;
	private final SmtSecurityAuthDeleteTaskMapper taskMapper;

	/**
	 * 保存审计主记录和全部任务关联。
	 *
	 * @param log 判定时的人员、权限和结果快照
	 * @param taskRefs 实际生成任务的来源引用；无效引用会在写入前拒绝
	 * @throws RuntimeException 任一数据库写入失败时抛出，交由调用方事务回滚
	 */
	@Override
	public void record(SmtSecurityAuthDeleteLog log, List<SecurityAuthDeleteTaskRef> taskRefs) {
		validateLog(log);
		List<SecurityAuthDeleteTaskRef> normalizedRefs = normalizeTaskRefs(taskRefs);
		int logRows = logMapper.insert(log);
		if (logRows != 1 || log.getId() == null) {
			throw new IllegalStateException("保存保密区权限自动删除审计主记录失败");
		}
		for (SecurityAuthDeleteTaskRef ref : normalizedRefs) {
			SmtSecurityAuthDeleteTask task = new SmtSecurityAuthDeleteTask();
			task.setLogId(log.getId());
			task.setTaskSource(ref.getTaskSource());
			task.setTaskId(normalizeTaskId(ref.getTaskId()));
			task.setDeviceCode(ref.getDeviceCode());
			task.setAction(ref.getAction());
			if (taskMapper.insert(task) != 1) {
				throw new IllegalStateException("保存保密区权限自动删除任务关联失败");
			}
		}
	}

	/**
	 * 按令牌园区范围查询审计分页。
	 *
	 * @param page 分页参数，空值使用默认第一页
	 * @param query 客户端筛选条件
	 * @return 转换为字符串 ID 的 API 分页结果
	 */
	@Override
	public IPage<SecurityAuthDeleteLogRespDTO> page(Page<?> page, SecurityAuthDeleteLogPageQueryReqDTO query) {
		List<Integer> parkIds = currentParkIds();
		validateQuery(page, query, parkIds);
		Page<?> safePage = normalizePage(page);
		SecurityAuthDeleteLogPageQueryReqDTO mapperQuery = normalizeQuery(query);
		@SuppressWarnings("unchecked")
		IPage<SecurityAuthDeleteLogPageDTO> sourcePage = logMapper.selectPageWithTaskSummary(
				(Page<?>) safePage, mapperQuery, parkIds);
		return convertPage(sourcePage);
	}

	/**
	 * 导出当前筛选结果并写出 UTF-8 BOM CSV。
	 *
	 * @param query 客户端筛选条件
	 * @param response HTTP 下载响应
	 * @throws IllegalArgumentException 超过导出上限或园区范围无效
	 * @throws IllegalStateException 输出失败
	 */
	@Override
	public void export(SecurityAuthDeleteLogPageQueryReqDTO query, HttpServletResponse response) {
		List<Integer> parkIds = currentParkIds();
		// 导出通过内部探测页判断总数，筛选校验不应受分页接口的100条页大小限制。
		validateQuery(null, query, parkIds);
		SecurityAuthDeleteLogPageQueryReqDTO mapperQuery = normalizeQuery(query);
		Page<SecurityAuthDeleteLogPageDTO> exportPage = new Page<>(1, MAX_EXPORT_ROWS + 1);
		IPage<SecurityAuthDeleteLogPageDTO> sourcePage = logMapper.selectPageWithTaskSummary(exportPage, mapperQuery, parkIds);
		validateExportCount(sourcePage == null ? 0 : sourcePage.getTotal());
		if (sourcePage != null && sourcePage.getRecords() != null
				&& sourcePage.getRecords().size() > MAX_EXPORT_ROWS) {
			throw new IllegalArgumentException("导出记录超过10000条，请缩小筛选范围");
		}
		if (response == null) {
			throw new IllegalArgumentException("导出响应不能为空");
		}
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType("text/csv;charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=security-auth-delete-log.csv");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
			writer.write('\uFEFF');
			writeCsvRow(writer, Arrays.asList("记录ID", "园区ID", "执行时间", "员工ID", "工号", "姓名", "部门",
					"权限组ID", "权限组", "最后进出时间", "触发原因", "结果", "说明", "任务数", "成功数",
					"失败数", "处理中数", "未知数"));
			if (sourcePage != null && sourcePage.getRecords() != null) {
				for (SecurityAuthDeleteLogPageDTO row : sourcePage.getRecords()) {
					writeCsvRow(writer, csvValues(row));
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("导出保密区权限自动删除审计失败", ex);
		}
	}

	/**
	 * 查询审计记录任务详情并先校验主记录园区权限。
	 *
	 * @param id 审计主键文本
	 * @return 任务详情列表
	 */
	@Override
	public List<SecurityAuthDeleteLogTaskRespDTO> tasks(String id) {
		List<Integer> parkIds = currentParkIds();
		Long logId = parseLogId(id);
		if (logMapper.selectAuthorizedLog(logId, parkIds) == null) {
			throw new IllegalArgumentException("记录不存在或无权访问");
		}
		List<SecurityAuthDeleteLogTaskDTO> sourceTasks = logMapper.selectTasks(logId, parkIds);
		List<SecurityAuthDeleteLogTaskRespDTO> result = new ArrayList<>();
		if (sourceTasks == null) {
			return result;
		}
		for (SecurityAuthDeleteLogTaskDTO sourceTask : sourceTasks) {
			SecurityAuthDeleteLogTaskRespDTO target = new SecurityAuthDeleteLogTaskRespDTO();
			target.setTaskSource(sourceTask.getTaskSource());
			target.setTaskId(sourceTask.getTaskId());
			target.setDeviceCode(sourceTask.getDeviceCode());
			target.setAction(sourceTask.getAction());
			target.setStatus(sourceTask.getStatus());
			target.setCode(sourceTask.getCode());
			target.setRemark(sourceTask.getRemark());
			target.setCreateTime(sourceTask.getCreateTime());
			target.setUpdateTime(sourceTask.getUpdateTime());
			result.add(target);
		}
		return result;
	}

	/**
	 * 校验分页、日期、结果代码和园区范围。
	 *
	 * @param page 分页参数
	 * @param query 查询参数
	 * @param parkIds 令牌园区范围
	 * @throws IllegalArgumentException 参数不合法或园区范围为空
	 */
	static void validateQuery(Page<?> page, SecurityAuthDeleteLogPageQueryReqDTO query, List<Integer> parkIds) {
		if (parkIds == null || parkIds.isEmpty()) {
			throw new IllegalArgumentException("园区数据范围为空，拒绝查询");
		}
		if (page != null && (page.getCurrent() < 1 || page.getSize() < 1 || page.getSize() > MAX_PAGE_SIZE)) {
			throw new IllegalArgumentException("分页大小必须在1到100之间");
		}
		if (query == null) {
			return;
		}
		if (query.getParkId() != null && !parkIds.contains(query.getParkId())) {
			throw new IllegalArgumentException("无权访问该园区");
		}
		if (query.getStartTime() != null && query.getEndTime() != null
				&& query.getStartTime().isAfter(query.getEndTime())) {
			throw new IllegalArgumentException("开始时间不能晚于结束时间");
		}
		if (query.getResult() != null && !query.getResult().isEmpty() && !VALID_RESULTS.contains(query.getResult())) {
			throw new IllegalArgumentException("结果代码不合法");
		}
	}

	/**
	 * 校验导出总条数。
	 *
	 * @param total 查询匹配总数
	 * @throws IllegalArgumentException 超出10000条
	 */
	static void validateExportCount(long total) {
		if (total > MAX_EXPORT_ROWS) {
			throw new IllegalArgumentException("导出记录超过10000条，请缩小筛选范围");
		}
	}

	/**
	 * 对可能被表格软件识别为公式的 CSV 值加单引号前缀。
	 *
	 * @param value 原始单元格值
	 * @return 可安全写入 CSV 的值
	 */
	static String escapeCsvValue(String value) {
		if (value == null) {
			return "";
		}
		int firstMeaningfulIndex = 0;
		while (firstMeaningfulIndex < value.length()
				&& isIgnorableCsvPrefix(value.charAt(firstMeaningfulIndex))) {
			firstMeaningfulIndex++;
		}
		if (firstMeaningfulIndex < value.length()
				&& isCsvFormulaPrefix(value.charAt(firstMeaningfulIndex))) {
			return "'" + value;
		}
		return value;
	}

	/** 判断表格软件可能吞掉的前导空白、控制字符或格式字符。 */
	private static boolean isIgnorableCsvPrefix(char value) {
		return Character.isWhitespace(value)
				|| Character.isSpaceChar(value)
				|| Character.isISOControl(value)
				|| Character.getType(value) == Character.FORMAT;
	}

	/** 判断可能被表格软件解释为公式的首字符。 */
	private static boolean isCsvFormulaPrefix(char value) {
		return value == '=' || value == '+' || value == '-' || value == '@';
	}

	/**
	 * 获取当前登录用户的园区范围。
	 *
	 * @return 非空园区 ID 列表；无用户或无范围时返回空列表，由校验方法拒绝查询
	 */
	private List<Integer> currentParkIds() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null) {
			return Collections.emptyList();
		}
		SmartUser user = SecurityUtils.getUser(authentication);
		if (user == null || user.getParkIdList() == null) {
			return Collections.emptyList();
		}
		return user.getParkIdList();
	}

	/**
	 * 将输入分页标准化为接口默认值。
	 *
	 * @param page 原始分页对象
	 * @return 可传给 MyBatis 的分页对象
	 */
	private Page<?> normalizePage(Page<?> page) {
		if (page == null) {
			return new Page<>(1, DEFAULT_PAGE_SIZE);
		}
		return page;
	}

	/**
	 * 复制查询条件并把包含式结束时间转换成下一秒的排他上界。
	 *
	 * @param query 原始查询条件
	 * @return 供 SQL 使用的查询条件
	 */
	private SecurityAuthDeleteLogPageQueryReqDTO normalizeQuery(SecurityAuthDeleteLogPageQueryReqDTO query) {
		SecurityAuthDeleteLogPageQueryReqDTO result = new SecurityAuthDeleteLogPageQueryReqDTO();
		if (query == null) {
			return result;
		}
		result.setParkId(query.getParkId());
		result.setStartTime(query.getStartTime());
		result.setEndTime(query.getEndTime() == null ? null : query.getEndTime().plusSeconds(1));
		result.setStaffBadge(query.getStaffBadge());
		result.setStaffName(query.getStaffName());
		result.setDepartment(query.getDepartment());
		result.setAuthName(query.getAuthName());
		result.setResult(query.getResult());
		return result;
	}

	/**
	 * 将核心分页投影转换为 API 响应。
	 *
	 * @param sourcePage 数据库分页投影
	 * @return API 分页结果
	 */
	private IPage<SecurityAuthDeleteLogRespDTO> convertPage(IPage<SecurityAuthDeleteLogPageDTO> sourcePage) {
		long current = sourcePage == null ? 1 : sourcePage.getCurrent();
		long size = sourcePage == null ? DEFAULT_PAGE_SIZE : sourcePage.getSize();
		Page<SecurityAuthDeleteLogRespDTO> targetPage = new Page<>(current, size);
		if (sourcePage == null) {
			targetPage.setTotal(0);
			return targetPage;
		}
		targetPage.setTotal(sourcePage.getTotal());
		List<SecurityAuthDeleteLogRespDTO> records = new ArrayList<>();
		if (sourcePage.getRecords() != null) {
			for (SecurityAuthDeleteLogPageDTO source : sourcePage.getRecords()) {
				records.add(toResponse(source));
			}
		}
		targetPage.setRecords(records);
		return targetPage;
	}

	/**
	 * 转换一行审计投影并将 Long ID 序列化为字符串。
	 *
	 * @param source 数据库投影
	 * @return API 响应
	 */
	private SecurityAuthDeleteLogRespDTO toResponse(SecurityAuthDeleteLogPageDTO source) {
		SecurityAuthDeleteLogRespDTO target = new SecurityAuthDeleteLogRespDTO();
		target.setId(source.getId() == null ? null : source.getId().toString());
		target.setParkId(source.getParkId());
		target.setExecTime(source.getExecTime());
		target.setStaffId(source.getStaffId() == null ? null : source.getStaffId().toString());
		target.setStaffBadge(source.getStaffBadge());
		target.setStaffName(source.getStaffName());
		target.setDepartment(source.getDepartment());
		target.setAuthId(source.getAuthId());
		target.setAuthName(source.getAuthName());
		target.setLastSnapTime(source.getLastSnapTime());
		target.setTriggerReason(source.getTriggerReason());
		target.setResult(source.getResult());
		target.setRemark(source.getRemark());
		target.setTaskCount(source.getTaskCount());
		target.setSuccessCount(source.getSuccessCount());
		target.setFailCount(source.getFailCount());
		target.setPendingCount(source.getPendingCount());
		target.setUnknownCount(source.getUnknownCount());
		return target;
	}

	/**
	 * 校验主记录的必填审计字段。
	 *
	 * @param log 待写入的审计记录
	 */
	private void validateLog(SmtSecurityAuthDeleteLog log) {
		if (log == null) {
			throw new IllegalArgumentException("审计记录不能为空");
		}
		if (log.getParkId() == null || log.getExecTime() == null || log.getResult() == null
				|| log.getResult().trim().isEmpty()) {
			throw new IllegalArgumentException("审计记录缺少园区、执行时间或结果");
		}
		if (!VALID_RESULTS.contains(log.getResult().trim())) {
			throw new IllegalArgumentException("审计结果代码不合法");
		}
	}

	/**
	 * 预校验并复制全部任务引用，保证主记录写入前不会出现半套关联。
	 *
	 * @param taskRefs 调用方提供的任务引用
	 * @return 去除首尾空白后的安全引用
	 */
	private List<SecurityAuthDeleteTaskRef> normalizeTaskRefs(List<SecurityAuthDeleteTaskRef> taskRefs) {
		if (taskRefs == null || taskRefs.isEmpty()) {
			return Collections.emptyList();
		}
		List<SecurityAuthDeleteTaskRef> result = new ArrayList<>();
		for (SecurityAuthDeleteTaskRef ref : taskRefs) {
			if (ref == null || (!TASK_SOURCE_NORMAL.equals(ref.getTaskSource()) && !TASK_SOURCE_ISC.equals(ref.getTaskSource()))) {
				throw new IllegalArgumentException("设备任务来源不合法");
			}
			String taskId = normalizeTaskId(ref.getTaskId());
			SecurityAuthDeleteTaskRef normalized = new SecurityAuthDeleteTaskRef();
			normalized.setTaskSource(ref.getTaskSource());
			normalized.setTaskId(taskId);
			normalized.setDeviceCode(ref.getDeviceCode());
			normalized.setAction(ref.getAction());
			result.add(normalized);
		}
		return result;
	}

	/**
	 * 校验任务 ID 是可持久化的十进制主键文本。
	 *
	 * @param taskId 设备任务返回值
	 * @return 去除空白并去掉多余前导零的任务 ID
	 */
	private String normalizeTaskId(String taskId) {
		String value = taskId == null ? null : taskId.trim();
		if (value == null || value.isEmpty() || value.length() > 19 || !value.matches("[0-9]+")) {
			throw new IllegalArgumentException("设备任务ID必须是数字");
		}
		try {
			return new BigInteger(value).toString();
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("设备任务ID超出可识别范围", ex);
		}
	}

	/**
	 * 将文本主键解析为审计 Long 主键。
	 *
	 * @param id 客户端路径参数
	 * @return Long 主键
	 */
	private Long parseLogId(String id) {
		if (id == null || id.trim().isEmpty() || !id.trim().matches("[0-9]+")) {
			throw new IllegalArgumentException("审计记录ID不合法");
		}
		try {
			return Long.valueOf(id.trim());
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("审计记录ID超出范围", ex);
		}
	}

	/**
	 * 写出一行 CSV 并对每个字段进行公式和引号处理。
	 *
	 * @param writer CSV 输出 writer
	 * @param values 行字段
	 * @throws IOException 输出失败
	 */
	private void writeCsvRow(BufferedWriter writer, List<String> values) throws IOException {
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				writer.write(',');
			}
			String value = escapeCsvValue(values.get(i));
			writer.write('"');
			writer.write(value.replace("\"", "\"\""));
			writer.write('"');
		}
		writer.newLine();
	}

	/**
	 * 组装审计行的导出字段。
	 *
	 * @param row 审计分页投影
	 * @return CSV 字段值
	 */
	private List<String> csvValues(SecurityAuthDeleteLogPageDTO row) {
		return Arrays.asList(
				text(row.getId()), text(row.getParkId()), time(row.getExecTime()), text(row.getStaffId()), row.getStaffBadge(),
				row.getStaffName(), row.getDepartment(), text(row.getAuthId()), row.getAuthName(), time(row.getLastSnapTime()),
				row.getTriggerReason(), resultLabel(row.getResult()), row.getRemark(), text(row.getTaskCount()), text(row.getSuccessCount()),
				text(row.getFailCount()), text(row.getPendingCount()), text(row.getUnknownCount()));
	}

	/** 将结果代码转换为导出文件可读的中文文案，接口 result 字段仍保留稳定代码。 */
	private String resultLabel(String result) {
		if (result == null || result.trim().isEmpty()) {
			return result;
		}
		switch (result) {
			case "SKIPPED_WHITELIST":
				return "白名单跳过";
			case "SKIPPED_NOT_DUE":
				return "未到删除期限";
			case "SKIPPED_NO_DEVICE":
				return "无关联设备";
			case "SKIPPED_STAFF_MISSING":
				return "人员不存在";
			case "DRY_RUN":
				return "演练命中";
			case "PROCESSING":
				return "任务执行中";
			case "SUCCESS":
				return "任务记录成功";
			case "FAILED":
				return "处理或任务失败";
			case "UNKNOWN":
				return "任务状态未知";
			default:
				return "未知结果（" + result + "）";
		}
	}

	/** 将对象转换为 CSV 文本，空值保持空单元格。 */
	private String text(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	/** 将时间按接口约定格式化到秒。 */
	private String time(LocalDateTime value) {
		return value == null ? "" : value.format(CSV_TIME_FORMATTER);
	}
}
