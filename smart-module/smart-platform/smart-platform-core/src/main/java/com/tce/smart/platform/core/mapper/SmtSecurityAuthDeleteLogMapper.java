package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogPageDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogTaskDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 保密区权限自动删除审计主表 Mapper。
 *
 * <p>分页查询在 XML 中统一完成园区范围、任务状态聚合和跨任务表关联。</p>
 */
public interface SmtSecurityAuthDeleteLogMapper extends BaseMapper<SmtSecurityAuthDeleteLog> {

	/**
	 * 按筛选条件查询审计分页及关联任务状态聚合。
	 *
	 * @param page MyBatis-Plus 分页对象
	 * @param query 客户端筛选条件
	 * @param parkIds 当前令牌允许访问的园区范围
	 * @return 分页投影
	 */
	IPage<SecurityAuthDeleteLogPageDTO> selectPageWithTaskSummary(Page<?> page,
			@Param("query") SecurityAuthDeleteLogPageQueryReqDTO query,
			@Param("parkIds") List<Integer> parkIds);

	/**
	 * 查询单条审计记录关联的全部任务当前状态。
	 *
	 * @param logId 审计记录主键
	 * @param parkIds 当前令牌允许访问的园区范围
	 * @return 任务明细，任务缺失时状态字段为空
	 */
	List<SecurityAuthDeleteLogTaskDTO> selectTasks(@Param("logId") Long logId,
			@Param("parkIds") List<Integer> parkIds);

	/**
	 * 查询指定园区范围内的主记录，用于任务详情越权校验。
	 *
	 * @param logId 审计记录主键
	 * @param parkIds 当前令牌允许访问的园区范围
	 * @return 授权主记录，不存在或越权时为空
	 */
	SmtSecurityAuthDeleteLog selectAuthorizedLog(@Param("logId") Long logId,
			@Param("parkIds") List<Integer> parkIds);
}
