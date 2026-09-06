package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.platform.core.service.impl.DirectTaskCompletionService;
import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import com.tce.smart.schedule.service.platform.IDeviceTaskService;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.util.ToolUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTaskServiceImpl implements IDeviceTaskService {
 private com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard transportGuard;
 @org.springframework.beans.factory.annotation.Autowired public void setTransportGuard(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard){this.transportGuard=guard;}


    /** 独立于普通网络失败的非成功码，批处理显式跳过且不更新短信或派生业务。 */
    public static final int DIRECT_REVIEW_CODE=46012;
    private static boolean guarded(Result result){return result!=null&&Integer.valueOf(DIRECT_REVIEW_CODE).equals(result.getCode());}
    private Result admission(SmtDeviceTask task,LegacyIdentity identity) {
        try {
            if(transportGuard==null)throw new IllegalStateException("DIRECT 持久门禁未装配");
            Decision d=transportGuard.admitLegacyDirect(task==null?null:task.getId(),identity);
            if(d!=null && d.legacyAllowed())return null;
            return Result.builder().code(DIRECT_REVIEW_CODE).msg(d==null?"DIRECT_GATE_UNAVAILABLE":d.getReason()).build();
        } catch(RuntimeException unavailable) {return Result.builder().code(DIRECT_REVIEW_CODE).msg("DIRECT_GATE_UNAVAILABLE").build();}
    }
    /** 四个最终 HTTP 出口共用实际 wire 对照；准入事务已结束才允许外调。 */
    private Result dispatchLegacyGuarded(SmtDeviceTask task,DispatcherDTO<?> request) {
        LegacyIdentity.LegacyIdentityBuilder i=LegacyIdentity.of(task).toBuilder().wirePark(request.getParkId()).wireEnvelopeDevice(request.getDeviceId());
        Object data=request.getData();
        if(data instanceof CarCardDTO) {
            CarCardDTO d=(CarCardDTO)data;i.wireOperation(EventEnum.PARKING_ENTRANCE_AUTH_ADD.getCode().equals(request.getEventType())?"CAR_ADD":"INVALID")
                .wireDevice(d.getDeviceCode()).wireCard(d.getCardNo()).wireGeneral(d.getPlateLicence()).wireCardType(d.getCardType());
        } else if(data instanceof CarCardDelDTO) {
            CarCardDelDTO d=(CarCardDelDTO)data;i.wireOperation(EventEnum.PARKING_ENTRANCE_AUTH_DELETE.getCode().equals(request.getEventType())?"CAR_DELETE":"INVALID")
                .wireDevice(d.getDeviceCode()).wireCard(d.getCardNo());
        } else if(data instanceof CardDTO) {
            CardDTO d=(CardDTO)data;i.wireOperation(EventEnum.DEVICE_ADD_CARD.getCode().equals(request.getEventType())?"CARD_ADD":EventEnum.DEVICE_UPDATE_CARD.getCode().equals(request.getEventType())?"CARD_UPDATE":"INVALID")
                .wireDevice(d.getDeviceCode()).wireCard(d.getCardNo()).wireSerial(d.getSerialNo()).wireTask(d.getReqId()).wireGeneral(d.getPersonName()).wireCardType(d.getCardType());
            if(d.getValidTime()!=null)i.wireStart(d.getValidTime().getStartTime()).wireEnd(d.getValidTime().getEndTime());
        } else if(data instanceof CardDelDTO) {
            CardDelDTO d=(CardDelDTO)data;i.wireOperation(EventEnum.DEVICE_DELETE_CARD.getCode().equals(request.getEventType())?"CARD_DELETE":"INVALID")
                .wireDevice(d.getDeviceCode()).wireCard(d.getCardNo()).wireSerial(d.getSerialNo()).wireTask(d.getReqId());
        } else i.wireOperation("INVALID");
        LegacyIdentity identity=i.build();
        Result rejection=admission(task,identity);if(rejection!=null)return rejection;
        return remoteDispatcherService.dispatch(request,SecurityConstants.FROM_IN);
    }

	private final SmtDeviceTaskService smtDeviceTaskService;

	private final SmtDeviceService smtDeviceService;

	private final RemoteDispatcherService remoteDispatcherService;

	private final SmtImageService smtImageService;

	private final SmtVisitorService smtVisitorService;

	private final DirectTaskCompletionService directTaskCompletionService;

	private final RemoteStaffService remoteStaffService;

	@Override
	public void downCard() {
		List<SmtDeviceTask> taskList = new ArrayList<>();
		int success = 0;
		long begin = System.currentTimeMillis();

		//查询正常任务
		List<SmtDeviceTask> normalTaskList = smtDeviceTaskService.getDown(DateUtil.currentSeconds(),
				DeviceTaskConstants.CARD);
		taskList.addAll(normalTaskList);
		//查询延迟任务
		Page page = new Page(1,100);
		IPage<SmtDeviceTask> delayDownPage = smtDeviceTaskService.getDelayDown(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CARD);
		List<SmtDeviceTask> delayTaskList = delayDownPage.getRecords();
		taskList.addAll(delayTaskList);

		log.info("开始-卡片下发任务，总数：{},正常任务: {}, 延迟任务: {}", taskList.size(),normalTaskList.size(),delayTaskList.size());
		for (SmtDeviceTask task : taskList) {
			if(Objects.isNull(task.getTimes()) || StringUtils.isEmpty(task.getImageId())){
				continue;
			}
			long start = System.currentTimeMillis();
			Result result = this.down(task);
            if(guarded(result))continue;
			log.info("卡片下发，耗时：{}，cardNo：{}，deviceCode：{}，code：{}，message：{}", System.currentTimeMillis() - start,
					task.getCardNo(), task.getDeviceCode(), result.getCode(), result.getMsg());
			if (DeviceTaskEnum.BRIGE_ERROR.getCode().equals(result.getCode()) || CommonConstants.FAIL.equals(result.getCode())) {
				log.error("中断本次任务-卡片下发，code：{}, message：{}", result.getCode(), result.getMsg());
				continue;
			}
			success++;
		}

		log.info("完成-卡片下发任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}

	@Override
	public void downCar() {
		List<SmtDeviceTask> taskList = new ArrayList<>();
		int success = 0;
		long begin = System.currentTimeMillis();

		//查询正常任务
		List<SmtDeviceTask> normalTaskList = smtDeviceTaskService.getDown(DateUtil.currentSeconds(),
				DeviceTaskConstants.CAR);

		taskList.addAll(normalTaskList);

		//查询延迟任务
		Page page = new Page<>(1,100);
		IPage<SmtDeviceTask> delayDownPage = smtDeviceTaskService.getDelayDown(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CAR);
		List<SmtDeviceTask> delayTaskList = delayDownPage.getRecords();
		taskList.addAll(delayTaskList);

		log.info("开始-车辆下发任务，总数：{}, 正常任务: {}, 延迟任务: {}", taskList.size(),normalTaskList.size(),delayTaskList.size());
		for (SmtDeviceTask task : taskList) {

			long start = System.currentTimeMillis();
			Result result = this.down(task);
            if(guarded(result))continue;
			log.info("车辆下发，耗时：{}，cardNo：{}，deviceCode：{}，code：{}，message：{}", System.currentTimeMillis() - start,
					task.getCardNo(), task.getDeviceCode(), result.getCode(), result.getMsg());
			if (DeviceTaskEnum.BRIGE_ERROR.getCode().equals(result.getCode()) || CommonConstants.FAIL.equals(result.getCode())) {
				log.error("中断本次任务-车辆下发，code：{}, message：{}", result.getCode(), result.getMsg());
				return;
			}
			success++;
		}

		log.info("完成-车辆下发任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}

	@Override
	public void delCard() {
		Page page = new Page<>();
		page.setCurrent(1);
		page.setSize(200);
		long begin = System.currentTimeMillis();
		int success = 0;

		List<SmtDeviceTask> taskList = new ArrayList<>();
		//查询正常删除任务
		IPage<SmtDeviceTask> normalTaskList = smtDeviceTaskService.getDel(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CARD);
		List<SmtDeviceTask> normalList = normalTaskList.getRecords();
		taskList.addAll(normalList);

		//查询延迟删除任务
		IPage<SmtDeviceTask> delayTaskList = smtDeviceTaskService.getDelayDel(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CARD);
		List<SmtDeviceTask> delayList = delayTaskList.getRecords();
		taskList.addAll(delayList);

		log.info("开始-卡片删除任务，总数：{}, 正常任务: {}, 延迟任务: {}", taskList.size(),
				normalList.size(),delayList.size());
		for (SmtDeviceTask task : taskList) {

			long start = System.currentTimeMillis();
			Result result = this.del(task);
            if(guarded(result))continue;
			if (result.isSuccess()) {
				smtVisitorService.updateSmsCode(Long.valueOf(task.getCardNo()));
			}
			log.info("卡片删除，耗时：{}，cardNo：{}，deviceCode：{}，code：{}，message：{}", System.currentTimeMillis() - start,
					task.getCardNo(), task.getDeviceCode(), result.getCode(), result.getMsg());
			if (DeviceTaskEnum.BRIGE_ERROR.getCode().equals(result.getCode()) || CommonConstants.FAIL.equals(result.getCode())) {
				log.error("中断本次任务-卡片删除，code：{}, message：{}", result.getCode(), result.getMsg());
				return;
			}
			success++;
		}

		log.info("完成-卡片删除任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}


	@Override
	public void delCar() {
		Page page = new Page<>();
		page.setCurrent(1);
		page.setSize(200);
		long begin = System.currentTimeMillis();
		int success = 0;

		List<SmtDeviceTask> taskList = new ArrayList<>();

		//查询正常删除任务
		IPage<SmtDeviceTask> normalTaskList = smtDeviceTaskService.getDel(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CAR);
		List<SmtDeviceTask> normalList = normalTaskList.getRecords();
		taskList.addAll(normalList);

		//查询延迟删除任务
		IPage<SmtDeviceTask> delayTaskList = smtDeviceTaskService.getDelayDel(page, DateUtil.currentSeconds(),
				DeviceTaskConstants.CAR);
		List<SmtDeviceTask> delayList = delayTaskList.getRecords();
		taskList.addAll(delayList);

		log.info("开始-车辆删除任务，总数：{}, 正常任务: {}, 延迟任务: {}", taskList.size(),
				normalList.size(),delayList.size());
		for (SmtDeviceTask task : taskList) {
			long start = System.currentTimeMillis();
			Result result = this.del(task);
            if(guarded(result))continue;
			log.info("车辆删除，耗时：{}，cardNo：{}，deviceCode：{}，code：{}，message：{}", System.currentTimeMillis() - start,
					task.getCardNo(), task.getDeviceCode(), result.getCode(), result.getMsg());
			if (DeviceTaskEnum.BRIGE_ERROR.getCode().equals(result.getCode()) || CommonConstants.FAIL.equals(result.getCode())) {
				log.error("中断本次任务-车辆删除，code：{}, message：{}", result.getCode(), result.getMsg());
				return;
			}
			success++;
		}

		log.info("完成-车辆删除任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}


	/**
	 * 下发
	 *
	 * @param smtDeviceTask smtDeviceTask
	 */
	public Result down(SmtDeviceTask smtDeviceTask) {
  Result rejection=admission(smtDeviceTask,LegacyIdentity.of(smtDeviceTask));if(rejection!=null)return rejection;
		SmtDevice device = smtDeviceService.getOne(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getId,
				smtDeviceTask.getDeviceCode()));
		if (!directTaskCompletionService.recordDispatchAttempt(smtDeviceTask)) {
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode())
					.msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		if (ObjectUtil.isNotNull(device) && device.getConnectStatus().equals(DeviceConstants.ON_LINE)) {
			if (smtDeviceTask.getDeviceType().equals(DeviceTaskConstants.CARD)) {
				return this.downCard(smtDeviceTask, device.getParkId());
			} else if (smtDeviceTask.getDeviceType().equals(DeviceTaskConstants.CAR)) {
				return this.downCarCard(smtDeviceTask, device.getParkId());
			}
		}
		return Result.builder().code(DeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getCode()).msg(DeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getDesc()).build();
	}

	/**
	 * 删除
	 *
	 * @param smtDeviceTask smtDeviceTask
	 */
	public Result del(SmtDeviceTask smtDeviceTask) {
  Result rejection=admission(smtDeviceTask,LegacyIdentity.of(smtDeviceTask));if(rejection!=null)return rejection;
		SmtDevice device = smtDeviceService.getOne(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getId,
				smtDeviceTask.getDeviceCode()));
		if (!directTaskCompletionService.recordDispatchAttempt(smtDeviceTask)) {
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode())
					.msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		if (ObjectUtil.isNotNull(device) && device.getConnectStatus().equals(DeviceConstants.ON_LINE)) {
			if (smtDeviceTask.getDeviceType().equals(DeviceTaskConstants.CARD)) {
				return this.delCard(smtDeviceTask, device.getParkId());
			} else if (smtDeviceTask.getDeviceType().equals(DeviceTaskConstants.CAR)) {
				return this.delCarCard(smtDeviceTask, device.getParkId());
			}
		}
		return Result.builder().code(DeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getCode()).msg(DeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getDesc()).build();
	}

	/**
	 * 道闸下发
	 * 车辆下发没有更新的操作
	 *
	 * @param smtDeviceTask 任务信息
	 * @return Result
	 */
	public Result downCarCard(SmtDeviceTask smtDeviceTask, Integer parkId) {
        if(smtDeviceTask==null)return admission(null,null);
		if (null != smtDeviceTask.getStatus() && (smtDeviceTask.getStatus().equals(DeviceTaskConstants.DOWN_SUCCESS) || smtDeviceTask.getStatus().equals(DeviceTaskConstants.DOWN_STOP))) {
			boolean flag = false;
			log.info("重复数据-卡片车辆下发，时间：{}，请求参数：id：{}，times：{}，返回结果：{}", DateUtil.formatDateTime(DateUtil.date()), smtDeviceTask.getId(),
					smtDeviceTask.getTimes(), flag);
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode()).msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		CarCardDTO carCardDTO = new CarCardDTO();
		CarCardDTO.CarCardValid carCardValid = new CarCardDTO.CarCardValid();
		carCardValid.setStartTime(smtDeviceTask.getStartTime());
		carCardValid.setEndTime(smtDeviceTask.getOverTime());
		//时间不需要设置
		//carCardDTO.setValidTime(carCardValid);
		carCardDTO.setPlateLicence(smtDeviceTask.getGeneral());
		carCardDTO.setDeviceCode(smtDeviceTask.getDeviceCode());
		carCardDTO.setCardType(smtDeviceTask.getCardType());
		carCardDTO.setCardNo(smtDeviceTask.getCardNo());
		DateTime start = DateUtil.date();
		// 园区分发
		DispatcherDTO<CarCardDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.PARKING_ENTRANCE_AUTH_ADD.getCode());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(carCardDTO.getDeviceCode());
		dispatcherDTO.setData(carCardDTO);
		Result result = dispatchLegacyGuarded(smtDeviceTask, dispatcherDTO);
        if(guarded(result))return result;
		DateTime end = DateUtil.date();
		smtDeviceTask.setCode(null);
		smtDeviceTask.setRemark("未取得车辆执行结果");
		if (result != null && result.isSuccess() && result.getData() != null) {
			JSONObject object = JSONUtil.parseObj(result.getData());
			smtDeviceTask.setCode(object.getInt("code"));
			smtDeviceTask.setRemark(object.getStr("msg"));
		}
		smtDeviceTask.setConsume(DateUtil.betweenMs(start, end));
		boolean flag = this.downCarResultHandle(smtDeviceTask);
		log.info("状态修改-卡片车辆下发，修改时间：{}，请求参数：id：{}，result.code：{}，返回结果：{}", DateUtil.formatDateTime(DateUtil.date()), smtDeviceTask.getId()
				, result.getCode(), flag);
		return result;
	}

	/**
	 * 卡片下发结果处理
	 * TODO 这里需要注意 人脸卡片的下发是走异步回调的流程 接口调用成功不表示下发成功 则状态不能修改为成功
	 *
	 * @param task   原任务快照
	 * @param result result
	 */
	private boolean downCardResultHandle(SmtDeviceTask task, Result result, Long consume) {
		Integer code = result == null ? null : result.getCode();
		String remark = result == null ? "未取得卡片受理结果" : result.getMessage();
		if (result != null && result.isSuccess()) {
			code = DeviceTaskEnum.DEVICE_OK.getCode();
			remark = DeviceTaskEnum.DEVICE_OK.getDesc();
		}
		// 受理返回不能回写 INIT 或 SUCCESS，异步成功回执可能已经先到达。
		return directTaskCompletionService.recordResult(task, null, code, remark, consume);
	}

	/**
	 * 车辆下发结果处理
	 * TODO 这里车辆的下发任务为同步调用 如果调用返回的code不等于0时 应该不修改状态 等待下次下发
	 * 下发失败时不修改状态 等待下次下发
	 * @param task 任务快照
	 */
	public boolean downCarResultHandle(SmtDeviceTask task) {
		if (DeviceTaskEnum.DEVICE_OK.getCode().equals(task.getCode())) {
			return directTaskCompletionService.completeSuccess(task, task.getCode(),
					DeviceTaskEnum.DEVICE_OK.getDesc(), this::createVisitorCarDeleteTask);
		}
		Integer nextStatus = DeviceTaskEnum.DEVICE_VEHICLE_HAS_EXIST.getCode().equals(task.getCode())
				? DeviceTaskStatusEnum.FAIL.getCode() : null;
		return directTaskCompletionService.recordResult(task, nextStatus, task.getCode(), task.getRemark(), task.getConsume());
	}

	/** 仅实际新增成功后派生访客到期删除，保留现有车辆当日结束时间规则。 */
	private void createVisitorCarDeleteTask(SmtDeviceTask task) {
		if (!DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())
				&& !DeviceTaskActionEnum.DELAY_DOWN.getCode().equals(task.getAction())) {
			return;
		}
		SmtVisitor visitor = smtVisitorService.getOne(Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getId, task.getCardNo()));
		if (visitor == null) {
			return;
		}
		SmtDeviceTask deleteTask = new SmtDeviceTask();
		BeanUtil.copyProperties(task, deleteTask);
		deleteTask.setId(null);
		deleteTask.setSerialNo(UUID.randomUUID().toString().replace("-", ""));
		deleteTask.setOverTime(ToolUtils.getDateEndTime(visitor.getEndTime()).getTime() / 1000);
		deleteTask.setUpdateTime(null);
		deleteTask.setCode(null);
		deleteTask.setRemark("");
		deleteTask.setCreateTime(LocalDateTime.now());
		deleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		deleteTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		deleteTask.setTimes(0);
		deleteTask.setConsume(null);
		if (!smtDeviceTaskService.save(deleteTask)) {
			throw new IllegalStateException("访客车辆到期删除任务保存失败");
		}
	}


	/**
	 * 道闸删除
	 *
	 * @param smtDeviceTask smtDeviceTask
	 * @return Result
	 */
	public Result delCarCard(SmtDeviceTask smtDeviceTask, Integer parkId) {
        if(smtDeviceTask==null)return admission(null,null);
		if (ObjectUtil.isNull(smtDeviceTask)) {
			log.info("重复数据-卡片车辆删除，时间：{}，请求参数：id：{}，cardNo：{}，deviceCode：{}", DateUtil.formatDateTime(DateUtil.date()),
					smtDeviceTask.getId(), smtDeviceTask.getCardNo(), smtDeviceTask.getDeviceCode());
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode()).msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		CarCardDelDTO carCardDelDTO = new CarCardDelDTO();
		carCardDelDTO.setCardNo(smtDeviceTask.getCardNo());
		carCardDelDTO.setDeviceCode(smtDeviceTask.getDeviceCode());
		//Result result = remoteCarCardService.delete(carCardDelDTO, SecurityConstants.FROM_IN);
		// 园区分发
		DispatcherDTO<CarCardDelDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.PARKING_ENTRANCE_AUTH_DELETE.getCode());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(carCardDelDTO.getDeviceCode());
		dispatcherDTO.setData(carCardDelDTO);
		//这里需要主要 result的code=0表示成功 和 DeviceTaskEnum.DEVICE_OK 不一致
		Result result = dispatchLegacyGuarded(smtDeviceTask, dispatcherDTO);
        if(guarded(result))return result;
		smtDeviceTask.setCode(null);
		smtDeviceTask.setRemark("未取得车辆执行结果");
		if (result != null && result.isSuccess() && result.getData() != null) {
			JSONObject object = JSONUtil.parseObj(result.getData());
			smtDeviceTask.setCode(object.getInt("code"));
			smtDeviceTask.setRemark(object.getStr("msg"));
		}
		boolean flag = this.delCarResultHandle(smtDeviceTask);
		log.info("状态修改-卡片车辆删除，修改时间：{}，请求参数：cardNo：{}，deviceCode：{}，result.code：{}，返回结果：{}",
				DateUtil.formatDateTime(DateUtil.date()), carCardDelDTO.getCardNo(), carCardDelDTO.getDeviceCode(),
				result.getCode(), flag);
		return result;

	}

	/**
	 * 卡片删除调用结果处理
	 * 调用成功后 状态不修改  在回调成功时修改为成功
	 * @param result result
	 */
	private boolean delCardResultHandle(SmtDeviceTask task, Result result, Long consume) {
		return downCardResultHandle(task, result, consume);
	}


	/**
	 * 车辆删除结果处理
	 *
	 * @param task 任务快照
	 */
	private boolean delCarResultHandle(SmtDeviceTask task) {
		if (DeviceTaskEnum.DEVICE_VEHICLE_NOT_EXIST.getCode().equals(task.getCode())
				|| DeviceTaskEnum.DEVICE_OK.getCode().equals(task.getCode())) {
			return directTaskCompletionService.completeSuccess(task, task.getCode(),
					DeviceTaskEnum.desc(task.getCode()), null);
		}
		return directTaskCompletionService.recordResult(task, null, task.getCode(), task.getRemark(), task.getConsume());
	}

	/**
	 * 卡片下发
	 *
	 * @param smtDeviceTask 任务信息
	 * @return Result
	 */
	private Result downCard(SmtDeviceTask smtDeviceTask, Integer parkId) {
        if(smtDeviceTask==null)return admission(null,null);
		if (null != smtDeviceTask.getStatus() && smtDeviceTask.getStatus().equals(DeviceTaskConstants.DOING)) {
			boolean flag = false;
			log.info("重复数据-卡片人员下发，时间：{}，请求参数：id：{}，times：{}，返回结果：{}", DateUtil.formatDateTime(DateUtil.date()), smtDeviceTask.getId(),
					smtDeviceTask.getTimes(), flag);
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode()).msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		CardDTO cardDTO = new CardDTO();
		//通过imgId查询图片内容
		String base64Img = smtImageService.getImageBase64ByCode(smtDeviceTask.getImageId());
		cardDTO.setFaceImage(base64Img);
		cardDTO.setCardNo(smtDeviceTask.getCardNo());
		Result<SmtStaffDTO> staffInfo = remoteStaffService.getSimpleSttaffById(smtDeviceTask.getCardNo(), SecurityConstants.FROM_IN);
		if (staffInfo.isSuccess() && Objects.nonNull(staffInfo.getData()) && staffInfo.getData().getStatus() != -1) {
			String badge = staffInfo.getData().getBadge();
			if (NumberUtil.isNumber(badge)) {
				cardDTO.setEmployeeNo(Integer.parseInt(badge));
			}
		}
		cardDTO.setDeviceCode(smtDeviceTask.getDeviceCode());
		cardDTO.setSerialNo(smtDeviceTask.getSerialNo());
		cardDTO.setReqId(smtDeviceTask.getId());

		DispatcherDTO<CardDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());

		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(cardDTO.getDeviceCode());

		if(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode()) || smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())){
			//下发
			cardDTO.setPersonName(smtDeviceTask.getGeneral());
			cardDTO.setCardType(smtDeviceTask.getCardType());
			CardDTO.CardValid cardValid = new CardDTO.CardValid();
			cardValid.setStartTime(smtDeviceTask.getStartTime());
			cardValid.setEndTime(smtDeviceTask.getOverTime());
			cardDTO.setValidTime(cardValid);

			dispatcherDTO.setEventType(EventEnum.DEVICE_ADD_CARD.getCode());
		} else if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode()) || smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())){
			//更新
			dispatcherDTO.setEventType(EventEnum.DEVICE_UPDATE_CARD.getCode());
		}

		DateTime start = DateUtil.date();
		// 园区分发
		dispatcherDTO.setData(cardDTO);
		Result result = dispatchLegacyGuarded(smtDeviceTask, dispatcherDTO);
        if(guarded(result))return result;
		//Result(code=0, msg=success, data=true)
		DateTime end = DateUtil.date();
		boolean flag = this.downCardResultHandle(smtDeviceTask, result, DateUtil.betweenMs(start, end));
		log.info("状态修改-卡片人员下发，修改时间：{}，请求参数：id：{}，result.code：{}，返回结果：{}", DateUtil.formatDateTime(DateUtil.date()), smtDeviceTask.getId()
				, result.getCode(), flag);
		return result;
	}

	/**
	 * 卡片删除
	 *
	 * @param smtDeviceTask smtDeviceTask
	 * @return Result
	 */
	public Result delCard(SmtDeviceTask smtDeviceTask, Integer parkId) {
        if(smtDeviceTask==null)return admission(null,null);
		if (Objects.equals(smtDeviceTask.getStatus(),DeviceTaskConstants.DOING)) {
			boolean flag = false;
			log.info("重复数据-卡片人员删除，时间：{}，请求参数：id：{}，cardNo：{}，deviceCode：{}，返回结果：{}", DateUtil.formatDateTime(DateUtil.date()),
					smtDeviceTask.getId(), smtDeviceTask.getCardNo(), smtDeviceTask.getDeviceCode(), flag);
			return Result.builder().code(DeviceTaskEnum.REPEATED_ISSUANCE.getCode()).msg(DeviceTaskEnum.REPEATED_ISSUANCE.getDesc()).build();
		}
		CardDelDTO cardDelDTO = new CardDelDTO();
		cardDelDTO.setCardNo(smtDeviceTask.getCardNo());
		cardDelDTO.setDeviceCode(smtDeviceTask.getDeviceCode());
		cardDelDTO.setReqId(smtDeviceTask.getId());
		cardDelDTO.setSerialNo(smtDeviceTask.getSerialNo());
		DateTime start = DateUtil.date();
		// 园区分发
		DispatcherDTO<CardDelDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.DEVICE_DELETE_CARD.getCode());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(cardDelDTO.getDeviceCode());
		dispatcherDTO.setData(cardDelDTO);
		Result result = dispatchLegacyGuarded(smtDeviceTask, dispatcherDTO);
        if(guarded(result))return result;
		DateTime end = DateUtil.date();
		boolean flag = this.delCardResultHandle(smtDeviceTask, result, DateUtil.betweenMs(start, end));
		log.info("状态修改-卡片人员删除，修改时间：{}，请求参数：id：{}，result.code：{},返回结果：{}", DateUtil.formatDateTime(DateUtil.date()), smtDeviceTask.getId(), result.getCode(), flag);
		return result;
	}
}