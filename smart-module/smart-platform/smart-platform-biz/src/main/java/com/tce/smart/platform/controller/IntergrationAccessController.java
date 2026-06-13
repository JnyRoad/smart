package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.core.dto.SaveSnapPersonDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @description: IntergrationController
 * access下发回调处理
 * @date: 2020-07-02 16:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/inner/access")
public class IntergrationAccessController extends BaseController {

	@Resource
	private SmtSnapPersonService smtSnapPersonService;

	@Resource
	private ImageService imageService;

	@Resource
	private SmtDeviceTaskService smtDeviceTaskService;

	@Resource
	private SmtTaskDownRecordService smtTaskDownRecordService;

	@Resource
	private SmtIscDownRecordService smtIscDownRecordService;

	@Resource
	private SmtVisitorService smtVisitorService;

	@Resource
	private SmtStaffService smtStaffService;

	@Resource
	private SmtFellowVisitorService smtFellowVisitorService;

	@Resource
	private SmtAdmittanceFellowService smtAdmittanceFellowService;

	@Resource
	private SmtAdmittanceApplyService smtAdmittanceApplyService;

	@Resource
	private SmtAdmittanceVehicleService smtAdmittanceVehicleService;
	/**
	 * 接收卡片新增结果
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/card/add/reply")
	public Result<Boolean> replyOfAddCard(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收到卡片新增结果{}", bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			return fail("卡片新增结果收到数据为空");
		}
		return doReplyOfCard(bridgeListenerDTO.getContent());
	}

	/**
	 * 接收卡片删除结果
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/card/delete/reply")
	public Result<Boolean> replyOfDeleteCard(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收到卡片删除结果{}", bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			return fail("卡片修改结果收到数据为空");
		}
		return doReplyOfCard(bridgeListenerDTO.getContent());
	}

	/**
	 * 接收卡片更新结果
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/card/update/reply")
	public Result<Boolean> replyOfUpdateCard(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收到卡片修改结果{}", bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			return fail("卡片修改结果收到数据为空");
		}
		return doReplyOfCard(bridgeListenerDTO.getContent());
	}

	/**
	 * 接收人员通行记录通知
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/log/reply")
	public Result<Boolean> replyOfAccess(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		log.info("接收人员通行记录{}", bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			throw new TCEException("人员通行记录收到数据为空");
		}

		/**
		 * {
		 *      "cardNo":"001", //卡片编号【必选】远程开门或手动按钮开门时该值可能为空
		 *      "deviceCode":"5A8EAB9B60C24B69A5FFB0AC45872479",//设备编号【必选】
		 *      "openMethod": 1, // 开门方式【必选】
		 *      "letPass": 1, //是否放行 0-未放行;1-放行;2-未知
		 *      "eventTime": 1555316902 // 事件UTC时间【必选】
		 *      "snapPhoto": "5A8EAB9B60C24B69A5FFB0AC45872479", // 抓拍图片ID【可选】当非人脸开门时没有抓拍图片
		 * }
		 */
		JSONObject object = JSONUtil.parseObj(bridgeListenerDTO.getContent());

