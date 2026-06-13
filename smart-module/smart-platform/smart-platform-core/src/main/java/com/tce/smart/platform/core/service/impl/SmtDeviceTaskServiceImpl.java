package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.enums.StaffSyncEnum;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.platform.core.vo.ISCTaskDownRecordVO;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.util.RegexUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtDeviceTaskServiceImpl extends ServiceImpl<SmtDeviceTaskMapper, SmtDeviceTask> implements SmtDeviceTaskService {

	private final SmtTaskDownRecordService smtTaskDownRecordService;

	private final SmtIscDownRecordService iscDownRecordService;

	private final SmtDeviceAuthorityRelationMapper smtDeviceAuthorityRelationMapper;

	private final SmtDeviceMapper smtDeviceMapper;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	/**
	 * 设备权限任务创建统一入口
	 *
	 * 功能说明：
	 * 1. 根据设备的isSync字段自动判断设备类型：
	 *    - isSync=1：ISC设备，路由到SmtIscDeviceTaskService处理
	 *    - isSync=0：非ISC设备，使用当前服务直接处理
	 * 2. 执行任务重复性检查，避免创建重复任务
	 * 3. 根据任务类型(下发/删除/延迟操作)创建对应的任务记录
	 *
	 * 支持的操作类型：
	 * - DOWN(1)：立即下发权限
	 * - UPDATE(3)：更新权限
	 * - DEL(2)：删除权限
	 * - DELAY_DOWN(11)：延迟下发权限
	 * - DELAY_UPDATE(13)：延迟更新权限
	 * - DELAY_DEL(12)：延迟删除权限
	 *
	 * @param deviceTaskVO 设备任务参数对象，包含：
	 *                     - deviceCode：设备编码（必填）
	 *                     - cardNo：卡号/人员ID（必填）
	 *                     - action：操作类型（必填，见DeviceTaskActionEnum）
	 *                     - deviceType：设备类型（1-闸机，2-车辆）
	 *                     - serviceType：业务类型
	 *                     - imageId：图片ID（人脸权限需要）
	 *                     - general：通用字段（通常为姓名等）
	 *                     - startTime：开始时间（秒级时间戳）
	 *                     - overTime：结束时间（秒级时间戳）
	 * @return String 任务创建结果：
	 *                - 成功：返回任务ID（数字字符串）
	 *                - 失败：返回错误信息（如"设备xxx不存在"、"任务已存在"等）
	 *                - null：创建失败且无具体错误信息
	 */
	@Override
	public String saveTask(DeviceTaskVO deviceTaskVO) {

		// 判断设备是否为ISC同步的 是则把任务创建在新表中
		SmtDevice smtDevice = smtDeviceMapper.selectById(deviceTaskVO.getDeviceCode());
		if (smtDevice == null) {
			log.info("设备{}不存在", deviceTaskVO.getDeviceCode());
			return "设备" + deviceTaskVO.getDeviceCode() + "不存在";
		}

		// 添加详细的ISC设备判断日志
		log.info("设备任务路由判断 - 设备ID: {}, 设备名: {}, isSync: {}, 卡号: {}, 动作: {}",
				deviceTaskVO.getDeviceCode(), smtDevice.getDeviceName(), smtDevice.getIsSync(),
				deviceTaskVO.getCardNo(), deviceTaskVO.getAction());

		if (StaffSyncEnum.YES.getCode().equals(smtDevice.getIsSync())) {
			log.info("ISC设备路由 - 设备: {} 路由到ISC任务服务", deviceTaskVO.getDeviceCode());
			return smtIscDeviceTaskService.saveTask(deviceTaskVO);
		}

		log.info("非ISC设备路由 - 设备: {} 使用标准任务服务", deviceTaskVO.getDeviceCode());

		//检查任务是否已存在
		if (checkTaskExists(deviceTaskVO)) {
			return "任务已存在";
		}

		log.info("添加任务，cardNo：{}，deviceCode：{}，action：{}", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
				deviceTaskVO.getAction());
		//生成随机序列
		String sNo = UUID.randomUUID().toString().replaceAll("-", "");
		if (deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())
		) {
			SmtDeviceTask deviceTask = new SmtDeviceTask();
			BeanUtil.copyProperties(deviceTaskVO, deviceTask);
			if (StringUtils.isEmpty(deviceTask.getSerialNo())) {
				deviceTask.setSerialNo(sNo);
			}
			deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTask.setCreateTime(LocalDateTime.now());
			// 生成新的下发任务
			this.save(deviceTask);
			return deviceTask.getId().toString();
		} else if (deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DEL.getCode()) ||
				deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
			// 删除任务
			SmtTaskDownRecord smtTaskDownRecord = findDeleteDownRecord(deviceTaskVO);
			if (null != smtTaskDownRecord) {
				//生成删除任务  注意这里删除操作是生成一条新的任务数据
				SmtDeviceTask deviceTask = new SmtDeviceTask();
				BeanUtil.copyProperties(smtTaskDownRecord, deviceTask);
				if (null == deviceTaskVO.getSerialNo()) {
					deviceTask.setSerialNo(sNo);
				} else {
					deviceTask.setSerialNo(deviceTaskVO.getSerialNo());
				}
				deviceTask.setStartTime(deviceTaskVO.getStartTime());
				deviceTask.setOverTime(deviceTaskVO.getOverTime());
				deviceTask.setUpdateTime(null);
				deviceTask.setRemark("");
				deviceTask.setCreateTime(LocalDateTime.now());
				deviceTask.setAction(deviceTaskVO.getAction());
				deviceTask.setCardType(deviceTaskVO.getCardType());
				deviceTask.setServiceType(deleteTaskServiceType(deviceTaskVO,
						smtTaskDownRecord.getDeviceType(), smtTaskDownRecord.getServiceType()));
				deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
				this.save(deviceTask);
				return deviceTask.getId().toString();
			}
		}
		return null;
	}

	private SmtTaskDownRecord findDeleteDownRecord(DeviceTaskVO deviceTaskVO) {
		SmtTaskDownRecord downRecord = smtTaskDownRecordService.getOne(
				buildDeleteDownRecordQuery(deviceTaskVO, deviceTaskVO.getServiceType()));
		if (downRecord != null || !shouldLookupLegacyStaffFaceRecord(deviceTaskVO)) {
			return downRecord;
		}
		return smtTaskDownRecordService.getOne(
				buildDeleteDownRecordQuery(deviceTaskVO, DeviceTaskConstants.UPDATE_FACE));
	}

	private LambdaQueryWrapper<SmtTaskDownRecord> buildDeleteDownRecordQuery(DeviceTaskVO deviceTaskVO, Integer serviceType) {
		LambdaQueryWrapper<SmtTaskDownRecord> downRecordQuery = Wrappers.<SmtTaskDownRecord>query().lambda()
				.eq(SmtTaskDownRecord::getCardNo, deviceTaskVO.getCardNo())
				.eq(SmtTaskDownRecord::getDeviceCode, deviceTaskVO.getDeviceCode());
		if (deviceTaskVO.getDeviceType() != null) {
			downRecordQuery.eq(SmtTaskDownRecord::getDeviceType, deviceTaskVO.getDeviceType());
		}
		if (serviceType != null) {
			downRecordQuery.eq(SmtTaskDownRecord::getServiceType, serviceType);
		}
		return downRecordQuery;
	}

	private boolean shouldLookupLegacyStaffFaceRecord(DeviceTaskVO deviceTaskVO) {
		return DeviceTaskConstants.CARD.equals(deviceTaskVO.getDeviceType())
				&& DeviceTaskConstants.CARD_STAFF_IMPORT.equals(deviceTaskVO.getServiceType());
	}

	private Integer deleteTaskServiceType(DeviceTaskVO deviceTaskVO, Integer deviceType, Integer serviceType) {
		Integer taskServiceType = deviceTaskVO.getServiceType() == null ? serviceType : deviceTaskVO.getServiceType();
		return deleteTaskServiceType(deviceType, taskServiceType);
	}

	private Integer deleteTaskServiceType(Integer deviceType, Integer serviceType) {
		if (isStaffCardFaceServiceType(deviceType, serviceType)) {
			return DeviceTaskConstants.CARD_STAFF_IMPORT;
		}
		return serviceType;
	}

	private boolean isStaffCardFaceServiceType(Integer deviceType, Integer serviceType) {
		return DeviceTaskConstants.CARD.equals(deviceType)
				&& (DeviceTaskConstants.CARD_STAFF_IMPORT.equals(serviceType)
				|| DeviceTaskConstants.UPDATE_FACE.equals(serviceType));
	}

	public boolean checkTaskExists(DeviceTaskVO deviceTaskVO) {
		//这里判断下是否已经存在了相同的任务
		// cardNo、deviceType、deviceCode、Action、(ImageId或general)、serviceType相同则为相同的任务
		List<Integer> status = new ArrayList<Integer>(){{add(DeviceTaskStatusEnum.DOING.getCode()); add(DeviceTaskStatusEnum.INIT.getCode());}};
		if (DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(deviceTaskVO.getDeviceType())) {
			//闸机
			int count = this.count(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, deviceTaskVO.getCardNo())
					.eq(SmtDeviceTask::getDeviceCode, deviceTaskVO.getDeviceCode())
					.eq(SmtDeviceTask::getAction, deviceTaskVO.getAction())
					.in(SmtDeviceTask::getStatus, status )
					.eq(SmtDeviceTask::getDeviceType, deviceTaskVO.getDeviceType())
					.eq(SmtDeviceTask::getServiceType, deviceTaskVO.getServiceType())
					.eq(SmtDeviceTask::getImageId, deviceTaskVO.getImageId())
			);
			if (count > 0) {
				log.info("任务，cardNo：{}，deviceCode：{}，action：{} 已添加，不能重复添加", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
						deviceTaskVO.getAction());
				return true;
			}
		} else if (isCarDeviceType(deviceTaskVO.getDeviceType())) {
			//道闸
			int count = this.count(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo, deviceTaskVO.getCardNo())
					.eq(SmtDeviceTask::getDeviceCode, deviceTaskVO.getDeviceCode())
					.eq(SmtDeviceTask::getAction, deviceTaskVO.getAction())
					.eq(SmtDeviceTask::getDeviceType, deviceTaskVO.getDeviceType())
					.in(SmtDeviceTask::getStatus, status )
					.eq(SmtDeviceTask::getServiceType, deviceTaskVO.getServiceType())
					.eq(SmtDeviceTask::getGeneral, deviceTaskVO.getGeneral())
			);
			if (count > 0) {
				log.info("任务，cardNo：{}，deviceCode：{}，action：{} 已添加，不能重复添加", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
						deviceTaskVO.getAction());
				return true;
			}
		}
		return false;
	}

	private boolean isCarDeviceType(Integer deviceType) {
		return DeviceTaskConstants.CAR.equals(deviceType)
				|| DeviceTypeEnum.DEVICE_TYPE_3.getCode().equals(deviceType);
	}

	@Override
	public IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		IPage<TaskDownRecordVO> ipage = this.baseMapper.getPerson(page, taskDownRecordDTO);
		List<TaskDownRecordVO> records = ipage.getRecords();
		for (TaskDownRecordVO record : records) {
			record.setActionDesc(DeviceTaskActionEnum.desc(record.getAction()));
			String[] strings = record.getGeneral().split("-");
			if(strings.length > 1) {
				record.setGeneral(strings[1]);
			}
		}
		ipage.setRecords(records);
		return ipage;
	}

	@Override
	public IPage<ISCTaskDownRecordVO> getPersonForISC(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		// 调用baseMapper的getPersonForISC方法，获取分页数据
		IPage<ISCTaskDownRecordVO> ipage = this.baseMapper.getPersonForISC(page, taskDownRecordDTO);
		// 获取分页数据中的记录列表
		List<ISCTaskDownRecordVO> records = ipage.getRecords();
		// 遍历记录列表
		for (ISCTaskDownRecordVO record : records) {
			// 设置记录的动作描述
			record.setActionDesc(DeviceTaskActionEnum.desc(record.getAction()));
			// 将记录的general字段按"-"分割，并取第二个元素
			String[] strings = record.getGeneral().split("-");
			if(strings.length > 1) {
				record.setGeneral(strings[1]);
			}
		}
		// 将处理后的记录列表设置回分页数据中
		ipage.setRecords(records);
		// 返回分页数据
		return ipage;
	}

	@Override
	public boolean deleteTask(DeviceTaskDeleteDTO deviceTaskDeleteDTO) {
		//根据serialNo来生成删除任务
		if (ObjectUtil.isNotNull(deviceTaskDeleteDTO) && StringUtils.isNotEmpty(deviceTaskDeleteDTO.getCardNo()) && CollectionUtil.isNotEmpty(deviceTaskDeleteDTO.getDeviceCode())) {
			for (String deviceCode : deviceTaskDeleteDTO.getDeviceCode()) {
				//判断设备是否为ISC同步的 是则把任务创建在新表中
				SmtDevice smtDevice = smtDeviceMapper.selectById(deviceCode);
				if (StaffSyncEnum.YES.getCode().equals(smtDevice.getIsSync())) {
					smtIscDeviceTaskService.deleteTask(deviceCode, deviceTaskDeleteDTO.getCardNo());
					continue;
				}
				//查询是否存在权限
				SmtTaskDownRecord taskDownRecord = smtTaskDownRecordService.getOne(new LambdaQueryWrapper<SmtTaskDownRecord>()
						.eq(SmtTaskDownRecord::getDeviceCode, deviceCode)
						.eq(SmtTaskDownRecord::getCardNo, deviceTaskDeleteDTO.getCardNo())
				);
				if (null != taskDownRecord) {

					//查询是否已生成删除任务
					int count = this.count(new LambdaQueryWrapper<SmtDeviceTask>()
							.eq(SmtDeviceTask::getDeviceCode, deviceCode)
							.eq(SmtDeviceTask::getCardNo, deviceTaskDeleteDTO.getCardNo())
							.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
							.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
					);
					if (count == 0) {
						//生成删除任务
						SmtDeviceTask deviceTask = new SmtDeviceTask();
						deviceTask.setId(null);
						deviceTask.setDeviceCode(taskDownRecord.getDeviceCode());
						deviceTask.setDeviceType(taskDownRecord.getDeviceType());
						deviceTask.setCardNo(taskDownRecord.getCardNo());
						deviceTask.setGeneral(taskDownRecord.getGeneral());
						deviceTask.setCreateTime(LocalDateTime.now());
						deviceTask.setServiceType(deleteTaskServiceType(
								taskDownRecord.getDeviceType(), taskDownRecord.getServiceType()));
						deviceTask.setUpdateTime(null);
						deviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
						deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
						//开始时间和过期时间为当前
						deviceTask.setStartTime(DateUtil.currentSeconds());
						deviceTask.setOverTime(DateUtil.currentSeconds());
						String sNo = UUID.randomUUID().toString().replaceAll("-", "");
						deviceTask.setSerialNo(sNo);
						return this.save(deviceTask);
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public List<String> getDeviceCode(DeviceTaskQueryDTO deviceTaskQueryDTO) {
		return this.baseMapper.getDeviceCode(deviceTaskQueryDTO);
	}

	@Override
	public List<String> getDownDeviceCode(DeviceTaskQueryDTO deviceTaskQueryDTO) {
		List<SmtTaskDownRecord> taskDownRecords = smtTaskDownRecordService.list(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getCardNo, deviceTaskQueryDTO.getCardNo())
				.eq(SmtTaskDownRecord::getDeviceType, deviceTaskQueryDTO.getDeviceType())
		);
		if (CollectionUtil.isNotEmpty(taskDownRecords)) {
			return taskDownRecords.stream().map(SmtTaskDownRecord::getDeviceCode).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateStatus(DeviceTaskDTO deviceTaskDTO) {
		Integer status = DeviceTaskConstants.FAIL;
		SmtDeviceTask result = deviceTaskDTO.getData();
		if (ObjectUtil.isNull(result)) {
			return Boolean.FALSE;
		}
		if (ObjectUtil.isNull(result.getId()) || ObjectUtil.isNull(result.getAction())) {
			return Boolean.FALSE;
		}
		// 新方法
		if (ObjectUtil.isNotNull(result.getId())) {
			SmtDeviceTask deviceTask = this.getById(result.getId());
			if (ObjectUtil.isNotNull(deviceTask)) {
				deviceTask.setRemark(deviceTaskDTO.getMessage());
				deviceTask.setCode(deviceTaskDTO.getCode());
				deviceTask.setUpdateTime(LocalDateTime.now());
				result.setCode(deviceTaskDTO.getCode());
				if (result.isSuccess()) {
					status = DeviceTaskConstants.DOWN_SUCCESS;
				} else if (result.isFail()) {
					status = DeviceTaskConstants.DOWN_STOP;
				}
				deviceTask.setStatus(status);
				return this.updateById(deviceTask);
			}

		}
		return Boolean.FALSE;
	}

	@Override
	public boolean updateStatus(Integer id, Integer status, String remark, Integer code, Long consume, Integer action) {
		SmtDeviceTask smtDeviceTask = new SmtDeviceTask();
		smtDeviceTask.setId(id);
		smtDeviceTask.setStatus(status);
		smtDeviceTask.setRemark(remark);
		smtDeviceTask.setCode(code);
		smtDeviceTask.setConsume(consume);
		smtDeviceTask.setUpdateTime(LocalDateTime.now());
		Integer statusType = status;
		if (action.equals(DeviceTaskConstants.DEL)) {
			if (status.equals(DeviceTaskConstants.DOWN_STOP)) {
				statusType = DeviceTaskConstants.DOWN_SUCCESS;
			}
		}
		return this.updateById(smtDeviceTask);
	}

	/**
	 * 此处的执行逻辑为 发送到队列的请求已经成功 code=200 但是5分钟还未收到回调 则把code改为0 重新添加到可下发任务中
	 */
	@Override
	public void repeat() {

		//获取5分钟之前的时间
		Date fiveMiTime = ToolUtils.getCalDate(new Date(), Calendar.MINUTE, -5);

		//查询需要更改的任务数
		LambdaQueryWrapper<SmtDeviceTask> wapper = new LambdaQueryWrapper<SmtDeviceTask>()
				.eq(SmtDeviceTask::getCode, 200)
				.lt(SmtDeviceTask::getUpdateTime, fiveMiTime);

		wapper.and(wap -> wap.eq(SmtDeviceTask::getStatus, 0).or().isNull(SmtDeviceTask::getStatus));

		int count = this.count(wapper);

		log.info("当前待修改的任务数为:({})", count);

		if (count > 0) {
			SmtDeviceTask deviceTask = new SmtDeviceTask();
			deviceTask.setCode(0);
			this.update(deviceTask, wapper);
		}
	}

	@Override
	public List<SmtDeviceTask> getDown(long overTime, int deviceType) {
		return this.baseMapper.getDown(overTime, deviceType);
	}

	@Override
	public IPage<SmtDeviceTask> getDel(Page page, long overTime, int deviceType) {
		return this.baseMapper.getDel(page, overTime, deviceType);
	}

	@Override
	public IPage<SmtDeviceTask> getDelayDown(Page page, long overTime, int deviceType) {
		return this.baseMapper.getDelayDown(page, overTime, deviceType);
	}

	@Override
	public IPage<SmtDeviceTask> getDelayDel(Page page, long overTime, int deviceType) {
		return this.baseMapper.getDelayDel(page, overTime, deviceType);
	}

	@Override
	public Boolean updateStatusById(Integer taskId, Integer id) {
		// TODO Auto-generated method stub
		SmtDeviceTask selectById = this.baseMapper.selectById(taskId);
		if (ObjectUtil.isNotNull(selectById)) {
			log.info("####任务id为" + id + "重新加入下发列表中###");
			selectById.setStatus(DeviceTaskConstants.FAIL);
			selectById.setUpdateTime(LocalDateTime.now());
			selectById.updateById();
		}
		SmtTaskDownRecord byId = smtTaskDownRecordService.getById(id);
		if (ObjectUtil.isNotNull(byId)) {
			byId.setTaskType(DeviceTaskConstants.FAIL);
			byId.updateById();
		}
		return true;
	}

	@Override
	public void updateVehicleAuthDelay(SmtVehicle smtVehicle, Integer oldAuthId, Integer newAuthId, Integer serviceType) {
		updateDeviceAuth(newAuthId, oldAuthId, smtVehicle.getId().toString(), smtVehicle.getVehiclePlate(), serviceType, DeviceTaskConstants.CAR, true, null);
	}

	@Override
	public void updateVehicleAuth(SmtVehicle smtVehicle, Integer oldAuthId, Integer newAuthId, Integer serviceType) {
		updateDeviceAuth(newAuthId, oldAuthId, smtVehicle.getId().toString(), smtVehicle.getVehiclePlate(), serviceType, DeviceTaskConstants.CAR, false, null);
	}

	@Override
	public void updateStaffAuth(SmtStaff staff, Integer newAuthId, Integer oldAuthId, Integer serviceType) {
		if (StringUtils.isEmpty(staff.getFacePicId())) {
			//员工没有人脸 不生成下发任务
			return;
		}
		updateDeviceAuth(newAuthId, oldAuthId, staff.getId().toString(), staff.getBadge() + SymbolConstants.MINUS + staff.getName(), serviceType, DeviceTaskConstants.CARD, false, staff.getFacePicId());
	}

	@Override
	public void updateStaffAuth(SmtStaff staff, List<Integer> oldAuthIds, List<Integer> newAuthIds, Integer serviceType) {
		if (StringUtils.isEmpty(staff.getFacePicId())) {
			//员工没有人脸 不生成下发任务
			return;
		}
		String gen = staff.getBadge() + SymbolConstants.MINUS + staff.getName();
		updateDeviceAuth(newAuthIds, oldAuthIds, staff.getId().toString(), gen, serviceType, DeviceTaskConstants.CARD, false, staff.getFacePicId());
	}

	@Override
	public void updateStaffAuthNew(SmtStaff staff, List<Integer> oldAuthIds, List<Integer> newAuthIds,
								   Integer serviceType, String taskRecordNum, Integer type) {
		if (StringUtils.isEmpty(staff.getFacePicId())) {
			//员工没有人脸 不生成下发任务
			return;
		}
		String general = staff.getBadge() + SymbolConstants.MINUS + staff.getName();
		updateDeviceAuthNew(newAuthIds, oldAuthIds, staff.getId().toString(), serviceType,
				DeviceTaskConstants.CARD, false, staff.getFacePicId(), taskRecordNum, general, type);
	}

	@Override
	public void delStaffAuthDelay(SmtStaff staff, List<Integer> delAuthIds, Boolean isDelay, Integer serviceType) {
		if (CollectionUtil.isEmpty(delAuthIds)) {
			log.info("删除权限ID为空");
			return;
		}
		// 1. 查询权限关联的设备
		List<SmtDeviceAuthorityRelation> delDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, delAuthIds));
		List<String> delDev = delDeviceAuthorityRelations.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
		DeviceTaskActionEnum delAction = DeviceTaskActionEnum.DEL;
		if (isDelay) {
			delAction = DeviceTaskActionEnum.DELAY_DEL;
		}
		addDeviceDelTaskImmed(delDev, staff.getId().toString(), staff.getBadge() + SymbolConstants.MINUS + staff.getName(), serviceType, delAction.getCode(), SmtVisitorEnum.CARD_TYPE_1, DeviceTaskConstants.CARD, staff.getFacePicId());
	}

	@Transactional
	public void updateDeviceAuth(List<Integer> newAuthIds, List<Integer> oldAuthIds, String cardNo, String general, Integer serviceType, Integer deviceType, Boolean isDelay, String imageId) {
		// 1. 查询旧权限关联的设备
		List<String> oldDev = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(oldAuthIds)) {
			List<SmtDeviceAuthorityRelation> oldDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, oldAuthIds));
			oldDev = oldDeviceAuthorityRelations.stream().map(dev -> dev.getDeviceId()).collect(Collectors.toList());
		}
		// 2. 查询新权限关联的设备（为空时跳过查询，避免 IN () 非法 SQL）
		List<String> newDev = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(newAuthIds)) {
			List<SmtDeviceAuthorityRelation> newDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, newAuthIds));
			newDev = newDeviceAuthorityRelations.stream().map(dev -> dev.getDeviceId()).collect(Collectors.toList());
		}

		// 3. 比较差异
		//这里比较差异的逻辑是 先求 oldDev和newDev 的交集
		//oldDev去除掉交集则为需要删除权限的设备集合
		//newDev去掉交集则为需要添加权限的设备集合
		List<String> tempList = new ArrayList<>(oldDev);
		tempList.retainAll(newDev);
		oldDev.removeAll(tempList);
		newDev.removeAll(tempList);

		// 4. 生成下发新权限的任务
		DeviceTaskActionEnum downAction = DeviceTaskActionEnum.DOWN;
		if (isDelay) {
			downAction = DeviceTaskActionEnum.DELAY_DOWN;
		}
		addDeviceTask(newDev, cardNo, general, serviceType, downAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId, null);

		// 5. 生成旧权限删除的任务
		DeviceTaskActionEnum delAction = DeviceTaskActionEnum.DEL;
		if (isDelay) {
			delAction = DeviceTaskActionEnum.DELAY_DEL;
		}
		addDeviceDelTaskImmed(oldDev, cardNo, general, serviceType, delAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId);
	}

	@Override
	public List<SmtDeviceTask> getNewTask(List<String> cardNo, List<String> newDev) {
		List<SmtDeviceTask> getList = this.list(Wrappers.<SmtDeviceTask>query().lambda()
				.in(CollUtil.isNotEmpty(cardNo), SmtDeviceTask::getCardNo, cardNo)
				.in(CollUtil.isNotEmpty(newDev), SmtDeviceTask::getDeviceCode, newDev)
				.ge(SmtDeviceTask::getCreateTime, LocalDateTime.now().plusHours(-3))
				.eq(SmtDeviceTask::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT)
				.eq(SmtDeviceTask::getCardType, SmtVisitorEnum.CAR_CARD_TYPE_1.getType()));
		return getList;
	}

	@Transactional(rollbackFor = Exception.class)
	public void updateDeviceAuthNew(List<Integer> newAuthIds, List<Integer> oldAuthIds, String cardNo,
									Integer serviceType, Integer deviceType, Boolean isDelay, String imageId,
									String taskRecordNum, String general, Integer type) {
		List<String> oldDev = new ArrayList<>();

		// 1.查询旧权限关联的设备
		if (CollectionUtils.isNotEmpty(oldAuthIds)) {
			List<SmtDeviceAuthorityRelation> oldDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, oldAuthIds));
			oldDev = oldDeviceAuthorityRelations.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
		}

		// 2. 查询新权限关联的设备
		List<SmtDeviceAuthorityRelation> newDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, newAuthIds));
		List<String> newDev = newDeviceAuthorityRelations.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
		if (CollUtil.isEmpty(newDev)) {
			String[] idsArray = StringUtils.split(general, SymbolConstants.MINUS);
			SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
					.action(DeviceTaskActionEnum.DOWN.getCode())
					.status(DeviceTaskStatusEnum.CANCEL.getCode())
					.remark("分配权限设备列表为空")
					.badge(idsArray[0]).name(idsArray[1]).createTime(LocalDateTime.now()).taskListId(taskRecordNum).build();
			deviceTaskDetail.insert();
			return;
		}

		if (!type.equals(3)) {
			// 查询该员工已下发成功的设备列表
			List<SmtTaskDownRecord> taskDownList = smtTaskDownRecordService.list(new LambdaQueryWrapper<SmtTaskDownRecord>().eq(SmtTaskDownRecord::getCardNo, cardNo));
			List<String> deviceList = new ArrayList<>();
			if (CollUtil.isNotEmpty(taskDownList)) {
				deviceList.addAll(taskDownList.stream().map(SmtTaskDownRecord::getDeviceCode).collect(Collectors.toList()));
			}
			List<SmtIscDownRecord> iscDownRecordList = iscDownRecordService.list(new LambdaQueryWrapper<SmtIscDownRecord>().eq(SmtIscDownRecord::getCardNo, cardNo));
			if (CollUtil.isNotEmpty(iscDownRecordList)) {
				deviceList.addAll(iscDownRecordList.stream().map(SmtIscDownRecord::getDeviceCode).collect(Collectors.toList()));
			}
			// 3. 比较差异
			//这里比较差异的逻辑是 先求 oldDev和newDev 的交集
			//oldDev去除掉交集则为需要删除权限的设备集合
			//newDev去掉交集则为需要添加权限的设备集合
			List<String> tempList = new ArrayList<>(oldDev);
			tempList.retainAll(newDev);
			// 针对在设备中单独删除某个关联人员，可能导致这两者的设备列表不一致，求差集，然后放入新设备
			List<String> temp2List = new ArrayList<>(oldDev);
			temp2List.removeAll(deviceList);

			oldDev.removeAll(tempList);
			newDev.removeAll(tempList);
			if (CollUtil.isNotEmpty(temp2List)) {
				newDev.addAll(temp2List);
			}
		}

		// 5. 生成旧权限删除的任务
		DeviceTaskActionEnum delAction = DeviceTaskActionEnum.DEL;
		if (isDelay) {
			delAction = DeviceTaskActionEnum.DELAY_DEL;
		}

		addDeviceDelTaskImmed(oldDev, cardNo, general, serviceType, delAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId);

		// 4. 生成下发新权限的任务
		DeviceTaskActionEnum downAction = DeviceTaskActionEnum.DOWN;
		if (isDelay) {
			downAction = DeviceTaskActionEnum.DELAY_DOWN;
		}

		addDeviceTask(newDev, cardNo, general, serviceType, downAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId, taskRecordNum);
		if (CollUtil.isEmpty(newDev)) {
			String[] idsArray = StringUtils.split(general, SymbolConstants.MINUS);
			SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
					.action(DeviceTaskActionEnum.DOWN.getCode())
					.status(DeviceTaskStatusEnum.CANCEL.getCode())
					.remark("所分配权限已存在")
					.badge(idsArray[0]).name(idsArray[1]).createTime(LocalDateTime.now()).taskListId(taskRecordNum).build();
			deviceTaskDetail.insert();
		}

	}

	@Override
	public void updateStaffAuthDelay(SmtStaff staff, Integer newAuthId, Integer oldAuthId, Integer serviceType) {
		if (StringUtils.isEmpty(staff.getFacePicId())) {
			//员工没有人脸 不生成下发任务
			return;
		}
		updateDeviceAuth(newAuthId, oldAuthId, staff.getId().toString(), staff.getBadge() + SymbolConstants.MINUS + staff.getName(), serviceType, DeviceTaskConstants.CARD, true, staff.getFacePicId());
	}

	@Transactional
	public void updateDeviceAuth(Integer newAuthId, Integer oldAuthId, String cardNo, String general, Integer serviceType, Integer deviceType, Boolean isDelay, String imageId) {
		// 1. 查询旧权限关联的设备
		List<String> oldDev = new ArrayList<>();
		if (null != oldAuthId) {
			List<SmtDeviceAuthorityRelation> oldDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId, oldAuthId));
			oldDev = oldDeviceAuthorityRelations.stream().map(dev -> dev.getDeviceId()).collect(Collectors.toList());
		}
		// 2. 查询新权限关联的设备
		List<SmtDeviceAuthorityRelation> newDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId, newAuthId));
		List<String> newDev = newDeviceAuthorityRelations.stream().map(dev -> dev.getDeviceId()).collect(Collectors.toList());

		// 3. 比较差异
		//这里比较差异的逻辑是 先求 oldDev和newDev 的交集
		//oldDev去除掉交集则为需要删除权限的设备集合
		//newDev去掉交集则为需要添加权限的设备集合
		List<String> tempList = new ArrayList<>(oldDev);
		tempList.retainAll(newDev);
		oldDev.removeAll(tempList);
		newDev.removeAll(tempList);

		// 4. 生成下发新权限的任务
		DeviceTaskActionEnum downAction = DeviceTaskActionEnum.DOWN;
		if (isDelay) {
			downAction = DeviceTaskActionEnum.DELAY_DOWN;
		}
		addDeviceTask(newDev, cardNo, general, serviceType, downAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId, null);

		// 5. 生成旧权限删除的任务
		DeviceTaskActionEnum delAction = DeviceTaskActionEnum.DEL;
		if (isDelay) {
			delAction = DeviceTaskActionEnum.DELAY_DEL;
		}
		addDeviceDelTaskImmed(oldDev, cardNo, general, serviceType, delAction.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1, deviceType, imageId);

	}

	@Override
	public void addDeviceTask(List<String> devList, String cardNo, String general, Integer serviceType, Integer action,
							  SmtVisitorEnum smtVisitorEnum, Integer deviceType, String imageId, String taskRecordNum) {
		for (String devCode : devList) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(action);
			deviceTaskVO.setServiceType(serviceType);
			deviceTaskVO.setGeneral(general);
			deviceTaskVO.setCardNo(cardNo);
			deviceTaskVO.setDeviceCode(devCode);
			deviceTaskVO.setCardType(smtVisitorEnum.getType());
			deviceTaskVO.setDeviceType(deviceType);
			deviceTaskVO.setImageId(imageId);
			deviceTaskVO.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);
			deviceTaskVO.setStartTime(DateUtil.currentSeconds());
			String taskId = saveTask(deviceTaskVO);
			if (StringUtils.isNotEmpty(taskRecordNum)) {
				String[] idsArray = StringUtils.split(general, SymbolConstants.MINUS);
				String badge = Objects.nonNull(idsArray) && idsArray.length > 0 ? idsArray[0] : "";
				String name = Objects.nonNull(idsArray) && idsArray.length > 1 ? idsArray[1] : "";
				DeviceVO device = smtDeviceMapper.getDeviceDetail(devCode);
				String deviceName = Objects.nonNull(device) && StringUtils.isNotEmpty(device.getDeviceName()) ? device.getDeviceName() : devCode;
				String areaName = Objects.nonNull(device) && StringUtils.isNotEmpty(device.getAreaName()) ? device.getAreaName() : "";
				if (RegexUtils.matchNumber(taskId)) {
					SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
							.status(DeviceTaskStatusEnum.INIT.getCode())
							.action(action)
							.taskId(taskId)
							.badge(badge).name(name)
							.deviceName(deviceName).areaName(areaName)
							.createTime(LocalDateTime.now())
							.taskListId(taskRecordNum).build();
					deviceTaskDetail.insert();
				} else {
					SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
							.status(DeviceTaskStatusEnum.FAIL.getCode())
							.action(action)
							.remark(taskId)
							.badge(badge).name(name)
							.deviceName(deviceName).areaName(areaName)
							.createTime(LocalDateTime.now())
							.taskListId(taskRecordNum).build();
					deviceTaskDetail.insert();
				}
			}
		}
	}

	@Override
	public void addDeviceDelTaskImmed(List<String> devList, String cardNo, String general, Integer serviceType, Integer action, SmtVisitorEnum smtVisitorEnum, Integer deviceType, String imageId) {
		for (String devCode : devList) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(action);
			deviceTaskVO.setServiceType(serviceType);
			deviceTaskVO.setGeneral(general);
			deviceTaskVO.setCardNo(cardNo);
			deviceTaskVO.setDeviceCode(devCode);
			deviceTaskVO.setCardType(smtVisitorEnum.getType());
			deviceTaskVO.setDeviceType(deviceType);
			deviceTaskVO.setImageId(imageId);
			deviceTaskVO.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTaskVO.setOverTime(DateUtil.currentSeconds());
			deviceTaskVO.setStartTime(DateUtil.currentSeconds());
			saveTask(deviceTaskVO);
		}
	}

	/**
	 * 1. 删除已成功下发的任务
	 * 1. 如果已生成删除任务，修改执行时间为当前
	 * 2. 否则生成删除任务，执行时间为当前
	 * 2. 停止正在下发中的任务
	 *
	 * @param id
	 * @return
	 */
	@Transactional
	@Override
	public Boolean delVisitorDeviceAuth(Long id) {
		//查询访客记录
		//查询访客人脸和车辆已下发成功记录
			LambdaQueryWrapper<SmtTaskDownRecord> queryWrapper = new LambdaQueryWrapper<SmtTaskDownRecord>()
					.eq(SmtTaskDownRecord::getCardNo, id)
					.and(wrapper -> wrapper
						.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
						.in(SmtTaskDownRecord::getServiceType, Arrays.asList(
								DeviceTaskConstants.CARD_VISITOR,
								DeviceTaskConstants.CARD_ADMITTANCE))
						.or()
						.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CAR)
						.in(SmtTaskDownRecord::getServiceType, Arrays.asList(
								DeviceTaskConstants.CAR_VISITOR,
								DeviceTaskConstants.CAT_ADMITTANCE)));
		List<SmtTaskDownRecord> successList = smtTaskDownRecordService.list(queryWrapper);
			successList.forEach(item -> {
				//查看是否已生成删除任务
				List<SmtDeviceTask> delTasks = filterReusableDeleteTasks(this.list(new LambdaQueryWrapper<SmtDeviceTask>()
							.eq(SmtDeviceTask::getCardNo, item.getCardNo())
						.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
						.eq(SmtDeviceTask::getDeviceType, item.getDeviceType())
						.eq(SmtDeviceTask::getServiceType, item.getServiceType())
						.eq(SmtDeviceTask::getDeviceCode, item.getDeviceCode())
						.and(wrapper -> wrapper.isNull(SmtDeviceTask::getStatus)
								.or()
								.in(SmtDeviceTask::getStatus, reusableDeleteStatusCodes()))
				));
				if (CollectionUtil.isNotEmpty(delTasks)) {
					//修改删除操作执行时间为当前
					delTasks.forEach(task -> {
						task.setOverTime(DateUtil.currentSeconds());
						if (!Objects.equals(task.getStatus(), DeviceTaskStatusEnum.DOING.getCode())) {
							task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
							task.setRemark(null);
							task.setCode(null);
						}
						this.updateById(task);
					});
				} else {
				//生成删除任务
				SmtDeviceTask newDeviceTask = new SmtDeviceTask();
				BeanUtils.copyProperties(item, newDeviceTask);
				String sNo = UUID.randomUUID().toString().replaceAll("-", "");
				newDeviceTask.setId(null);
				newDeviceTask.setCreateTime(LocalDateTime.now());
				newDeviceTask.setOverTime(DateUtil.currentSeconds());    //删除任务执行时间为当前
				newDeviceTask.setSerialNo(sNo);
				newDeviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
				newDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
				newDeviceTask.setUpdateTime(null);
				newDeviceTask.setRemark(null);
				newDeviceTask.setCode(null);
				this.save(newDeviceTask);
			}
		});

		//查询正在下发中的任务
		List<SmtDeviceTask> downingList = this.list(new LambdaQueryWrapper<SmtDeviceTask>()
				.eq(SmtDeviceTask::getCardNo, id)
				.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DOWN.getCode())
				.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
		);
		downingList.forEach(item -> {
			//修改下发中的任务为取消
			item.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
			item.setRemark(DeviceTaskStatusEnum.CANCEL.getDesc());
			this.updateById(item);
		});

		smtIscDeviceTaskService.delVisitorDeviceAuth(id);
		return true;
	}

	private List<SmtDeviceTask> filterReusableDeleteTasks(List<SmtDeviceTask> delTasks) {
		List<SmtDeviceTask> reusableTasks = new ArrayList<>();
		if (CollectionUtil.isEmpty(delTasks)) {
			return reusableTasks;
		}
		for (SmtDeviceTask task : delTasks) {
			if (isReusableDeleteTask(task)) {
				reusableTasks.add(task);
			}
		}
		return reusableTasks;
	}

	private boolean isReusableDeleteTask(SmtDeviceTask task) {
		Integer status = task.getStatus();
		return status == null
				|| Objects.equals(status, DeviceTaskStatusEnum.INIT.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DOING.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.FAIL.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private List<Integer> reusableDeleteStatusCodes() {
		return Arrays.asList(
				DeviceTaskStatusEnum.INIT.getCode(),
				DeviceTaskStatusEnum.DOING.getCode(),
				DeviceTaskStatusEnum.FAIL.getCode(),
				DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}
}
