package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtFellowVisitorMapper;
import com.tce.smart.platform.core.mapper.SmtIscDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
public class SmtIscDeviceTaskServiceImpl extends ServiceImpl<SmtIscDeviceTaskMapper, SmtIscDeviceTask> implements SmtIscDeviceTaskService {

	private static final String ISC_VEHICLE_AUTH_UNSUPPORTED_MESSAGE = "ISC车辆权限不支持下发";

	@Autowired
    private SmtIscDownRecordService smtIscDownRecordService;

	@Autowired
    private SmtDeviceMapper smtDeviceMapper;

	@Autowired
	private SmtVisitorMapper smtVisitorMapper;

	@Autowired
	private SmtFellowVisitorMapper smtFellowVisitorMapper;

	@Autowired
	private SmtAdmittanceFellowMapper smtAdmittanceFellowMapper;

	@Value("${smart.hf-park-id:0}")
	private Integer hfParkId;

	/**
	 * 对外提供的唯一任务入口
	 * @param deviceTaskVO
	 * @return
	 */
	@Override
	public String saveTask(DeviceTaskVO deviceTaskVO) {

		if (isIscVehicleAuthTask(deviceTaskVO.getDeviceType())) {
			return rejectVehicleAuthTask(deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(), deviceTaskVO.getAction());
		}

		//检查任务是否已存在
		if(checkTaskExists(deviceTaskVO)){
			return "任务已存在";
		}
		SmtDevice smtDevice = smtDeviceMapper.selectById(deviceTaskVO.getDeviceCode());

			if(hfParkId.equals(smtDevice.getParkId())){
				if(DeviceTaskConstants.CARD.equals(deviceTaskVO.getDeviceType()) && !isHefeiCardAccessServiceType(deviceTaskVO.getServiceType())){
					//非访客人脸照片 不下发
					return "合肥非访客人脸照片无需下发ISC权限";
				}
		}
		String applyBadge = deviceTaskVO.getApplyBadge();
		if (StringUtils.isEmpty(applyBadge)){

		}


		log.info("添加ISC任务，cardNo：{}，deviceCode：{}，action：{}", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
				deviceTaskVO.getAction());
		if (deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
				|| deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())
		) {
			SmtIscDeviceTask deviceTask = new SmtIscDeviceTask();
			BeanUtil.copyProperties(deviceTaskVO, deviceTask);
			deviceTask.setBadge(deviceTaskVO.getApplyBadge());
			setOpsUser(deviceTask);
			deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTask.setCreateTime(LocalDateTime.now());
			//生成新的下发任务
			this.save(deviceTask);
			return deviceTask.getId().toString();
		} else if(deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DEL.getCode()) ||
				deviceTaskVO.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
			//删除任务
			SmtIscDownRecord smtTaskDownRecord = findDeleteDownRecord(deviceTaskVO);
			if(null != smtTaskDownRecord){
				if (isIscVehicleAuthTask(smtTaskDownRecord.getDeviceType())) {
					return rejectVehicleAuthTask(deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
							deviceTaskVO.getAction());
				}
				//生成删除任务  注意这里删除操作是生成一条新的任务数据
				SmtIscDeviceTask deviceTask = new SmtIscDeviceTask();
				BeanUtil.copyProperties(smtTaskDownRecord, deviceTask);
				deviceTask.setId(null);
				deviceTask.setStartTime(deviceTaskVO.getStartTime());
				deviceTask.setOverTime(deviceTaskVO.getOverTime());
				deviceTask.setUpdateTime(null);
				deviceTask.setRemark("");
				deviceTask.setCreateTime(LocalDateTime.now());
				deviceTask.setAction(deviceTaskVO.getAction());
				deviceTask.setServiceType(deleteTaskServiceType(deviceTaskVO,
						smtTaskDownRecord.getDeviceType(), smtTaskDownRecord.getServiceType()));
				deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
				deviceTask.setBadge(deviceTaskVO.getApplyBadge());
				setOpsUser(deviceTask);
				this.save(deviceTask);
				return deviceTask.getId().toString();
			}
			SmtIscDeviceTask deviceTask = new SmtIscDeviceTask();
			BeanUtil.copyProperties(deviceTaskVO, deviceTask);
			if (isIscVehicleAuthTask(deviceTask.getDeviceType())) {
				return rejectVehicleAuthTask(deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
						deviceTaskVO.getAction());
			}
			deviceTask.setId(null);
			deviceTask.setCreateTime(LocalDateTime.now());
			deviceTask.setUpdateTime(null);
			deviceTask.setRemark("");
			deviceTask.setCode(null);
			deviceTask.setAction(deviceTaskVO.getAction());
			deviceTask.setServiceType(deleteTaskServiceType(deviceTaskVO,
					deviceTask.getDeviceType(), deviceTask.getServiceType()));
			deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTask.setBadge(deviceTaskVO.getApplyBadge());
			setOpsUser(deviceTask);
			this.save(deviceTask);
			log.info("未找到ISC下发记录，已生成兜底删除任务：cardNo={}, deviceCode={}", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode());
			return deviceTask.getId().toString();
		}
		return null;
	}

	private SmtIscDownRecord findDeleteDownRecord(DeviceTaskVO deviceTaskVO) {
		SmtIscDownRecord downRecord = smtIscDownRecordService.getOne(
				buildDeleteDownRecordQuery(deviceTaskVO, deviceTaskVO.getServiceType()));
		if (downRecord != null || !shouldLookupLegacyStaffFaceRecord(deviceTaskVO)) {
			return downRecord;
		}
		return smtIscDownRecordService.getOne(
				buildDeleteDownRecordQuery(deviceTaskVO, DeviceTaskConstants.UPDATE_FACE));
	}

	private LambdaQueryWrapper<SmtIscDownRecord> buildDeleteDownRecordQuery(DeviceTaskVO deviceTaskVO, Integer serviceType) {
		LambdaQueryWrapper<SmtIscDownRecord> downRecordQuery = Wrappers.<SmtIscDownRecord>query().lambda()
				.eq(SmtIscDownRecord::getCardNo, deviceTaskVO.getCardNo())
				.eq(SmtIscDownRecord::getDeviceCode, deviceTaskVO.getDeviceCode());
		if (deviceTaskVO.getDeviceType() != null) {
			downRecordQuery.eq(SmtIscDownRecord::getDeviceType, deviceTaskVO.getDeviceType());
		}
		if (serviceType != null) {
			downRecordQuery.eq(SmtIscDownRecord::getServiceType, serviceType);
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

	private boolean isHefeiCardAccessServiceType(Integer serviceType) {
		return DeviceTaskConstants.CARD_STAFF_IMPORT.equals(serviceType)
				|| DeviceTaskConstants.CARD_VISITOR.equals(serviceType)
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(serviceType)
				|| DeviceTaskConstants.UPDATE_FACE.equals(serviceType);
	}

	private boolean isIscVehicleAuthTask(Integer deviceType) {
		return isCarDeviceType(deviceType);
	}

	private String rejectVehicleAuthTask(String cardNo, String deviceCode, Integer action) {
		log.info("ISC车辆权限不支持下发，跳过车辆任务，cardNo：{}，deviceCode：{}，action：{}", cardNo, deviceCode, action);
		return ISC_VEHICLE_AUTH_UNSUPPORTED_MESSAGE;
	}

	private boolean checkTaskExists(DeviceTaskVO deviceTaskVO){
		//这里判断下是否已经存在了相同的任务
		// cardNo、deviceType、deviceCode、Action、(ImageId或general)、serviceType相同则为相同的任务
		List<Integer> status = new ArrayList<Integer>(){{add(DeviceTaskStatusEnum.DOING.getCode()); add(DeviceTaskStatusEnum.INIT.getCode());}};
		boolean downloadAction = DeviceTaskActionEnum.DOWN.getCode().equals(deviceTaskVO.getAction())
				|| DeviceTaskActionEnum.UPDATE.getCode().equals(deviceTaskVO.getAction())
				|| DeviceTaskActionEnum.DELAY_DOWN.getCode().equals(deviceTaskVO.getAction())
				|| DeviceTaskActionEnum.DELAY_UPDATE.getCode().equals(deviceTaskVO.getAction());
		LocalDateTime activeTaskStartTime = LocalDateTime.now().minusMonths(DeviceTaskConstants.OFFLINE_TASK_CANCEL_MONTHS);
		if(DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(deviceTaskVO.getDeviceType())) {
			//闸机
			int count = this.count(new LambdaQueryWrapper<SmtIscDeviceTask>()
					.eq(SmtIscDeviceTask::getCardNo, deviceTaskVO.getCardNo())
					.eq(SmtIscDeviceTask::getDeviceCode, deviceTaskVO.getDeviceCode())
					.eq(SmtIscDeviceTask::getAction, deviceTaskVO.getAction())
					.in(SmtIscDeviceTask::getStatus, status )
					.eq(SmtIscDeviceTask::getDeviceType, deviceTaskVO.getDeviceType())
					.eq(SmtIscDeviceTask::getServiceType, deviceTaskVO.getServiceType())
					.eq(SmtIscDeviceTask::getImageId, deviceTaskVO.getImageId())
					.ge(downloadAction, SmtIscDeviceTask::getCreateTime, activeTaskStartTime)
			);
			if(count > 0){
				log.info("ISC任务，cardNo：{}，deviceCode：{}，action：{} 已添加，不能重复添加", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
						deviceTaskVO.getAction());
				return true;
			}
		} else if(isCarDeviceType(deviceTaskVO.getDeviceType())){
			//道闸
			int count = this.count(new LambdaQueryWrapper<SmtIscDeviceTask>()
					.eq(SmtIscDeviceTask::getCardNo, deviceTaskVO.getCardNo())
					.eq(SmtIscDeviceTask::getDeviceCode, deviceTaskVO.getDeviceCode())
					.eq(SmtIscDeviceTask::getAction, deviceTaskVO.getAction())
					.in(SmtIscDeviceTask::getStatus, status )
					.eq(SmtIscDeviceTask::getDeviceType, deviceTaskVO.getDeviceType())
					.eq(SmtIscDeviceTask::getServiceType, deviceTaskVO.getServiceType())
					.eq(SmtIscDeviceTask::getGeneral, deviceTaskVO.getGeneral())
					.ge(downloadAction, SmtIscDeviceTask::getCreateTime, activeTaskStartTime)
			);
			if(count > 0){
				log.info("ISC任务，cardNo：{}，deviceCode：{}，action：{} 已添加，不能重复添加", deviceTaskVO.getCardNo(), deviceTaskVO.getDeviceCode(),
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
	public boolean deleteTask(String deviceCode, String cardNo) {
		//查询是否存在权限
		SmtIscDownRecord taskDownRecord = smtIscDownRecordService.getOne(new LambdaQueryWrapper<SmtIscDownRecord>()
				.eq(SmtIscDownRecord::getDeviceCode, deviceCode)
				.eq(SmtIscDownRecord::getCardNo, cardNo)
		);
		if(null != taskDownRecord){
			if (isIscVehicleAuthTask(taskDownRecord.getDeviceType())) {
				log.info("ISC车辆权限不支持下发，跳过车辆删除任务，cardNo：{}，deviceCode：{}", cardNo, deviceCode);
				return true;
			}
			//查询是否已生成删除任务
			int count = this.count(new LambdaQueryWrapper<SmtIscDeviceTask>()
					.eq(SmtIscDeviceTask::getDeviceCode, deviceCode)
					.eq(SmtIscDeviceTask::getCardNo, cardNo)
					.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
			);
			if(count == 0){
				// 查询下发任务记录
				SmtIscDeviceTask task = this.getById(taskDownRecord.getTaskId());
				//生成删除任务
				SmtIscDeviceTask deviceTask = new SmtIscDeviceTask();
				deviceTask.setId(null);
				deviceTask.setDeviceCode(taskDownRecord.getDeviceCode());
				deviceTask.setDeviceType(taskDownRecord.getDeviceType());
				deviceTask.setCardNo(taskDownRecord.getCardNo());
				deviceTask.setGeneral(taskDownRecord.getGeneral());
				deviceTask.setCreateTime(LocalDateTime.now());
				deviceTask.setServiceType(deleteTaskServiceType(
						taskDownRecord.getDeviceType(), taskDownRecord.getServiceType()));
				deviceTask.setPersonId(taskDownRecord.getPersonId());
				deviceTask.setUpdateTime(null);
				deviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
				deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
				//开始时间和过期时间为当前
				deviceTask.setStartTime(DateUtil.currentSeconds());
				deviceTask.setOverTime(DateUtil.currentSeconds());
				deviceTask.setBadge(Objects.nonNull(task) ? task.getBadge() : "");
				setOpsUser(deviceTask);
				return this.save(deviceTask);
			}
			return Boolean.TRUE;
		}
		int count = this.count(new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getDeviceCode, deviceCode)
				.eq(SmtIscDeviceTask::getCardNo, cardNo)
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
		);
		if(count > 0){
			return Boolean.TRUE;
		}
		List<SmtIscDeviceTask> sourceTasks = this.list(new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getDeviceCode, deviceCode)
				.eq(SmtIscDeviceTask::getCardNo, cardNo)
				.orderByDesc(SmtIscDeviceTask::getCreateTime)
		);
		SmtIscDeviceTask sourceTask = CollectionUtil.isEmpty(sourceTasks) ? null : sourceTasks.get(0);
		if (sourceTask != null && isIscVehicleAuthTask(sourceTask.getDeviceType())) {
			log.info("ISC车辆权限不支持下发，跳过车辆兜底删除任务，cardNo：{}，deviceCode：{}", cardNo, deviceCode);
			return true;
		}
		SmtIscDeviceTask deviceTask = new SmtIscDeviceTask();
		deviceTask.setId(null);
		deviceTask.setDeviceCode(deviceCode);
		deviceTask.setDeviceType(sourceTask == null ? DeviceTaskConstants.CARD : sourceTask.getDeviceType());
		deviceTask.setCardNo(cardNo);
		deviceTask.setGeneral(sourceTask == null ? null : sourceTask.getGeneral());
		deviceTask.setCreateTime(LocalDateTime.now());
		deviceTask.setServiceType(sourceTask == null ? DeviceTaskConstants.CARD
				: deleteTaskServiceType(sourceTask.getDeviceType(), sourceTask.getServiceType()));
		deviceTask.setPersonId(sourceTask == null ? null : sourceTask.getPersonId());
		deviceTask.setImageId(sourceTask == null ? null : sourceTask.getImageId());
		deviceTask.setUpdateTime(null);
		deviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		deviceTask.setStartTime(DateUtil.currentSeconds());
		deviceTask.setOverTime(DateUtil.currentSeconds());
		deviceTask.setBadge(sourceTask == null ? "" : sourceTask.getBadge());
		setOpsUser(deviceTask);
		log.info("未找到ISC下发记录，已生成兜底删除任务：cardNo={}, deviceCode={}", cardNo, deviceCode);
		return this.save(deviceTask);
	}

	@Override
	public List<SmtIscDeviceTask> getCardDown(Page page, long overTime, int deviceType){
		return  this.baseMapper.getCardDown(page, overTime,deviceType);
	}

	@Override
	public IPage<SmtIscDeviceTask> getReTryCardDown(Page page, long overTime, int deviceType) {
		return  this.baseMapper.getReTryCardDown(page, overTime,deviceType);
	}

	@Override
	public IPage<SmtIscDeviceTask> getDel(Page page, long overTime, int deviceType) {
		return this.baseMapper.getDel(page,overTime,deviceType);
	}

	@Override
	public IPage<SmtIscDeviceTask> getDelayDown(Page page,long overTime, int deviceType){
		return this.baseMapper.getDelayDown(page,overTime,deviceType);
	}

	@Override
	public IPage<SmtIscDeviceTask> getDelayDel(Page page, long overTime, int deviceType) {
		return this.baseMapper.getDelayDel(page,overTime,deviceType);
	}

	/**
	 * 	1. 删除已成功下发的任务
	 * 		1. 如果已生成删除任务，修改执行时间为当前
	 * 		2. 否则生成删除任务，执行时间为当前
	 * 	2. 停止正在下发中的任务
	 * @param id
	 * @return
	 */
	@Transactional
	@Override
	public Boolean delVisitorDeviceAuth(Long id) {
		List<String> temporaryPersonCertNos = resolveTemporaryPersonCertNos(id);
		//查询访客人脸和车辆已下发成功记录
		LambdaQueryWrapper<SmtIscDownRecord> queryWrapper = buildVisitorDownRecordQuery(id, temporaryPersonCertNos);
		List<SmtIscDownRecord> successList = smtIscDownRecordService.list(queryWrapper);
		successList.forEach(item -> {
			//查看是否已生成删除任务
			List<SmtIscDeviceTask> delTasks = filterReusableDeleteTasks(this.list(buildVisitorDeleteTaskQuery(item)));
			if(CollectionUtil.isNotEmpty(delTasks)){
					//修改删除操作执行时间为当前
					delTasks.forEach(task -> {
						task.setOverTime(DateUtil.currentSeconds());
						if (!Objects.equals(task.getStatus(), DeviceTaskStatusEnum.DOING.getCode())) {
							task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
							task.setIscTaskId(null);
							task.setRemark(null);
							task.setCode(null);
						}
						setOpsUser(task);
						this.updateById(task);
					});
			} else {
				//生成删除任务
				SmtIscDeviceTask newDeviceTask = new SmtIscDeviceTask();
				BeanUtils.copyProperties(item,newDeviceTask);
				newDeviceTask.setId(null);
				newDeviceTask.setCreateTime(LocalDateTime.now());
				newDeviceTask.setOverTime(DateUtil.currentSeconds());    //删除任务执行时间为当前
				newDeviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
				newDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
				newDeviceTask.setUpdateTime(null);
				newDeviceTask.setRemark(null);
				newDeviceTask.setCode(null);
				setOpsUser(newDeviceTask);
				this.save(newDeviceTask);
			}
		});

		//查询正在下发中的任务
		List<SmtIscDeviceTask> downingList = this.list(buildVisitorDowningTaskQuery(id, temporaryPersonCertNos));
		downingList.forEach(item -> {
			//修改下发中的任务为取消
			item.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
			item.setRemark(DeviceTaskStatusEnum.CANCEL.getDesc());
			setOpsUser(item);
			this.updateById(item);
		});
		return true;
	}

	private List<SmtIscDeviceTask> filterReusableDeleteTasks(List<SmtIscDeviceTask> delTasks) {
		List<SmtIscDeviceTask> reusableTasks = new ArrayList<>();
		if (CollectionUtil.isEmpty(delTasks)) {
			return reusableTasks;
		}
		for (SmtIscDeviceTask task : delTasks) {
			if (isReusableDeleteTask(task) && !hasReachedAuthConfigMaxRetryTimes(task)) {
				reusableTasks.add(task);
			}
		}
		return reusableTasks;
	}

	private boolean isReusableDeleteTask(SmtIscDeviceTask task) {
		Integer status = task.getStatus();
		return status == null
				|| Objects.equals(status, DeviceTaskStatusEnum.INIT.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DOING.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.FAIL.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private boolean hasReachedAuthConfigMaxRetryTimes(SmtIscDeviceTask task) {
		return task.getTimes() != null && task.getTimes() >= DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES;
	}

	private List<Integer> reusableDeleteStatusCodes() {
		return Arrays.asList(
				DeviceTaskStatusEnum.INIT.getCode(),
				DeviceTaskStatusEnum.DOING.getCode(),
				DeviceTaskStatusEnum.FAIL.getCode(),
				DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private LambdaQueryWrapper<SmtIscDownRecord> buildVisitorDownRecordQuery(Long id, List<String> temporaryPersonCertNos) {
		LambdaQueryWrapper<SmtIscDownRecord> queryWrapper = new LambdaQueryWrapper<SmtIscDownRecord>()
				.and(wrapper -> {
					wrapper.eq(SmtIscDownRecord::getCardNo, id);
					if (CollectionUtil.isNotEmpty(temporaryPersonCertNos)) {
						wrapper.or().in(SmtIscDownRecord::getBadge, temporaryPersonCertNos);
					}
					return wrapper;
				});
		appendVisitorAccessServiceTypes(queryWrapper);
		return queryWrapper;
	}

	private LambdaQueryWrapper<SmtIscDeviceTask> buildVisitorDeleteTaskQuery(SmtIscDownRecord downRecord) {
		LambdaQueryWrapper<SmtIscDeviceTask> queryWrapper = new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.eq(SmtIscDeviceTask::getDeviceType, downRecord.getDeviceType())
				.eq(SmtIscDeviceTask::getServiceType, downRecord.getServiceType())
				.eq(SmtIscDeviceTask::getDeviceCode, downRecord.getDeviceCode())
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getTimes)
						.or().lt(SmtIscDeviceTask::getTimes, DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES))
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getStatus)
						.or()
						.in(SmtIscDeviceTask::getStatus, reusableDeleteStatusCodes()));
		if (isTemporaryAccessRecord(downRecord)) {
			if (StringUtils.isNotEmpty(downRecord.getPersonId())) {
				return queryWrapper.eq(SmtIscDeviceTask::getPersonId, downRecord.getPersonId());
			}
			if (StringUtils.isNotEmpty(downRecord.getBadge())) {
				return queryWrapper.eq(SmtIscDeviceTask::getBadge, downRecord.getBadge());
			}
		}
		return queryWrapper.eq(SmtIscDeviceTask::getCardNo, downRecord.getCardNo());
	}

	private LambdaQueryWrapper<SmtIscDeviceTask> buildVisitorDowningTaskQuery(Long id, List<String> temporaryPersonCertNos) {
		LambdaQueryWrapper<SmtIscDeviceTask> queryWrapper = new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DOWN.getCode())
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
				.and(wrapper -> {
					wrapper.eq(SmtIscDeviceTask::getCardNo, id);
					if (CollectionUtil.isNotEmpty(temporaryPersonCertNos)) {
						wrapper.or().in(SmtIscDeviceTask::getBadge, temporaryPersonCertNos);
					}
					return wrapper;
				});
		appendVisitorAccessTaskServiceTypes(queryWrapper);
		return queryWrapper;
	}

	private void appendVisitorAccessServiceTypes(LambdaQueryWrapper<SmtIscDownRecord> queryWrapper) {
		queryWrapper.and(wrapper -> wrapper
				.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtIscDownRecord::getServiceType, Arrays.asList(
						DeviceTaskConstants.CARD_VISITOR,
						DeviceTaskConstants.CARD_ADMITTANCE)));
	}

	private void appendVisitorAccessTaskServiceTypes(LambdaQueryWrapper<SmtIscDeviceTask> queryWrapper) {
		queryWrapper.and(wrapper -> wrapper
				.eq(SmtIscDeviceTask::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtIscDeviceTask::getServiceType, Arrays.asList(
						DeviceTaskConstants.CARD_VISITOR,
						DeviceTaskConstants.CARD_ADMITTANCE)));
	}

	private List<String> resolveTemporaryPersonCertNos(Long id) {
		List<String> certNos = new ArrayList<>();
		if (id == null) {
			return certNos;
		}
		SmtVisitor visitor = smtVisitorMapper.selectById(id);
		if (visitor != null) {
			addCertNo(certNos, visitor.getCertNo());
		}
		SmtFellowVisitor fellowVisitor = smtFellowVisitorMapper.selectById(id);
		if (fellowVisitor != null) {
			addCertNo(certNos, fellowVisitor.getCertNo());
		}
		SmtAdmittanceFellow admittanceFellow = smtAdmittanceFellowMapper.selectById(id);
		if (admittanceFellow != null) {
			addCertNo(certNos, admittanceFellow.getCertNo());
		}
		return certNos;
	}

	private void addCertNo(List<String> certNos, String certNo) {
		if (StringUtils.isEmpty(certNo) || certNos.contains(certNo)) {
			return;
		}
		certNos.add(certNo);
	}

	private boolean isTemporaryAccessRecord(SmtIscDownRecord downRecord) {
		return downRecord != null
				&& DeviceTaskConstants.CARD.equals(downRecord.getDeviceType())
				&& (DeviceTaskConstants.CARD_VISITOR.equals(downRecord.getServiceType())
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(downRecord.getServiceType()));
	}

	/**
	 * 写入操作人
	 */
	private void setOpsUser(SmtIscDeviceTask deviceTask) {
		Authentication authentication = SecurityUtils.getAuthentication();
		SmartUser user = Objects.nonNull(authentication) ? SecurityUtils.getUser(authentication) : null;
		if(Objects.nonNull(user)){
			deviceTask.setOptUser(user.getUsername());
		} else {
			deviceTask.setOptUser("sys");
		}
		if(DeviceTaskConstants.CARD_VISITOR.equals(deviceTask.getServiceType())) {
			deviceTask.setOptUser("fkyy");
		}
	}

	// ========== 新增优化查询方法实现 ==========

	@Override
	public List<SmtIscDeviceTask> getDeleteTasks(int deviceType, long currentTime) {
		return this.baseMapper.getDeleteTasks(deviceType, currentTime);
	}

	@Override
	public List<SmtIscDeviceTask> getValidDownloadTasks(int deviceType, long currentTime) {
		return this.baseMapper.getValidDownloadTasks(deviceType, currentTime);
	}

	@Override
	public List<SmtIscDeviceTask> getExpiredTasks(int deviceType, long currentTime) {
		return this.baseMapper.getExpiredTasks(deviceType, currentTime);
	}

	@Override
	public List<SmtIscDeviceTask> getRecentOnlineDeviceTasks(int deviceType, List<String> deviceCodes) {
		return this.baseMapper.getRecentOnlineDeviceTasks(deviceType, deviceCodes);
	}

	@Override
	public int cancelStaleOfflineDownloadTasks(int deviceType, int staleMonths) {
		if (staleMonths <= 0) {
			log.warn("长期离线ISC下发任务自动取消阈值配置无效：{}个月", staleMonths);
			return 0;
		}
		List<SmtIscDeviceTask> staleTasks = this.baseMapper.getStaleOfflineDownloadTasks(deviceType, staleMonths);
		if (CollectionUtil.isEmpty(staleTasks)) {
			return 0;
		}
		LocalDateTime now = LocalDateTime.now();
		int canceledCount = 0;
		for (SmtIscDeviceTask task : staleTasks) {
			setOpsUser(task);
			canceledCount += this.baseMapper.cancelStaleOfflineDownloadTask(
					task.getId(),
					deviceType,
					staleMonths,
					DeviceTaskStatusEnum.CANCEL.getCode(),
					ISCDeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getCode(),
					"设备离线或已删除超过" + staleMonths + "个月，任务自动取消",
					now,
					task.getOptUser()
			);
		}
		log.info("已自动取消长期离线或已删除设备待处理ISC下发任务：{}条，查询命中：{}条，阈值：{}个月", canceledCount, staleTasks.size(), staleMonths);
		return canceledCount;
	}

	@Override
	public int requeueOrphanDoingTasks(int deviceType, int staleMinutes) {
		if (staleMinutes <= 0) {
			log.warn("孤儿任务回收滞留阈值配置无效：{}分钟", staleMinutes);
			return 0;
		}
		LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(staleMinutes);
		int requeued = this.baseMapper.update(null, Wrappers.<SmtIscDeviceTask>lambdaUpdate()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
				.set(SmtIscDeviceTask::getIscTaskId, null)
				.set(SmtIscDeviceTask::getCode, null)
				.set(SmtIscDeviceTask::getRemark, "系统回收：下发中状态滞留超时，已重新排队")
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.eq(SmtIscDeviceTask::getDeviceType, deviceType)
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.DOING.getCode())
				.lt(SmtIscDeviceTask::getUpdateTime, staleBefore)
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getIscTaskId)
						.or().notIn(SmtIscDeviceTask::getCode,
								ISCDeviceTaskEnum.AUTH_CONFIG_OK.getCode(),
								ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode())
						.or().isNull(SmtIscDeviceTask::getCode)));
		if (requeued > 0) {
			log.info("已回收滞留的ISC下发孤儿任务：{}条，设备类型：{}，滞留阈值：{}分钟", requeued, deviceType, staleMinutes);
		}
		return requeued;
	}

	@Override
	public int expireOverdueDownloadTasks(int deviceType) {
		// 待下发/设备离线状态且OVER_TIME已过的下发类任务不会再被任何调度查询选中，显式收敛为已过期；
		// 删除类任务（action 2/12）以OVER_TIME作为执行时间，不在过期范围
		int expired = this.baseMapper.update(null, Wrappers.<SmtIscDeviceTask>lambdaUpdate()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.EXPIRED.getCode())
				.set(SmtIscDeviceTask::getRemark, "权限有效期已过，任务自动标记为已过期")
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.eq(SmtIscDeviceTask::getDeviceType, deviceType)
				.in(SmtIscDeviceTask::getAction,
						DeviceTaskActionEnum.DOWN.getCode(),
						DeviceTaskActionEnum.UPDATE.getCode(),
						DeviceTaskActionEnum.DELAY_DOWN.getCode(),
						DeviceTaskActionEnum.DELAY_UPDATE.getCode())
				.isNotNull(SmtIscDeviceTask::getOverTime)
				.le(SmtIscDeviceTask::getOverTime, DateUtil.currentSeconds())
				.and(wrapper -> wrapper.in(SmtIscDeviceTask::getStatus,
								DeviceTaskStatusEnum.INIT.getCode(),
								DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode())
						.or().isNull(SmtIscDeviceTask::getStatus))
				.and(wrapper -> wrapper.ne(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.DEVICE_OK.getCode())
						.or().isNull(SmtIscDeviceTask::getCode)));
		if (expired > 0) {
			log.info("已将有效期已过的待下发ISC任务标记为过期：{}条，设备类型：{}", expired, deviceType);
		}
		return expired;
	}

	@Override
	public int markOfflineDeviceTasks(int deviceType) {
		int marked = this.baseMapper.markOfflineDeviceTasks(deviceType, LocalDateTime.now());
		if (marked > 0) {
			log.info("已将离线设备待下发任务标记为设备离线状态：{}条", marked);
		}
		return marked;
	}

	@Override
	public boolean stopExceededRetryAuthTasks(int deviceType, int maxRetryTimes, String remark) {
		if (maxRetryTimes <= 0) {
			log.warn("ISC权限任务最大重试次数配置无效：{}", maxRetryTimes);
			return false;
		}
		boolean updated = this.update(Wrappers.<SmtIscDeviceTask>lambdaUpdate()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.FAIL.getCode())
				.set(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode())
				.set(SmtIscDeviceTask::getRemark, remark)
				.set(SmtIscDeviceTask::getIscTaskId, null)
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.eq(SmtIscDeviceTask::getDeviceType, deviceType)
				.in(SmtIscDeviceTask::getAction,
						DeviceTaskActionEnum.DOWN.getCode(),
						DeviceTaskActionEnum.UPDATE.getCode(),
						DeviceTaskActionEnum.DELAY_DOWN.getCode(),
						DeviceTaskActionEnum.DELAY_UPDATE.getCode(),
						DeviceTaskActionEnum.DEL.getCode(),
						DeviceTaskActionEnum.DELAY_DEL.getCode())
				.and(wrapper -> wrapper.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
						.or().eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode())
						.or().isNull(SmtIscDeviceTask::getStatus))
				.and(wrapper -> wrapper.ne(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.DEVICE_OK.getCode())
						.or().isNull(SmtIscDeviceTask::getCode))
				.ge(SmtIscDeviceTask::getTimes, maxRetryTimes));
		if (updated) {
			log.info("已停止达到最大重试次数的ISC权限任务，设备类型：{}，最大重试次数：{}", deviceType, maxRetryTimes);
		}
		return updated;
	}
}
