package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DeviceTaskQueryDTO;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtIscDeviceTaskMapper extends BaseMapper<SmtIscDeviceTask> {

	/**
	 * 查询到期且可执行的删除任务
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 删除任务列表
	 */
	List<SmtIscDeviceTask> getDeleteTasks(@Param("deviceType") int deviceType, @Param("currentTime") long currentTime);

	/**
	 * 查询有效权限下发任务
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 有效权限任务列表
	 */
	List<SmtIscDeviceTask> getValidDownloadTasks(@Param("deviceType") int deviceType, @Param("currentTime") long currentTime);

	/**
	 * 查询过期任务（用于标记过期状态）
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 过期任务列表
	 */
	List<SmtIscDeviceTask> getExpiredTasks(@Param("deviceType") int deviceType, @Param("currentTime") long currentTime);

	/**
	 * 查询最近上线设备的待处理任务
	 * @param deviceType 设备类型
	 * @param deviceCodes 设备编码列表
	 * @return 待处理任务列表
	 */
	List<SmtIscDeviceTask> getRecentOnlineDeviceTasks(@Param("deviceType") int deviceType, @Param("deviceCodes") List<String> deviceCodes);

	/**
	 * 查询长期离线或已删除设备的待处理下发任务
	 * @param deviceType 设备类型
	 * @param staleMonths 超过多少个月未处理
	 * @return 待取消任务列表
	 */
	List<SmtIscDeviceTask> getStaleOfflineDownloadTasks(@Param("deviceType") int deviceType, @Param("staleMonths") int staleMonths);

	/**
	 * 带条件取消长期离线或已删除设备的待处理下发任务，避免设备刚上线或任务已处理后的竞态覆盖
	 */
	/**
	 * 将离线设备上的待下发任务标记为设备离线状态（status=6），设备恢复在线后由调度查询自动恢复
	 */
	int markOfflineDeviceTasks(@Param("deviceType") int deviceType, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 与markOfflineDeviceTasks同条件（复用同一SQL片段mark_offline_device_tasks_condition），
	 * 查询即将被标记为设备离线的任务所属入厂申请单ID（去重，排除NULL）
	 */
	Set<Long> getOfflineDeviceTaskApplyIds(@Param("deviceType") int deviceType);

	int cancelStaleOfflineDownloadTask(@Param("id") Long id,
									   @Param("deviceType") int deviceType,
									   @Param("staleMonths") int staleMonths,
									   @Param("status") int status,
									   @Param("code") int code,
									   @Param("remark") String remark,
									   @Param("updateTime") LocalDateTime updateTime,
									   @Param("optUser") String optUser);

	/**
	 * 查询人员下发任务
	 * @return 返回结果
	 */
	List<SmtIscDeviceTask> getPersonDown(@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询卡片下发任务
	 * @return 返回结果
	 */
	List<SmtIscDeviceTask> getCardDown(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询需要重试的下发任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return
	 */
	IPage<SmtIscDeviceTask> getReTryCardDown(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询删除任务
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询人员删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return
	 */
	IPage<SmtIscDeviceTask> getPersonDel(Page page, @Param("overTime") long overTime,
										 @Param("deviceType") int deviceType, @Param("serviceType") int serviceType);



	/**
	 * 查询延迟下发任务
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDelayDown(Page page,@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询延迟删除任务
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDelayDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询人员延迟删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return
	 */
	IPage<SmtIscDeviceTask> getDelayPersonDel(Page page, @Param("overTime") long overTime,
											  @Param("deviceType") int deviceType, @Param("serviceType") int serviceType);


	/**
	 * 查询需要删除的任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return
	 */
	IPage<SmtIscDeviceTask> getNeedDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);




	/**
	 * 查询设备CODE
	 * @return 返回结果
	 */
	List<String> getDeviceCode(@Param("query") DeviceTaskQueryDTO deviceTaskQueryDTO);

	/**
	 * 列表查询
	 * @param page
	 * @param taskDownRecordDTO
	 * @return
	 */
	IPage<TaskDownRecordVO> getPerson(Page page, @Param("query") TaskDownRecordDTO taskDownRecordDTO);

	/**
	 * 根据设备编号和卡片编号及状态查询
	 * @param deviceCode deviceCode
	 * @param cardNo cardNo
	 * @return 返回结果
	 */
	List<SmtIscDeviceTask> getDeviceTask(@Param("deviceCode") String deviceCode,@Param("cardNo") String cardNo);

	/**
	 * 获取列表
	 * @param smtDeviceTask smtDeviceTask
	 * @return 返回结果
	 */
	List<SmtIscDeviceTask> listSmtDeviceTask(@Param("query") SmtDeviceTask smtDeviceTask,@Param("parkIds") List<Integer> parkIds);


}
