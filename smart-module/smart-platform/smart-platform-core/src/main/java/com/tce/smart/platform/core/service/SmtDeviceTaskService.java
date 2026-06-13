package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.vo.ISCTaskDownRecordVO;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtDeviceTaskService extends IService<SmtDeviceTask> {

	/**
	 * 插入任务信息
	 *
	 * @return 返回设备集合
	 */
	String saveTask(DeviceTaskVO deviceTaskVO);


	/**
	 * 删除任务信息
	 * deviceTaskDeleteDTO 设备任务信息
	 *
	 * @return 返回设备集合
	 */
	boolean deleteTask(DeviceTaskDeleteDTO deviceTaskDeleteDTO);

	/**
	 * 查询设备CODE
	 *
	 * @return 返回结果
	 */
	List<String> getDeviceCode(DeviceTaskQueryDTO deviceTaskQueryDTO);

	/**
	 * 查询设备CODE
	 *
	 * @return 返回结果
	 */
	List<String> getDownDeviceCode(DeviceTaskQueryDTO deviceTaskQueryDTO);


	/**
	 * 修改设备状态
	 *
	 * @param deviceTaskDTO 信息封装
	 */
	boolean updateStatus(DeviceTaskDTO deviceTaskDTO);


	boolean updateStatus(Integer id, Integer status, String remark, Integer code, Long consume,Integer action);

	/**
	 * 修改设备状态为待下发状态
	 */
	void repeat();

	/**
	 * 查询下发任务
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	List<SmtDeviceTask> getDown(long overTime, int deviceType);

	/**
	 * 查询删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);


	/**
	 * 查询延迟下发任务
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDelayDown(Page page,@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询延迟删除任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDelayDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);


	/**
	 * 根据id将status修改为0
	 * @param taskId
	 * @param id
	 * @return
	 */
	Boolean updateStatusById(Integer taskId, Integer id);

	/**
	 * 更新车辆权限
	 * @param smtVehicle
	 * @param newAuthId
	 */
	void updateVehicleAuthDelay(SmtVehicle smtVehicle,Integer oldAuthId,  Integer newAuthId,Integer serviceType);

	/**
	 * 更新车辆权限-延迟下发
	 * @param smtVehicle
	 * @param newAuthId
	 */
	void updateVehicleAuth(SmtVehicle smtVehicle,Integer oldAuthId,  Integer newAuthId,Integer serviceType);

	/**
	 * 更新员工人脸权限
	 * @param staff
	 * @param newAuthId
	 * @param oldAuthId
	 */
	void updateStaffAuth(SmtStaff staff,Integer newAuthId, Integer oldAuthId,Integer serviceType);

	/**
	 * 直接更新员工人脸权限
	 * @param staff
	 * @param oldAuthIds
	 * @param newAuthIds
	 */
	void updateStaffAuth(SmtStaff staff, List<Integer> oldAuthIds, List<Integer> newAuthIds, Integer serviceType);

	void updateStaffAuthNew(SmtStaff staff, List<Integer> oldAuthIds, List<Integer> newAuthIds,
							Integer serviceType, String taskRecordNum, Integer type);

	/**
	 * 获得最新任务进度
	 * @param cardNo
	 * @param newDev
	 * @return
	 */
	List<SmtDeviceTask> getNewTask(List<String> cardNo, List<String> newDev);

	/**
	 * 更新员工人脸权限-延迟下发
	 * @param staff
	 * @param newAuthId
	 * @param oldAuthId
	 */
	void updateStaffAuthDelay(SmtStaff staff,Integer newAuthId, Integer oldAuthId,Integer serviceType);

	/**
	 * 删除员工人脸权限-延迟下发
	 * @param staff
	 * @param delAuthIds
	 * @param isDelay
	 * @param serviceType
	 */
	void delStaffAuthDelay(SmtStaff staff, List<Integer> delAuthIds, Boolean isDelay, Integer serviceType);

	/**
	 * 设备生成权限
	 * @param devList
	 * @param cardNo
	 * @param general
	 * @param serviceType
	 * @param action
	 * @param smtVisitorEnum
	 * @param deviceType
	 */
	void addDeviceTask(List<String> devList, String cardNo, String general, Integer serviceType, Integer action, SmtVisitorEnum smtVisitorEnum, Integer deviceType,String imageId, String taskRecordNum);

	/**
	 * 生成设备权限删除任务
	 * @param devList
	 * @param cardNo
	 * @param general
	 * @param serviceType
	 * @param action
	 * @param smtVisitorEnum
	 * @param deviceType
	 */
	void addDeviceDelTaskImmed(List<String> devList, String cardNo, String general, Integer serviceType, Integer action, SmtVisitorEnum smtVisitorEnum, Integer deviceType,String imageId);

	/**
	 * 删除访客设备权限
	 * @param id
	 * @return
	 */
	Boolean delVisitorDeviceAuth(Long id);

	IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO);

	IPage<ISCTaskDownRecordVO> getPersonForISC(Page page, TaskDownRecordDTO taskDownRecordDTO);

}
