package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtIscDeviceTaskService extends IService<SmtIscDeviceTask> {

	/**
	 * 插入任务信息
	 *
	 * @return 返回设备集合
	 */
	String saveTask(DeviceTaskVO deviceTaskVO);

	/**
	 * 保密区权限下发专用任务入口：按意图键接管旧批次后创建最新批次任务。
	 * 通用 {@link #saveTask(DeviceTaskVO)} 的去重语义保持不变。
	 */
	String saveSecurityAuthTask(DeviceTaskVO deviceTaskVO);

	boolean deleteTask(String deviceCode, String cardNo);

	/**
	 * 查询卡片下发任务
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	List<SmtIscDeviceTask> getCardDown(Page page, long overTime, int deviceType);

	IPage<SmtIscDeviceTask> getReTryCardDown(Page page, long overTime, int deviceType);

	/**
	 * 查询删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);


	/**
	 * 查询延迟下发任务
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDelayDown(Page page,@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询延迟删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtIscDeviceTask> getDelayDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 删除访客设备权限
	 * @param id
	 * @return
	 */
	Boolean delVisitorDeviceAuth(Long id);

	// ========== 新增优化查询方法 ==========

	/**
	 * 查询到期且可执行的删除任务
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 删除任务列表
	 */
	List<SmtIscDeviceTask> getDeleteTasks(int deviceType, long currentTime);

	/**
	 * 查询有效权限下发任务
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 有效权限任务列表
	 */
	List<SmtIscDeviceTask> getValidDownloadTasks(int deviceType, long currentTime);

	/**
	 * 查询过期任务（用于标记过期状态）
	 * @param deviceType 设备类型
	 * @param currentTime 当前时间
	 * @return 过期任务列表
	 */
	List<SmtIscDeviceTask> getExpiredTasks(int deviceType, long currentTime);

	/**
	 * 查询最近上线设备的待处理任务
	 * @param deviceType 设备类型
	 * @param deviceCodes 设备编码列表
	 * @return 待处理任务列表
	 */
	List<SmtIscDeviceTask> getRecentOnlineDeviceTasks(int deviceType, List<String> deviceCodes);

	/**
	 * 取消长期离线或已删除设备的待处理下发任务
	 * @param deviceType 设备类型
	 * @param staleMonths 超过多少个月未处理
	 * @return 取消任务数量
	 */
	int cancelStaleOfflineDownloadTasks(int deviceType, int staleMonths);

	/**
	 * 取消长期离线或已删除设备的待处理下发任务，并返回受影响（来源于入厂申请）的申请单ID集合，
	 * 供调用方对这些申请单逐单触发 {@code AdmittanceDispatchAggregator} 聚合回写——
	 * 纯批量落终态的任务会绕过单任务出口的聚合钩子，必须由调用方补触发。
	 * @param deviceType 设备类型
	 * @param staleMonths 超过多少个月未处理
	 * @return 受影响的入厂申请单ID集合（不含 apply_id 为空的任务，可能为空集合）
	 */
	Set<Long> cancelStaleOfflineDownloadTasksAndCollectApplyIds(int deviceType, int staleMonths);

	/**
	 * 停止已达到最大重试次数的待处理权限任务
	 * @param deviceType 设备类型
	 * @param maxRetryTimes 最大重试次数
	 * @param remark 停止重试说明
	 * @return 是否更新成功
	 */
	/**
	 * 回收滞留在"处理中"但已脱离正常轮询条件的孤儿任务（iscTaskId为空或code不在201/202），重置为初始化待重试
	 * @param deviceType 设备类型
	 * @param staleMinutes 滞留多少分钟以上才回收
	 * @return 回收任务数量
	 */
	int requeueOrphanDoingTasks(int deviceType, int staleMinutes);

	/**
	 * 将离线设备上的待下发任务标记为设备离线状态，便于前端展示原因；设备恢复在线后自动恢复调度
	 * @return 标记任务数量
	 */
	int markOfflineDeviceTasks(int deviceType);

	/**
	 * 将离线设备上的待下发任务标记为设备离线状态，并返回受影响（来源于入厂申请）的申请单ID集合，
	 * 供调用方逐单触发聚合回写——DEVICE_OFFLINE 虽非终态，但聚合把它计入失败判定
	 * （见 {@code AdmittanceDispatchAggregator.FAILURE_TERMINAL_STATUSES}），需要及时反映到申请单。
	 * @return 受影响的入厂申请单ID集合（可能为空集合）
	 */
	Set<Long> markOfflineDeviceTasksAndCollectApplyIds(int deviceType);

	/**
	 * 将OVER_TIME已过、仍处于待下发/设备离线状态的下发类任务收敛为已过期（删除类任务不受影响）
	 * @return 标记任务数量
	 */
	int expireOverdueDownloadTasks(int deviceType);

	/**
	 * 将OVER_TIME已过的下发类任务收敛为已过期，并返回受影响（来源于入厂申请）的申请单ID集合，
	 * 供调用方逐单触发聚合回写。
	 * @return 受影响的入厂申请单ID集合（可能为空集合）
	 */
	Set<Long> expireOverdueDownloadTasksAndCollectApplyIds(int deviceType);

	boolean stopExceededRetryAuthTasks(int deviceType, int maxRetryTimes, String remark);

	/**
	 * 停止已达到最大重试次数的待处理权限任务，并返回受影响（来源于入厂申请）的申请单ID集合，
	 * 供调用方逐单触发聚合回写。
	 * @return 受影响的入厂申请单ID集合（可能为空集合）
	 */
	Set<Long> stopExceededRetryAuthTasksAndCollectApplyIds(int deviceType, int maxRetryTimes, String remark);

}