		SaveSnapPersonDTO saveSnapPersonDTO = new SaveSnapPersonDTO();
		saveSnapPersonDTO.setDeviceId(object.getStr("deviceCode"));
		saveSnapPersonDTO.setCardNo(object.getStr("cardNo"));
		saveSnapPersonDTO.setEventType(object.getInt("openMethod"));
		saveSnapPersonDTO.setLetPass(object.getInt("letPass"));
		saveSnapPersonDTO.setFaceTemperature(object.getDouble("faceTemperature"));
		Calendar c = Calendar.getInstance();
		long millions=new Long(object.getInt("eventTime")).longValue()*1000;
		c.setTimeInMillis(millions);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = sdf.format(c.getTime());
		saveSnapPersonDTO.setSnapTime(dateString);
		saveSnapPersonDTO.setSnapPhotoId(object.getStr("snapPhoto"));
		if (object.getBool("isISC", false)) {
			// ISC门禁抓拍时，若是员工，那么cardNo为工号，若是非员工，则为主键ID
			String cardNo = object.getStr("cardNo");
			String iscPersonId = object.getStr("personId");
			boolean virtualIscCardNo = isVirtualIscCardNo(cardNo);
			log.info("ISC人员抓拍>>>>>>>工号为：{}", cardNo);
			if (virtualIscCardNo) {
				saveSnapPersonDTO.setCardNo(null);
			}
			if (StrUtil.isNotBlank(cardNo) && !virtualIscCardNo) {
				SmtStaff smtStaff = smtStaffService.getOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, cardNo), false);
				if (smtStaff != null){
					saveSnapPersonDTO.setCardNo(smtStaff.getId().toString());
				}
			}
			if (StrUtil.isBlank(saveSnapPersonDTO.getCardNo())) {
				String localCardNo = resolveIscTemporaryLocalCardNo(iscPersonId, object.getStr("deviceCode"));
				if (StrUtil.isNotBlank(localCardNo)) {
					saveSnapPersonDTO.setCardNo(localCardNo);
				}
			}
		}

		return smtSnapPersonService.addSnapPerson(saveSnapPersonDTO);
	}

	private String resolveIscTemporaryLocalCardNo(String iscPersonId, String deviceCode) {
		if (StrUtil.hasBlank(iscPersonId, deviceCode)) {
			return null;
		}
		SmtIscDownRecord downRecord = smtIscDownRecordService.getOne(Wrappers.<SmtIscDownRecord>lambdaQuery()
				.eq(SmtIscDownRecord::getPersonId, iscPersonId)
				.eq(SmtIscDownRecord::getDeviceCode, deviceCode)
				.and(wrapper -> wrapper
						.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
						.in(SmtIscDownRecord::getServiceType, Arrays.asList(
								DeviceTaskConstants.CARD_VISITOR,
								DeviceTaskConstants.CARD_ADMITTANCE))
						.or()
						.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CAR)
						.in(SmtIscDownRecord::getServiceType, Arrays.asList(
								DeviceTaskConstants.CAR_VISITOR,
								DeviceTaskConstants.CAT_ADMITTANCE)))
				.last("AND ROWNUM = 1"));
		return downRecord == null ? null : downRecord.getCardNo();
	}

	private boolean isVirtualIscCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo) && cardNo.startsWith("999");
	}

	/**
	 * 处理卡片回复结果
	 * @param replyContent
	 * @return
	 */
	@Transactional
	public Result<Boolean> doReplyOfCard(String replyContent){

		/**
		 * {
		 *     "code": 200, //结果状态码，200为成功，否则失败【必选】
		 *     "message": "操作成功",//结果信息【必选】
		 *     "data":{
		 *          "serialNo":"5A8EAB9B60C24B69A5FFB0AC45872479", // 消息流水号【必选】
		 *      }
		 * }
		 */

		/**
		 * {"code":402,"data":{"serialNo":"bb923e75adc94930bbb5b8e23c8645c8"},"message":"参数错误"}
		 */

		//{"code":200,"data":{"serialNo":"03005d6614f449d8a05a5a9f69b9158a"},"message":"操作成功"}

		JSONObject jsonObject = JSONUtil.parseObj(replyContent);
		if(null != jsonObject && jsonObject.containsKey("code")){
			if(jsonObject.containsKey("data")){
				JSONObject dataObj = jsonObject.getJSONObject("data");
				if(dataObj.containsKey("serialNo")){
					String serialNo = dataObj.getStr("serialNo");
					//修改处理状态
					SmtDeviceTask deviceTask = smtDeviceTaskService.getOne(new LambdaQueryWrapper<SmtDeviceTask>().eq(SmtDeviceTask::getSerialNo, serialNo));
					DeviceTaskStatusEnum status = DeviceTaskStatusEnum.FAIL;
					if(jsonObject.getInt("code").equals(DeviceTaskEnum.DEVICE_OK.getCode())){
						status = DeviceTaskStatusEnum.SUCCESS;
					}
					String msg = jsonObject.containsKey("message") ? jsonObject.getStr("message") : "";
					if(null != deviceTask){
						if(!(jsonObject.getInt("code").equals(DeviceTaskEnum.DEVICE_OPRATION_NEED_RETRY.getCode())
								|| jsonObject.getInt("code").equals(DeviceTaskEnum.DEVICE_DEVICE_ERROR.getCode())
						|| jsonObject.getInt("code").equals(DeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE.getCode()))){
							//设备错误、需要重试、设备不在线这几个状态 不修改任务状态  等待下次重试
							deviceTask.setStatus(status.getCode());
						}
						deviceTask.setUpdateTime(LocalDateTime.now());
						deviceTask.setRemark(msg);
						smtDeviceTaskService.updateById(deviceTask);
						if(status == DeviceTaskStatusEnum.SUCCESS){
							//如果回调成功 处理下发记录
							smtTaskDownRecordService.handleTaskDownRecord(deviceTask);
							//如果是访客人脸或者访客车辆 应在收到下发成功回调后生成卡片删除任务 删除时间为预约结束时间
							//如果访客离开时 应把删除时间修改为离开时的时间
							handelVisitor(deviceTask);
						}
					}
					return success();
				}
			}
		}
		return fail("处理卡片回复结果失败");
	}

	/**
	 * 处理访客预约成功回调
	 * @param deviceTask
	 */
	@Transactional
	public void handelVisitor(SmtDeviceTask deviceTask){
		if((
					(deviceTask.getDeviceType().equals(DeviceTypeEnum.DEVICE_TYPE_1.getCode()) && isVisitorCardAccess(deviceTask.getServiceType())) ||
								(isVehicleAccessDevice(deviceTask.getDeviceType()) && isVisitorVehicleAccess(deviceTask.getServiceType()))
				)
						&& deviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
			){
			//查询预约记录
			SmtVisitor smtVisitor = smtVisitorService.getById(deviceTask.getCardNo());
			if(null != smtVisitor){
				genDeviceTask(deviceTask,smtVisitor.getEndTime());
			} else {
				//可能是随行人员的人脸下发通知
				SmtFellowVisitor fellowVisitor = smtFellowVisitorService.getById(deviceTask.getCardNo());
					if(null != fellowVisitor){
						smtVisitor = smtVisitorService.getById(fellowVisitor.getVisitorId());
						genDeviceTask(deviceTask,smtVisitor.getEndTime());
					} else if (isVehicleAccessDevice(deviceTask.getDeviceType()) && isVisitorVehicleAccess(deviceTask.getServiceType())) {
						Long vehicleId = parseLong(deviceTask.getCardNo());
						if (vehicleId == null) {
							return;
						}
						SmtAdmittanceVehicle vehicle = smtAdmittanceVehicleService.getById(vehicleId);
						if (vehicle == null) {
							return;
						}
						SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(vehicle.getVisitorId());
						if (apply == null || apply.getEndTime() == null) {
							return;
						}
						genDeviceTask(deviceTask,Date.from(apply.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
					}else {
						//入厂申请人脸下发通知
						Long fellowId = parseLong(deviceTask.getCardNo());
						if (fellowId == null) {
							return;
						}
						SmtAdmittanceFellow fellow = smtAdmittanceFellowService.getById(fellowId);
						if (fellow == null) {
							return;
						}
						SmtAdmittanceApply apply = smtAdmittanceApplyService.getById(fellow.getVisitorId());
						if (apply == null || apply.getEndTime() == null) {
							return;
						}
						genDeviceTask(deviceTask,Date.from(apply.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
					}
				}
		}
	}

	private boolean isVisitorCardAccess(Integer serviceType) {
		return DeviceTaskConstants.CARD_VISITOR.equals(serviceType)
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(serviceType);
	}

	private boolean isVisitorVehicleAccess(Integer serviceType) {
		return DeviceTaskConstants.CAR_VISITOR.equals(serviceType)
				|| DeviceTaskConstants.CAT_ADMITTANCE.equals(serviceType);
	}

	private boolean isVehicleAccessDevice(Integer deviceType) {
		return DeviceTaskConstants.CAR.equals(deviceType)
				|| DeviceTypeEnum.DEVICE_TYPE_3.getCode().equals(deviceType);
	}

	private Long parseLong(String value) {
		if (StrUtil.isBlank(value)) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 生成访客删除任务 包括随行人员
	 * @param deviceTask
	 * @param endTime
	 */
	@Transactional
	public void genDeviceTask(SmtDeviceTask deviceTask, Date endTime){
		//查询是否已生成删除任务
		List<SmtDeviceTask> reusableDeleteTasks = filterReusableDeleteTasks(smtDeviceTaskService.list(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getDeviceCode, deviceTask.getDeviceCode())
					.eq(SmtDeviceTask::getCardNo, deviceTask.getCardNo())
					.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
					.eq(SmtDeviceTask::getDeviceType, deviceTask.getDeviceType())
					.eq(SmtDeviceTask::getServiceType, deviceTask.getServiceType())
			));
		if(!reusableDeleteTasks.isEmpty()){
			Long deleteOverTime = resolveDeleteOverTime(deviceTask, endTime);
			reusableDeleteTasks.forEach(task -> {
				task.setOverTime(deleteOverTime);
				if (!DeviceTaskStatusEnum.DOING.getCode().equals(task.getStatus())) {
					task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
					task.setRemark(null);
					task.setCode(null);
				}
				task.setUpdateTime(LocalDateTime.now());
				smtDeviceTaskService.updateById(task);
			});
			return;
		}
		SmtDeviceTask newDeviceTask = new SmtDeviceTask();
		BeanUtils.copyProperties(deviceTask,newDeviceTask);
		String sNo = UUID.randomUUID().toString().replaceAll("-", "");
		newDeviceTask.setId(null);
		newDeviceTask.setCreateTime(LocalDateTime.now());
		newDeviceTask.setOverTime(resolveDeleteOverTime(deviceTask, endTime));
		newDeviceTask.setSerialNo(sNo);
		newDeviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		newDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		newDeviceTask.setUpdateTime(null);
		newDeviceTask.setRemark(null);
		newDeviceTask.setCode(null);
		smtDeviceTaskService.save(newDeviceTask);
	}

	private List<SmtDeviceTask> filterReusableDeleteTasks(List<SmtDeviceTask> deleteTasks) {
		List<SmtDeviceTask> reusableDeleteTasks = new ArrayList<>();
		if (deleteTasks == null) {
			return reusableDeleteTasks;
		}
		for (SmtDeviceTask task : deleteTasks) {
			if (isReusableDeleteTask(task)) {
				reusableDeleteTasks.add(task);
			}
		}
		return reusableDeleteTasks;
	}

	private boolean isReusableDeleteTask(SmtDeviceTask task) {
		Integer status = task.getStatus();
		return status == null
				|| Objects.equals(status, DeviceTaskStatusEnum.INIT.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DOING.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.FAIL.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private Long resolveDeleteOverTime(SmtDeviceTask deviceTask, Date endTime) {
		if (DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(deviceTask.getDeviceType())
					&& isVisitorCardAccess(deviceTask.getServiceType())) {
				return endTime.getTime() / 1000;
			}
		if (isVehicleAccessDevice(deviceTask.getDeviceType())
				&& isVisitorVehicleAccess(deviceTask.getServiceType())) {
			return endTime.getTime() / 1000;
		}
		Date dateEndTime = ToolUtils.getDateEndTime(endTime);
		return dateEndTime.getTime() / 1000;
	}

}
