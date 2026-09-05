package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogTaskRespDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 保密区权限自动删除审计报表服务。
 *
 * <p>写入方法由自动删除调用方纳入其当前事务；查询方法在服务端统一校验令牌园区范围。</p>
 */
public interface SmtSecurityAuthDeleteLogService {

	/**
	 * 保存一次判定快照及其生成的全部设备任务关联。
	 *
	 * @param log 判定快照，必须包含园区、执行时间和结果
	 * @param taskRefs 设备任务来源引用，可为空但不允许包含无效任务 ID
	 * @throws RuntimeException 任一主记录或关联写入失败时向调用方抛出
	 */
	void record(SmtSecurityAuthDeleteLog log, List<SecurityAuthDeleteTaskRef> taskRefs);

	/**
	 * 按当前用户园区范围查询自动删除审计分页。
	 *
	 * @param page 分页参数，空值按第一页20条处理
	 * @param query 组合筛选条件，可为空
	 * @return 审计报表分页
	 */
	IPage<SecurityAuthDeleteLogRespDTO> page(Page<?> page, SecurityAuthDeleteLogPageQueryReqDTO query);

	/**
	 * 导出当前用户有权访问的筛选结果。
	 *
	 * @param query 组合筛选条件，可为空
	 * @param response CSV 下载响应
	 */
	void export(SecurityAuthDeleteLogPageQueryReqDTO query, HttpServletResponse response);

	/**
	 * 查询一条审计记录的全部关联设备任务，并先校验主记录园区归属。
	 *
	 * @param id 审计记录主键文本
	 * @return 任务明细；主记录不存在或越权时抛出异常
	 */
	List<SecurityAuthDeleteLogTaskRespDTO> tasks(String id);
}
