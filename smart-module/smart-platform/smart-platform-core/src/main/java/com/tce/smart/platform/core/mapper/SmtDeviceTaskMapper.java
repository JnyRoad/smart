package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DeviceTaskQueryDTO;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.vo.ISCTaskDownRecordVO;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtDeviceTaskMapper extends BaseMapper<SmtDeviceTask> {

	/**
	 * 查询下发任务
	 * @return 返回结果
	 */
	List<SmtDeviceTask> getDown(@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询删除任务
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询延迟下发任务
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDelayDown(Page page,@Param("overTime") long overTime, @Param("deviceType") int deviceType);

	/**
	 * 查询延迟删除任务
	 * @return 返回结果
	 */
	IPage<SmtDeviceTask> getDelayDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);


	/**
	 * 查询需要删除的任务
	 * @param page
	 * @param overTime
	 * @param deviceType
	 * @return
	 */
	IPage<SmtDeviceTask> getNeedDel(Page page, @Param("overTime") long overTime, @Param("deviceType") int deviceType);




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

	IPage<ISCTaskDownRecordVO> getPersonForISC(Page page, @Param("query") TaskDownRecordDTO taskDownRecordDTO);

	/**
	 * 根据设备编号和卡片编号及状态查询
	 * @param deviceCode deviceCode
	 * @param cardNo cardNo
	 * @return 返回结果
	 */
	List<SmtDeviceTask> getDeviceTask(@Param("deviceCode") String deviceCode,@Param("cardNo") String cardNo);

	/**
	 * 获取列表
	 * @param smtDeviceTask smtDeviceTask
	 * @return 返回结果
	 */
	List<SmtDeviceTask> listSmtDeviceTask(@Param("query") SmtDeviceTask smtDeviceTask,@Param("parkIds") List<Integer> parkIds);


}
