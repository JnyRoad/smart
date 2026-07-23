package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.IscTemperatureDTO;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleIscPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleStaffIdentityRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import com.tce.smart.platform.api.feign.RemoteSnapPersonService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.enums.*;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.core.mapper.SmtFellowVisitorMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.schedule.dto.DownDetailDTO;
import com.tce.smart.schedule.service.platform.ISCDeviceTaskService;
import com.tce.smart.schedule.support.IscLogPayloadFormatter;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.RedisKeyConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.ToolUtils;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author sunfujian
 * @date 2021/8/25 9:44
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class ISCDeviceTaskServiceImpl implements ISCDeviceTaskService {

	private final RemoteStaffService remoteStaffService;

	private final RemoteDispatcherService remoteDispatcherService;

	private final SmtImageService smtImageService;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	private final SmtAdmittanceApplyMapper smtAdmittanceApplyMapper;

	private final SmtAdmittanceFellowMapper smtAdmittanceFellowService;

	private final SmtDeviceService smtDeviceService;

	private final SmtIscDownRecordService smtIscDownRecordService;

	private final RemoteParkService remoteParkService;

	private final SmtVisitorService smtVisitorService;

	private final SmtFellowVisitorMapper smtFellowVisitorMapper;

	private final SmtAdmittanceFellowMapper smtAdmittanceFellowMapper;

	private final RemoteSnapPersonService remoteSnapPersonService;

	private final SmtMsgTempService smtMsgTempService;

	private final SmtStaffOtherService smtStaffOtherService;

	private final StringRedisTemplate redisTemplate;

	private static final int DEFAULT_CHANNEL_NO = 1;

	private static final int DOWNLOAD_DETAIL_PAGE_SIZE = 1000;

	private static final int AUTH_CONFIG_PERSON_BATCH_SIZE = 1000;

	private static final int ISC_LOG_TASK_ID_LIMIT = 100;

	private static final int ISC_TASK_TIMEOUT_HOURS = 6;

	private static final int ORPHAN_DOING_REQUEUE_MINUTES = 30;

	private static final int DOWNLOAD_DETAIL_MAX_PAGES = 50;

	private static final int DELETE_TASK_RETRY_DELAY_SECONDS = 60 * 60;

	private static final int AUTH_ITEM_QUERY_PAGE_NO = 1;

	private static final int AUTH_ITEM_QUERY_PAGE_SIZE = 1;

	private static final String DELETE_NO_AVAILABLE_DOWNLOAD_DATA_REMARK = "删除权限已无可用下发数据，已按删除成功处理";

	private static final String DELETE_PERSON_GONE_REMARK = "ISC人员已删除，权限已由ISC级联清理，按删除成功处理";

	private static final String AUTH_CONFIG_MAX_RETRY_REMARK = "权限下发失败已达到最大重试次数"
			+ DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES + "次，停止自动重试，请人工介入处理";

	private static final String ISC_VEHICLE_AUTH_UNSUPPORTED_REMARK = "ISC车辆权限不支持下发，已取消";

	/**
	 * 智能锁修改密码限制key
	 */
	private final String SMAT_DEVICE_OFFLINE_MSG = "smart_app:device:offline:msg:";

	/**
	 * 许昌园区外包工组织编号
	 */
	@Value("${smart.org.xc-hpo-index-code}")
	private String xcHpoOrgIndexCode;

	/**
	 * 许昌园区访客组织编号
	 */
	@Value("${smart.org.xc-visitor-index-code}")
	private String xcVisitorOrgIndexCode;

	/**
	 * 合肥园区临时人员组织编号
	 */
	@Value("${smart.org.hf-index-code}")
	private String hfOrgIndexCode;

	@Value("${smart.device-mode.access}")
	private String accessDeviceMode;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Value("${smart.hf-park-id:0}")
	private Integer hfParkId;

	/**
	 * ISC任务终态落库后触发入厂申请聚合回写（状态诚实化最终落点）。
	 * 只对来源于入厂申请（{@code task.getApplyId() != null}）的任务生效；
	 * 聚合/回写内部已自行处理重试与异常兜底（见{@link AdmittanceDispatchAggregator}），
	 * 这里再包一层try/catch纯粹是防御性的：绝不能因为聚合失败影响ISC任务主流程。
	 * 同一批次内多个任务同时落终态会重复触发聚合，聚合本身按当前DB状态重新计算，天然幂等。
	 */
	private void triggerDispatchAggregationIfApplicable(SmtIscDeviceTask task) {
		if (task == null || task.getApplyId() == null) {
			return;
		}
		try {
			new AdmittanceDispatchAggregator(smtIscDeviceTaskService, smtAdmittanceApplyMapper).aggregate(task.getApplyId());
		} catch (Exception e) {
			log.error("ISC任务终态聚合回写异常，applyId={}, taskId={}", task.getApplyId(), task.getId(), e);
		}
	}

	private boolean isTemporaryAccessTask(SmtIscDeviceTask task) {
		return task != null
				&& DeviceTaskConstants.CARD.equals(task.getDeviceType())
				&& isTemporaryAccessServiceType(task.getServiceType());
	}

	private boolean isTemporaryAccessServiceType(Integer serviceType) {
		return DeviceTaskConstants.CARD_VISITOR.equals(serviceType)
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(serviceType);
	}

	private boolean isUpdateFaceTask(SmtIscDeviceTask task) {
		return task != null && DeviceTaskConstants.UPDATE_FACE.equals(task.getServiceType());
	}

	private boolean shouldUpdateExistingFace(SmtIscDeviceTask task) {
		return isUpdateFaceTask(task) || isTemporaryAuthorizationTask(task);
	}

	private boolean isVirtualIscCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo) && cardNo.startsWith("999");
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

	private String taskPersonMapKey(SmtIscDeviceTask task) {
		if (task != null && task.getId() != null) {
			return "task:" + task.getId();
		}
		return "card:" + (task == null ? "" : StrUtil.nullToEmpty(task.getCardNo()));
	}

	private String personDetailMapKey(SmtIscDeviceTask task) {
		if (isTemporaryAuthorizationTask(task)) {
			return taskPersonMapKey(task);
		}
		return staffPersonDetailMapKey(task.getParkId(), task.getCardNo());
	}

	private String staffPersonDetailMapKey(Integer parkId, String cardNo) {
		return "park:" + (parkId == null ? "" : parkId) + ":card:" + StrUtil.nullToEmpty(cardNo);
	}

	private String faceProcessMapKey(SmtIscDeviceTask task, String personId) {
		String key = StrUtil.nullToEmpty(personId)
				+ "|park:" + (task == null || task.getParkId() == null ? "" : task.getParkId());
		if (shouldUpdateExistingFace(task)) {
			return key + "|image:" + StrUtil.nullToEmpty(task.getImageId());
		}
		return key;
	}

	private String resolveTemporaryAccessCertNo(SmtIscDeviceTask task) {
		if (task == null) {
			return null;
		}
		if (StrUtil.isNotBlank(task.getBadge())) {
			return task.getBadge();
		}
		if (StrUtil.isBlank(task.getCardNo()) || isVirtualIscCardNo(task.getCardNo())) {
			return null;
		}
		Long localPersonId = parseLong(task.getCardNo());
		return localPersonId == null ? null : getCertNo(localPersonId);
	}

	private void wrapNotStaffParam(SmtIscDeviceTask task, Map<String, Object> params) {
		String cardNo = task.getCardNo();
		if (StrUtil.isNotBlank(cardNo) && !isVirtualIscCardNo(cardNo)) {
			params.put("personId", cardNo);
			params.put("jobNo", cardNo);
		}
		// 性别，1：男；2：女；0：未知
		params.put("gender", 0);
		// 固定组织编号，所有人员都添加至统一组织
		params.put("orgIndexCode", Objects.equals(xcParkId, task.getParkId()) ? xcVisitorOrgIndexCode : hfOrgIndexCode);
		// 固定为身份证类型
		params.put("certificateType", "111");
		if (StrUtil.isBlank(cardNo) || isVirtualIscCardNo(cardNo)) {
			wrapTemporaryAccessParam(task, params);
			return;
		}
		//查询预约记录
		SmtVisitor visitor = smtVisitorService.getById(cardNo);
		if (null != visitor) {
			params.put("personName", visitor.getVisitorName());
			params.put("phoneNo", visitor.getVisitorPhone());
			// 固定为身份证类型
			params.put("certificateType", "111");
			params.put("certificateNo", visitor.getCertNo());
			return;
		}
		// 可能是随行人员的人脸下发通知
		SmtFellowVisitor fellowVisitor = smtFellowVisitorMapper.selectById(cardNo);
		if (null != fellowVisitor) {
			params.put("personName", fellowVisitor.getFellowName());
			SmtVisitor toVisitor = smtVisitorService.getById(fellowVisitor.getVisitorId());
			// 随行人员取访客人员的手机号
			params.put("phoneNo", toVisitor == null ? "" : toVisitor.getVisitorPhone());
			params.put("certificateNo", fellowVisitor.getCertNo());
			return;
		}
		// 入厂申请人脸下发通知
		Long admittanceFellowId = parseLong(cardNo);
		if (admittanceFellowId == null) {
			return;
		}
		SmtAdmittanceFellow fellow = smtAdmittanceFellowMapper.selectById(admittanceFellowId);
		if (fellow != null) {
			params.put("personName", fellow.getFellowName());
			SmtAdmittanceApply applyId = smtAdmittanceApplyMapper.selectById(fellow.getVisitorId());
			params.put("phoneNo", applyId == null ? "" : applyId.getVisitorPhone());
			params.put("certificateNo", fellow.getCertNo());
		}
	}

	private void wrapTemporaryAccessParam(SmtIscDeviceTask task, Map<String, Object> params) {
		if (StrUtil.isNotBlank(task.getGeneral())) {
			params.put("personName", task.getGeneral());
		}
		if (StrUtil.isNotBlank(task.getBadge())) {
			params.put("certificateNo", task.getBadge());
		}
		params.put("phoneNo", "");
	}

	/**
	 * 添加或更新人员至ISC
	 * @param task 任务
	 * @param isAdd 是否添加
	 * @return 返回人员在ISC平台的personId，如果操作失败返回null
	 *
	 * 修复说明：
	 * 1. 在添加人员前先检查人员是否已存在，避免重复添加导致"PersonId Already In Db"错误
	 * 2. 优化错误处理逻辑，当出现人员或人脸已存在错误时，尝试查询现有人员信息并返回personId
	 */
	private String addOrUpdatePerson(SmtIscDeviceTask task, boolean isAdd) {
		try {
			// 在添加人员前先检查人员是否已存在
			if (isAdd) {
				JSONObject person = this.getPersonDetail(task);
				log.info("event=isc_person_lookup task_id={} park_id={} staff_id={} staff_no={} person_payload={}",
						task.getId(), task.getParkId(), task.getCardNo(), logTaskBadge(task), IscLogPayloadFormatter.format(person));
				if (person != null) {
					// 人员已存在，返回现有的personId，不需要重复添加
					String existingPersonId = person.getStr("personId");
					if (StrUtil.isNotBlank(existingPersonId)) {
						log.info("人员[{}]已存在于ISC平台，personId：{}，继续权限配置流程", task.getCardNo(), existingPersonId);

						// 检查是否需要更新人脸信息
						JSONArray photoArr = person.getJSONArray("personPhoto");
						if (photoArr == null || photoArr.size() == 0) {
							// 人员存在但没有人脸，尝试添加人脸
							log.info("人员[{}]存在但无人脸信息，尝试添加人脸", task.getCardNo());
							boolean faceAdded = addFace(task, existingPersonId);
							if (!faceAdded) {
								log.warn("为已存在人员[{}]添加人脸失败，但继续权限配置", task.getCardNo());
								recordIscFaceRetry(task);
							}
						} else if (shouldUpdateExistingFace(task)) {
							String faceId = photoArr.getJSONObject(0).getStr("personPhotoIndexCode");
							boolean faceUpdated = modifyFace(task, faceId);
							if (!faceUpdated) {
								log.warn("为已存在人员[{}]更新人脸失败，但继续权限配置", task.getCardNo());
								recordIscFaceRetry(task);
							}
						} else {
							log.info("人员[{}]及人脸已存在，直接进行权限配置", task.getCardNo());
						}

						return existingPersonId;
					}
				}
			}

			Map<String, Object> params = new HashMap<>(10);
			String personId;

			Result<InternalScheduleIscPersonRespDTO> staffInfo = null;
			if (!isTemporaryAuthorizationTask(task)) {
				staffInfo = remoteStaffService.getScheduleIscPersonStaff(task.getCardNo(), SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			}
			log.info("event=isc_staff_lookup task_id={} park_id={} staff_lookup_success={}",
					task.getId(), task.getParkId(), staffInfo != null && staffInfo.isSuccess() && staffInfo.getData() != null);
			if (staffInfo != null && staffInfo.isSuccess() && Objects.nonNull(staffInfo.getData())) {
				// 获取员工信息
				InternalScheduleIscPersonRespDTO staff = staffInfo.getData();
				// 将员工编号放入params中
				params.put("personId", staff.getBadge());
				// 将员工姓名放入params中
				params.put("personName", staff.getName());
				// 性别，1：男；2：女；0：未知
				params.put("gender", staff.getSex() == null ? 0 : staff.getSex().equals(0) ? 1 : 2);
				// 固定组织编号，所有人员都添加至统一组织
				params.put("orgIndexCode", Objects.equals(xcParkId, task.getParkId()) ? xcHpoOrgIndexCode : hfOrgIndexCode);
				// 将员工生日放入params中
				params.put("birthday", staff.getBirth());
				// 固定为身份证类型
				params.put("certificateType", "111");
				// 将员工身份证号放入params中
				params.put("certificateNo", staff.getCertno());
				// 将员工编号放入params中
				params.put("jobNo", staff.getBadge());
				// 将员工编号赋值给personId
				personId = staff.getBadge();
			} else {
				// 将task的cardNo参数封装到params中，并传入task的parkId参数
				wrapNotStaffParam(task, params);
				// 将task的cardNo参数赋值给personId
				personId = StrUtil.isNotBlank(task.getCardNo()) && !isVirtualIscCardNo(task.getCardNo()) ? task.getCardNo() : null;
			}
			log.info("人员新增，园区：{}", task.getParkId());
			if (isAdd) {
				// 新增人员同时添加人脸图片数据
				SmtImage faceImg = smtImageService.getByCode(task.getImageId());
				if (faceImg != null && faceImg.getImage() != null) {
					Map<String, Object> faceMap = new HashMap<>(2);
					faceMap.put("faceData", Base64.encode(faceImg.getImage()));
					params.put("faces", new Object[]{faceMap});
				} else {
					log.warn("人员人脸图片不存在或无效，imageId：{}", task.getImageId());
				}
			}
			if(Objects.isNull(params.get("personName"))) {
				log.error("添加人员至ISC平台异常，cardNo信息未找到");
				return null;
			}

			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(task.getParkId());
			dispatcherDTO.setEventType(isAdd ? EventEnum.ISC_PERSON_ADD.getCode() : EventEnum.ISC_PERSON_UPDATE.getCode());
			dispatcherDTO.setData(params);
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				String errorMsg = result.getMessage();
				log.info("添加人员至ISC平台失败：{}", errorMsg);

				// 优化错误处理：对于已存在的情况，尝试获取现有人员信息继续流程
				if (errorMsg.contains("PersonFace Exists")) {
					log.info("人脸已存在，尝试获取现有人员信息继续权限配置");
					// 重新查询人员信息，如果能查到则返回personId继续权限配置流程
					JSONObject existingPerson = this.getPersonDetail(task);
					if (existingPerson != null) {
						String existingPersonId = existingPerson.getStr("personId");
						if (StrUtil.isNotBlank(existingPersonId)) {
							log.info("获取到已存在人员信息，继续权限配置：personId={}", existingPersonId);
							return existingPersonId;
						}
					}
				} else if (errorMsg.contains("PersonId Already In Db")) {
					log.info("人员已存在，尝试获取现有人员信息继续权限配置：{}", personId);
					// 重新查询人员信息
					JSONObject existingPerson = this.getPersonDetail(task);
					if (existingPerson != null) {
						String existingPersonId = existingPerson.getStr("personId");
						if (StrUtil.isNotBlank(existingPersonId)) {
							log.info("获取到已存在人员信息，继续权限配置：personId={}", existingPersonId);
							return existingPersonId;
						}
					}
				}

				return null;
			}
			String addedPersonId = resolveAddedPersonId(result.getData(), task, personId);
			if (StrUtil.isBlank(addedPersonId)) {
				log.warn("添加人员至ISC平台成功但未解析到personId，taskId={}, cardNo={}, badge={}",
						task.getId(), task.getCardNo(), task.getBadge());
				return null;
			}
			log.info("成功添加人员至ISC平台：personId={}", addedPersonId);
			return addedPersonId;
		} catch (Exception e) {
			log.error("添加人员至ISC平台异常：{}", task.getCardNo(), e);
			return null;
		}
	}

	private String resolveAddedPersonId(String resultData, SmtIscDeviceTask task, String fallbackPersonId) {
		if (StrUtil.isNotBlank(resultData)) {
			try {
				String personId = JSONUtil.parseObj(resultData).getStr("personId");
				if (StrUtil.isNotBlank(personId)) {
					return personId;
				}
			} catch (Exception e) {
				log.warn("解析ISC新增人员返回personId失败，taskId={}, data={}", task.getId(), resultData);
			}
		}
		String existingPersonId = resolveCurrentIscPersonId(task);
		if (StrUtil.isNotBlank(existingPersonId)) {
			return existingPersonId;
		}
		if (canUseLocalPersonIdFallback(task, fallbackPersonId)) {
			return fallbackPersonId;
		}
		return null;
	}

	private boolean canUseLocalPersonIdFallback(SmtIscDeviceTask task, String fallbackPersonId) {
		return StrUtil.isNotBlank(fallbackPersonId) && isTemporaryAuthorizationTask(task);
	}

	private String resolveCurrentIscPersonId(SmtIscDeviceTask task) {
		JSONObject existingPerson = this.getPersonDetail(task);
		return existingPerson == null ? null : existingPerson.getStr("personId");
	}

	private JSONObject getPersonDetail(SmtIscDeviceTask task) {
		try {
			String taskBadge = null;
			String taskCertNo = null;

			if (isTemporaryAuthorizationTask(task)) {
				taskCertNo = resolveTemporaryAccessCertNo(task);
			} else {
				Result<InternalScheduleStaffIdentityRespDTO> staffInfo = remoteStaffService.getScheduleIdentityStaff(task.getCardNo(),
						SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (staffInfo != null && staffInfo.isSuccess() && Objects.nonNull(staffInfo.getData())) {
					taskBadge = staffInfo.getData().getBadge();
					taskCertNo = staffInfo.getData().getCertno();
				}
			}

			if (StrUtil.isBlank(taskBadge) && StrUtil.isEmpty(taskCertNo)) {
				log.error("员工信息查询失败：{}", task.getCardNo());
				return null;
			}

			Map<String, Object> params = new HashMap<>(2);
			/**
			 * 参数名
			 * certificateNo：证件号码，从获取人员列表v2接口返回报文中的certificateNo字段
			 * personId：人员Id，从获取人员列表v2接口返回报文中的personId字段；
			 * phoneNo：手机号码，从获取人员列表v2接口返回报文中的phoneNo字段；
			 * jobNo：工号，从获取人员列表v2接口返回报文中的jobNo字段。
			 */
			if (StrUtil.isNotBlank(taskBadge)) {
				params.put("paramName", "jobNo");
				// 员工传工号，非员工传主键ID
				params.put("paramValue", new String[]{taskBadge});
			} else if (StrUtil.isNotBlank(taskCertNo)) {
				params.put("paramName", "certificateNo");
				// 员工传工号，非员工传主键ID
				params.put("paramValue", new String[]{taskCertNo});
			}
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(task.getParkId());
			dispatcherDTO.setEventType(EventEnum.ISC_PERSON_GET.getCode());
			dispatcherDTO.setData(params);
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("从ISC平台查询人员[{}]失败：{}", task.getCardNo(), result.getMessage());
				return null;
			}
			JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
			if (list == null || list.isEmpty()) {
				return null;
			}
			for (int i = 0; i < list.size(); i++) {
				JSONObject personObj = list.getJSONObject(i);
				String personId = personObj.getStr("personId");
				if (StrUtil.isBlank(personId)) {
					continue;
				}
				// 状态小于0代表ISC侧已删除，不能继续用该人员做权限配置。
				Integer status = personObj.getInt("status");
				if (status == null || status >= 0) {
					return personObj;
				}
				log.warn("ISC人员[{}]已处于删除状态，personId={}", task.getCardNo(), personId);
			}
			return null;
		} catch (Exception e) {
			log.error("从ISC平台查询人员[{}]异常：{}", task.getCardNo(), e);
			return null;
		}
	}

	/**
	 * 员工任务人员信息批量查询
	 * @param smtStaffList
	 * @return
	 */
	private Map<String,JSONObject> getStaffPersonListDetail(List<SmtStaff> smtStaffList,Integer parkId) {
		try {
			List<String> taskBadges = smtStaffList.stream()
					.map(SmtStaff::getBadge)  // 过滤掉badge为null的元素
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			Map<String, Object> params = new HashMap<>(2);
			/**
			 * 参数名
			 * certificateNo：证件号码，从获取人员列表v2接口返回报文中的certificateNo字段
			 * personId：人员Id，从获取人员列表v2接口返回报文中的personId字段；
			 * phoneNo：手机号码，从获取人员列表v2接口返回报文中的phoneNo字段；
			 * jobNo：工号，从获取人员列表v2接口返回报文中的jobNo字段。
			 */
			params.put("paramName", "jobNo");
			// 员工传工号，非员工传主键ID
			params.put("paramValue", taskBadges.toArray());
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(parkId);
			dispatcherDTO.setEventType(EventEnum.ISC_PERSON_GET.getCode());
			dispatcherDTO.setData(params);
			log.debug("event=isc_person_batch_lookup_request park_id={} payload={}", parkId,
					IscLogPayloadFormatter.format(params));
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.debug("从ISC平台批量查询人员失败：{}", result.getMessage());
				return null;
			} else {
				log.debug("event=isc_person_batch_lookup_response park_id={} payload={}", parkId,
						IscLogPayloadFormatter.format(result.getData()));
			}
			Map<String,JSONObject> personMap = new HashMap<>();
			JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
			for(Object obj : list){
				// String personId = ((JSONObject)obj).getStr("personId");
				Integer status = ((JSONObject)obj).getInt("status");
				if (status != null && status < 0) {
					continue;
				}
					String jobNo = ((JSONObject)obj).getStr("jobNo");
					SmtStaff smtStaffDTO = smtStaffList.stream().filter(s -> s.getBadge().equals(jobNo)).findFirst().orElse(null);
					if(Objects.nonNull(smtStaffDTO)){
						personMap.put(staffPersonDetailMapKey(parkId, smtStaffDTO.getId().toString()),(JSONObject)obj);
					}
				}
			return personMap;
		} catch (Exception e) {
			// 查询失败返回null（与result失败路径同一哨兵），由调用方跳过本轮重试，避免误判"人员不存在"或中断其他园区
			log.error("从ISC平台批量查询人员异常：{}", e);
			return null;
		}
	}

	/**
	 * 访客任务人员信息批量查询
	 * @param taskList
	 * @return
	 */
	private Map<String,JSONObject> getVisitorPersonListDetail(List<SmtIscDeviceTask> taskList) {
		try {
			// 访客的任务 badge字段存的是访客的身份证
			// Map<String,List<SmtIscDeviceTask>> visiCertIdMap = taskList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getBadge));

			Map<String,List<SmtIscDeviceTask>> visiCertIdMap = new LinkedHashMap<>();
			for (SmtIscDeviceTask task : taskList) {
				String certNo = resolveTemporaryAccessCertNo(task);
				if (StrUtil.isBlank(certNo)) {
					continue;
				}
				visiCertIdMap.computeIfAbsent(certNo, key -> new ArrayList<>()).add(task);
			}
			if (visiCertIdMap.isEmpty()) {
				log.warn("访客/入厂权限任务缺少可用于ISC查询的证件号，任务数：{}", taskList.size());
				return Collections.emptyMap();
			}

			Map<String, Object> params = new HashMap<>(2);
			/**
			 * 参数名
			 * certificateNo：证件号码，从获取人员列表v2接口返回报文中的certificateNo字段
			 * personId：人员Id，从获取人员列表v2接口返回报文中的personId字段；
			 * phoneNo：手机号码，从获取人员列表v2接口返回报文中的phoneNo字段；
			 * jobNo：工号，从获取人员列表v2接口返回报文中的jobNo字段。
			 */
			params.put("paramName", "certificateNo");
			// 员工传工号，非员工传主键ID
			params.put("paramValue", visiCertIdMap.keySet());
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(taskList.get(0).getParkId());
			dispatcherDTO.setEventType(EventEnum.ISC_PERSON_GET.getCode());
			dispatcherDTO.setData(params);
			log.info("event=isc_visitor_batch_lookup_request park_id={} payload={}", dispatcherDTO.getParkId(),
					IscLogPayloadFormatter.format(params));
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				// 查询失败返回null哨兵，调用方跳过本轮避免误判"人员不存在"而重复建人
				log.info("从ISC平台批量查询人员失败：{}", result.getMessage());
				return null;
			}
			Map<String,JSONObject> personMap = new HashMap<>();
			JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
			if (list == null || list.isEmpty()) {
				return Collections.emptyMap();
			}
			for(Object obj : list){
				Integer status = ((JSONObject)obj).getInt("status");
				if (status != null && status < 0) {
					continue;
				}
				String certificateNo = ((JSONObject)obj).getStr("certificateNo");
				if(visiCertIdMap.containsKey(certificateNo)){
					for (SmtIscDeviceTask task : visiCertIdMap.get(certificateNo)) {
						personMap.put(taskPersonMapKey(task), (JSONObject) obj);
					}
				}
			}
			return personMap;
		} catch (Exception e) {
			log.error("从ISC平台批量查询人员异常：{}", e);
			return null;
		}
	}

	private String getCertNo(Long id) {
		SmtAdmittanceFellow fellow = smtAdmittanceFellowService.selectById(id);
		if (Objects.nonNull(fellow)) {
			return fellow.getCertNo();
		}
		SmtVisitor visitor = smtVisitorService.getById(id);
		if (Objects.nonNull(visitor)) {
			return visitor.getCertNo();
		}
		SmtFellowVisitor fellowVisitor = smtFellowVisitorMapper.selectById(id);
		if (Objects.nonNull(fellowVisitor)) {
			return fellowVisitor.getCertNo();
		}
		return null;
	}

	private Map<String,String> getCertNoList(List<Long> ids) {
		Map<String,String> certNoMap = new HashMap<>();

		List<SmtAdmittanceApply> smtAdmittanceApplies = smtAdmittanceApplyMapper.selectBatchIds(ids);
		if (CollectionUtil.isNotEmpty(smtAdmittanceApplies)) {
			for(SmtAdmittanceApply apply : smtAdmittanceApplies){
				certNoMap.put(apply.getCertNo(),apply.getId().toString());
			}
		}

		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.selectBatchIds(ids);
		if (CollectionUtil.isNotEmpty(fellows)) {
			for(SmtAdmittanceFellow fellow : fellows){
				certNoMap.put(fellow.getCertNo(),fellow.getId().toString());
			}
		}
		Collection<SmtVisitor> visitors = smtVisitorService.listByIds(ids);
		if (CollectionUtil.isNotEmpty(visitors)) {
			for(SmtVisitor visitor : visitors){
				certNoMap.put(visitor.getCertNo(),visitor.getId().toString());
			}
		}
		List<SmtFellowVisitor> fellowVisitors = smtFellowVisitorMapper.selectBatchIds(ids);
		if (CollectionUtil.isNotEmpty(fellowVisitors)) {
			for(SmtFellowVisitor visitor : fellowVisitors){
				certNoMap.put(visitor.getCertNo(),visitor.getId().toString());
			}
		}
		return certNoMap;
	}

	private JSONArray getPersonDetailById(Set<String> personIds, Integer parkId) {
		try {
			Map<String, Object> params = new HashMap<>(2);
			params.put("paramName", "personId");
			params.put("paramValue", personIds);
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(parkId);
			dispatcherDTO.setEventType(EventEnum.ISC_PERSON_GET.getCode());
			dispatcherDTO.setData(params);
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			log.info("event=isc_person_detail_response park_id={} person_count={} payload={}", parkId, personIds.size(),
					IscLogPayloadFormatter.format(result == null ? null : result.getData()));
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("从ISC平台查询人员[{}]失败：{}", personIds, result.getMessage());
				return null;
			}
			return JSONUtil.parseObj(result.getData()).getJSONArray("list");
		} catch (Exception e) {
			log.error("从ISC平台查询人员[{}]异常：{}", personIds, e);
			return null;
		}
	}

	private boolean handleTaskResultBatch(List<SmtIscDeviceTask> taskList, ISCDeviceTaskEnum taskEnum, String taskId) {
		boolean update = false;
		for (SmtIscDeviceTask task : taskList) {
			task.setCode(taskEnum.getCode());
			task.setRemark(taskEnum.getDesc());
			task.setIscTaskId(taskId);
			task.setUpdateTime(LocalDateTime.now());
			update = smtIscDeviceTaskService.updateById(task);
			if (!update) {
				// 任务行可能已被并发删除（如重新下发清理），记录后继续处理其余任务
				log.warn("ISC任务结果回写失败（任务可能已被删除），taskId={}", task.getId());
			}
		}
		return update;
	}

	private void handleTaskResult(SmtIscDeviceTask task, ISCDeviceTaskEnum taskEnum, Integer code, String msg) {
		if (taskEnum != null) {
			task.setCode(taskEnum.getCode());
			task.setRemark(taskEnum.getDesc());
			return;
		}
		task.setCode(code);
		task.setRemark(msg);
	}

	private void stopExceededRetryAuthTasks() {
		// 改用AndCollectApplyIds重载：该方法是纯批量UPDATE（WHERE按deviceType/action/status/times过滤，
		// 不含apply_id），会绕过11处单任务终态出口已接入的聚合钩子（ISCDeviceTaskServiceImpl其余出口触发
		// triggerDispatchAggregationIfApplicable），导致来源于入厂申请的任务被批量停止重试后申请单永久漏算聚合
		Set<Long> affectedApplyIds = smtIscDeviceTaskService.stopExceededRetryAuthTasksAndCollectApplyIds(
				DeviceTaskConstants.CARD, DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, AUTH_CONFIG_MAX_RETRY_REMARK);
		if (CollectionUtil.isNotEmpty(affectedApplyIds)) {
			log.info("本轮停止达到最大重试次数的ISC权限任务，最大重试次数：{}，涉及入厂申请单：{}个",
					DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, affectedApplyIds.size());
		}
		triggerDispatchAggregationForApplyIds(affectedApplyIds);
	}

	/**
	 * 批量终态路径补聚合钩子：对SELECT-then-UPDATE批量方法返回的申请单ID逐个触发聚合回写。
	 * 单个申请单聚合失败不影响其余申请单继续聚合，异常记ERROR后继续下一个。
	 * @param applyIds 受批量UPDATE影响、且来源于入厂申请的申请单ID集合（可能为空）
	 */
	private void triggerDispatchAggregationForApplyIds(Set<Long> applyIds) {
		if (CollectionUtil.isEmpty(applyIds)) {
			return;
		}
		AdmittanceDispatchAggregator aggregator =
				new AdmittanceDispatchAggregator(smtIscDeviceTaskService, smtAdmittanceApplyMapper);
		for (Long applyId : applyIds) {
			try {
				aggregator.aggregate(applyId);
			} catch (Exception e) {
				log.error("批量终态任务聚合回写异常，applyId={}", applyId, e);
			}
		}
	}

	/**
	 * ISC设备权限下发 - 优化版本
	 * 使用分离查询策略，提升性能和准确性
	 */
	@Override
	public void downAccess() {
		// 记录开始时间
		long begin = System.currentTimeMillis();
		// 记录成功数量
		int success = 0;
		cancelPendingIscVehicleTasks();

		// 创建任务列表
		List<SmtIscDeviceTask> taskList = new ArrayList<>();

		// 获取系统时区
		TimeZone timeZone = TimeZone.getDefault();

		// 获取当前服务器时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(timeZone);
		String currentTime = sdf.format(new Date());

		// 查询正常下发任务
		long currentSeconds = DateUtil.currentSeconds();
		stopExceededRetryAuthTasks();
		smtIscDeviceTaskService.requeueOrphanDoingTasks(DeviceTaskConstants.CARD, ORPHAN_DOING_REQUEUE_MINUTES);

		// 以下三个批量终态方法同样绕过单任务出口的聚合钩子，改用AndCollectApplyIds重载并逐单触发聚合
		Set<Long> expiredApplyIds = smtIscDeviceTaskService.expireOverdueDownloadTasksAndCollectApplyIds(DeviceTaskConstants.CARD);
		triggerDispatchAggregationForApplyIds(expiredApplyIds);

		// markOfflineDeviceTasks写DEVICE_OFFLINE非任务终态，但聚合把它计入失败判定（见AdmittanceDispatchAggregator
		// FAILURE_TERMINAL_STATUSES），离线也应及时反映到申请单，故一并补钩子
		Set<Long> offlineApplyIds = smtIscDeviceTaskService.markOfflineDeviceTasksAndCollectApplyIds(DeviceTaskConstants.CARD);
		triggerDispatchAggregationForApplyIds(offlineApplyIds);

		// 改用AndCollectApplyIds重载（单次SELECT-then-UPDATE即可拿到取消数量与申请单ID，
		// 避免重复调用cancelStaleOfflineDownloadTasks(int,int)导致同一批任务被处理两次）
		Set<Long> canceledStaleApplyIds = smtIscDeviceTaskService.cancelStaleOfflineDownloadTasksAndCollectApplyIds(
				DeviceTaskConstants.CARD, DeviceTaskConstants.OFFLINE_TASK_CANCEL_MONTHS);
		if (CollectionUtil.isNotEmpty(canceledStaleApplyIds)) {
			log.info("本轮自动取消长期离线ISC下发任务，涉及入厂申请单：{}个", canceledStaleApplyIds.size());
		}
		triggerDispatchAggregationForApplyIds(canceledStaleApplyIds);
		log.info("开始-查询ISC正常下发任务TaskList，时间戳：{} 时区：{} 当前服务器时间: {}", currentSeconds, timeZone, currentTime);
		List<SmtIscDeviceTask> normalTaskList = new ArrayList<>();
		normalTaskList.addAll(emptyTaskList(smtIscDeviceTaskService.getCardDown(newDeviceTaskPage(), currentSeconds,
				DeviceTaskConstants.CARD)));
		log.info("结束-查询ISC正常下发任务TaskList，任务数量：{}", normalTaskList.size());
		// 获取正常下发任务列表
		log.info("开始-获取ISC正常下发任务列表");
		log.info("结束-获取ISC正常下发任务列表，任务数量：{}", normalTaskList.size());

		// 处理正常任务中的过期情况
		List<SmtIscDeviceTask> validNormalTasks = filterAndHandleExpiredTasks(normalTaskList, currentSeconds);
		taskList.addAll(validNormalTasks);

		// 查询延迟下发任务
		currentSeconds = DateUtil.currentSeconds();
		log.info("开始-查询ISC延迟下发任务TaskList，时间戳：{}", currentSeconds);
		List<SmtIscDeviceTask> delayList = new ArrayList<>();
		IPage<SmtIscDeviceTask> delayTaskList = smtIscDeviceTaskService.getDelayDown(newDeviceTaskPage(), currentSeconds,
				DeviceTaskConstants.CARD);
		delayList.addAll(pageRecords(delayTaskList));
		log.info("结束-查询ISC延迟下发任务TaskList，任务数量：{}", delayList.size());
		// 获取延迟下发任务列表
		log.info("开始-获取ISC延迟下发任务列表");
		log.info("结束-获取ISC延迟下发任务列表，任务数量：{}", delayList.size());

		// 处理延迟任务中的过期情况
		List<SmtIscDeviceTask> validDelayTasks = filterAndHandleExpiredTasks(delayList, currentSeconds);
		taskList.addAll(validDelayTasks);

		// 记录日志，开始ISC人员权限下发任务，总数、正常任务、延迟任务
		log.info("开始-ISC人员权限下发任务，总数：{}, 有效正常任务: {}, 有效延迟任务: {}", taskList.size(),
				validNormalTasks.size(), validDelayTasks.size());
		// 调用addOrDelAuthConfig方法，下发ISC人员权限
		boolean update = addOrDelAuthConfig(taskList, true);
		// 如果下发成功，记录成功数量
		if (update) {
			success += taskList.size();
		}
		// 记录日志，完成ISC人员权限下发任务，耗时、成功、失败
		log.info("完成-ISC人员权限下发任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}

	/**
	 * 过滤和处理过期任务
	 * @param tasks 任务列表
	 * @param currentSeconds 当前时间戳
	 * @return 有效的任务列表
	 */
	private List<SmtIscDeviceTask> filterAndHandleExpiredTasks(List<SmtIscDeviceTask> tasks, long currentSeconds) {
		List<SmtIscDeviceTask> validTasks = new ArrayList<>();

		for (SmtIscDeviceTask task : tasks) {
			try {
				// 检查任务是否过期
				if (task.getOverTime() != null && task.getOverTime() <= currentSeconds) {
					// 任务已过期，需要特殊处理
					if (shouldProcessExpiredTask(task)) {
						// 某些过期任务仍需要处理（如员工离职删除）
						validTasks.add(task);
						log.info("过期任务仍需处理：任务ID={}, 类型={}, 服务类型={}",
								task.getId(), task.getAction(), task.getServiceType());
					} else {
						// 标记为过期状态
						handleExpiredTask(task);
						log.info("任务已过期并标记：任务ID={}, 过期时间={}",
								task.getId(), DateUtil.date(task.getOverTime() * 1000));
					}
				} else {
					// 任务未过期，正常处理
					validTasks.add(task);
				}
			} catch (Exception e) {
				log.error("处理任务过期检查异常，任务ID：{}", task.getId(), e);
				// 异常情况下仍然加入处理列表
				validTasks.add(task);
			}
		}

		return validTasks;
	}

	private List<SmtIscDeviceTask> emptyTaskList(List<SmtIscDeviceTask> tasks) {
		return tasks == null ? Collections.emptyList() : tasks;
	}

	private List<SmtIscDeviceTask> pageRecords(IPage<SmtIscDeviceTask> page) {
		return page == null || page.getRecords() == null ? Collections.emptyList() : page.getRecords();
	}

	/**
	 * 判断过期任务是否仍需要处理
	 * @param task 任务
	 * @return true表示需要处理，false表示可以跳过
	 */
	private boolean shouldProcessExpiredTask(SmtIscDeviceTask task) {
		// 1. 访客删除任务：按原有时间规则，不在这里处理
		if (isVisitorDeleteTask(task)) {
			return false;
		}

		// 2. 删除任务：员工离职等删除操作不受时间限制，必须执行
		if (DeviceTaskActionEnum.DEL.getCode().equals(task.getAction()) ||
			DeviceTaskActionEnum.DELAY_DEL.getCode().equals(task.getAction())) {
			log.info("删除任务不受过期时间限制，继续执行：任务ID={}", task.getId());
			return true;
		}

		// 3. 员工权限任务：检查员工状态
		if (!isTemporaryAuthorizationTask(task)) {
			try {
				SmtStaff staff = smtStaffOtherService.getOne(
					new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getId, task.getCardNo())
				);

				// 员工已离职：权限任务仍需处理（用于清理权限）
				if (staff != null && StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
					log.info("员工已离职，权限任务继续处理：员工ID={}, 任务ID={}", task.getCardNo(), task.getId());
					return true;
				}
			} catch (Exception e) {
				log.error("查询员工状态异常，任务ID：{}", task.getId(), e);
			}
		}

		// 4. 其他情况：过期任务不处理
		return false;
	}

	/**
	 * 处理单个过期任务
	 * @param task 过期任务
	 */
	private void handleExpiredTask(SmtIscDeviceTask task) {
		try {
			if (isTemporaryAuthorizationTask(task)) {
				// 临时权限过期：标记为过期状态
				task.setStatus(DeviceTaskStatusEnum.EXPIRED.getCode());
				task.setRemark("临时人员权限已过期，有效期至：" + DateUtil.date(task.getOverTime() * 1000));
			} else {
				// 员工权限过期：检查员工状态和任务类型
				SmtStaff staff = smtStaffOtherService.getOne(
					new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getId, task.getCardNo())
				);

				boolean isDeleteTask = DeviceTaskActionEnum.DEL.getCode().equals(task.getAction()) ||
									   DeviceTaskActionEnum.DELAY_DEL.getCode().equals(task.getAction());

				if (staff != null && StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
					// 员工已离职
					if (isDeleteTask) {
						// 删除任务：不标记为过期，让其继续执行（在shouldProcessExpiredTask中已处理）
						log.info("员工已离职，删除任务将继续执行：员工ID={}, 任务ID={}", task.getCardNo(), task.getId());
						return; // 不更新状态，让任务继续执行
					} else {
						// 下发任务：标记为取消状态
						task.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
						task.setRemark("员工已离职，权限下发任务自动取消");
					}
				} else {
					// 员工未离职但权限过期
					if (isDeleteTask) {
						// 删除任务过期：一般不应该发生，但标记为过期
						task.setStatus(DeviceTaskStatusEnum.EXPIRED.getCode());
						task.setRemark("删除任务已过期，有效期至：" + DateUtil.date(task.getOverTime() * 1000));
					} else {
						// 下发任务过期：标记为过期状态
						task.setStatus(DeviceTaskStatusEnum.EXPIRED.getCode());
						task.setRemark("员工权限已过期，有效期至：" + DateUtil.date(task.getOverTime() * 1000));
					}
				}
			}

			task.setUpdateTime(LocalDateTime.now());
			smtIscDeviceTaskService.updateById(task);
			triggerDispatchAggregationIfApplicable(task);

		} catch (Exception e) {
			log.error("处理过期任务异常，任务ID：{}", task.getId(), e);
		}
	}

	/**
	 * 判断是否为访客删除任务
	 * 访客删除任务需要按照原有的时间规则执行，不在这里处理
	 */
	private boolean isVisitorDeleteTask(SmtIscDeviceTask task) {
			return DeviceTaskActionEnum.DEL.getCode().equals(task.getAction())
					&& isTemporaryAuthorizationTask(task);
	}

	private Map<String,String> downAccessPre(List<SmtIscDeviceTask> tasks, Set<Long> skipTaskIds) {
		log.info("开始-ISC人员权限下发预处理任务，总数：{}", tasks.size());
		Map<String, JSONObject> personMap = new HashMap<>();
		Set<Integer> failedQueryParks = new HashSet<>();
		List<SmtStaff> smtStaffList = new ArrayList<>();

		List<SmtIscDeviceTask> temporaryAccessTasks = tasks.stream()
				.filter(this::isTemporaryAccessTask)
				.filter(task -> StrUtil.isNotBlank(resolveTemporaryAccessCertNo(task)))
				.collect(Collectors.toList());
		boolean visitorQueryFailed = false;
		if (CollectionUtil.isNotEmpty(temporaryAccessTasks)) {
			Map<String, JSONObject> tempMap = getVisitorPersonListDetail(temporaryAccessTasks);
			if (tempMap == null) {
				// 批量查询失败≠人员不存在：本轮跳过临时人员任务，保持INIT下轮重试
				visitorQueryFailed = true;
				log.warn("临时人员批量查询ISC失败，本轮跳过{}个临时任务，等待下轮重试", temporaryAccessTasks.size());
			} else if (CollectionUtil.isNotEmpty(tempMap)) {
				personMap.putAll(tempMap);
				log.info("查询到临时人员ISC人员信息{}条，人员{}", tempMap.size(), tempMap.keySet());
			}
		}

		List<SmtIscDeviceTask> staffTasks = tasks.stream()
				.filter(task -> !isTemporaryAccessTask(task))
				.collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(staffTasks)) {
			List<String> cardNoList = staffTasks.stream()
					.map(SmtIscDeviceTask::getCardNo)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			smtStaffList = smtStaffOtherService.list(new LambdaQueryWrapper<SmtStaff>()
					.in(SmtStaff::getId, cardNoList)
			);

			log.info("zhongsj-查询到员工信息{}条,人员id-{}", smtStaffList.size(), String.join(", ", cardNoList));
			if (CollectionUtil.isEmpty(smtStaffList)) {
				log.error("批量员工信息查询失败");
				return null;
			}

			Map<Long, SmtStaff> staffById = smtStaffList.stream()
					.collect(Collectors.toMap(SmtStaff::getId, staff -> staff, (first, second) -> first));
			Map<Integer, List<SmtIscDeviceTask>> staffTasksByPark = staffTasks.stream()
					.filter(task -> task.getParkId() != null)
					.collect(Collectors.groupingBy(SmtIscDeviceTask::getParkId));
			for (Map.Entry<Integer, List<SmtIscDeviceTask>> entry : staffTasksByPark.entrySet()) {
				List<SmtStaff> parkStaffList = entry.getValue().stream()
						.map(task -> parseLong(task.getCardNo()))
						.filter(Objects::nonNull)
						.map(staffById::get)
						.filter(Objects::nonNull)
						.distinct()
						.collect(Collectors.toList());
				Map<String, JSONObject> tempMap = getStaffPersonListDetail(parkStaffList, entry.getKey());
				if (tempMap == null) {
					// 批量查询失败≠人员不存在：本轮跳过该园区员工任务，保持INIT下轮重试
					failedQueryParks.add(entry.getKey());
					log.warn("园区[{}]批量查询ISC人员失败，本轮跳过该园区员工任务，等待下轮重试", entry.getKey());
					continue;
				}
				if (CollectionUtil.isNotEmpty(tempMap)) {
					personMap.putAll(tempMap);
					log.info("查询到园区[{}]员工ISC人员信息{}条", entry.getKey(), tempMap.size());
				}
			}
		}
		Map<String, String> personIdMap = new HashMap<>();
		// 添加已处理人脸的缓存，避免重复添加同一张脸
		Set<String> processedFaceKeys = new HashSet<>();

		List<SmtIscDeviceTask> orderedTasks = tasks.stream()
				.sorted(Comparator.comparing(SmtIscDeviceTask::getCreateTime,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		for (SmtIscDeviceTask task : orderedTasks) {
			if (!isTemporaryAccessTask(task) && task.getParkId() != null && failedQueryParks.contains(task.getParkId())) {
				skipTaskIds.add(task.getId());
				continue;
			}
			if (visitorQueryFailed && isTemporaryAccessTask(task)
					&& StrUtil.isNotBlank(resolveTemporaryAccessCertNo(task))) {
				skipTaskIds.add(task.getId());
				continue;
			}
			String taskPersonKey = taskPersonMapKey(task);
			String detailMapKey = personDetailMapKey(task);
			// 若ISC平台不存在此人员，则新增人员信息
			if (personMap.containsKey(detailMapKey)) {
				//查询到人员信息
				JSONObject personDetail = personMap.get(detailMapKey);
				String personId = personDetail.getStr("personId");
				// 添加或更新人脸图片
				JSONArray photoArr = personDetail.getJSONArray("personPhoto");
				String faceId = photoArr != null && photoArr.size() > 0 ? photoArr.getJSONObject(0).getStr("personPhotoIndexCode") : null;

				// 优化：只有当该人员还没有处理过人脸且确实需要更新人脸时才进行人脸操作
				boolean faceProcessSuccess = true;
				String faceProcessKey = faceProcessMapKey(task, personId);
				if (!processedFaceKeys.contains(faceProcessKey)) {
					// 此处调用modifyFace修改 是否可以判断图片有无变更
					// boolean update = StrUtil.isBlank(faceId) ? addFace(task, personId) : modifyFace(task, faceId);
					// 不再更新人脸图片
					// update = StrUtil.isBlank(faceId) ? addFace(task, personId) : Boolean.TRUE;
					processedFaceKeys.add(faceProcessKey); // 先标记为已处理，避免重复处理

					if (StrUtil.isBlank(faceId)) {
						// 人员存在但没有人脸，尝试添加人脸
						log.info("人员[{}]存在但无人脸信息，尝试添加人脸，personId：{}", task.getCardNo(), personId);
						faceProcessSuccess = addFace(task, personId);
						if (!faceProcessSuccess) {
							log.warn("为已存在人员[{}]添加人脸失败，但继续权限配置流程", task.getCardNo());
							recordIscFaceRetry(task);
							// 即使人脸添加失败，也继续权限配置流程，因为人员已存在
							faceProcessSuccess = true;
						}
					} else if (shouldUpdateExistingFace(task)) {
						log.info("人员[{}]人脸已存在，按设备园区更新ISC人脸，personId：{}", task.getCardNo(), personId);
						faceProcessSuccess = modifyFace(task, faceId);
						if (!faceProcessSuccess) {
							log.warn("为已存在人员[{}]更新人脸失败，但继续权限配置流程", task.getCardNo());
							recordIscFaceRetry(task);
							faceProcessSuccess = true;
						}
					} else {
						// 人员和人脸都存在，检查是否需要更新人脸
						log.info("人员[{}]及人脸已存在，personId：{}，跳过人脸处理", task.getCardNo(), personId);
						// 可以选择是否更新人脸，这里选择不更新以避免"PersonFace Exists"错误
						// boolean faceUpdateSuccess = modifyFace(task, faceId);
					}
					log.info("人员[{}]人脸处理完成，personId：{}，结果：{}", task.getCardNo(), personId, faceProcessSuccess);
				} else {
					log.info("人员[{}]人脸已处理过，跳过重复处理，personId：{}", task.getCardNo(), personId);
				}

				// 无论人脸处理是否成功，都将personId加入映射，确保权限配置能继续
				personIdMap.put(taskPersonKey, personId);
			} else {
				// 未查询到人员信息
				//许昌园区不需要添加正式员工到ISC 临时员工需要添加到ISC
				if (Objects.equals(xcParkId, task.getParkId()) && !isTemporaryAuthorizationTask(task)) {
					SmtStaff smtStaff = smtStaffList.stream().filter(s -> s.getId().equals(Long.parseLong(task.getCardNo()))).findFirst().orElse(null);
					if (Objects.isNull(smtStaff) || !StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode().equals(smtStaff.getStatus())) {
						//非临时工跳过
						log.info("许昌园区非临时工跳过ISC人员添加：{}", task.getCardNo());
						continue;
					}
				}

				// 添加人员同时添加人脸信息
				log.info("人员[{}]不存在于ISC平台，开始添加人员", task.getCardNo());
				String personId = addOrUpdatePerson(task, true);
				if (personId == null) {
					log.error("添加人员[{}]到ISC平台失败", task.getCardNo());
					handleTaskResult(task, ISCDeviceTaskEnum.ADD_PERSON_ERROR, null, null);
					continue;
				}
				log.info("成功添加人员[{}]到ISC平台，personId：{}", task.getCardNo(), personId);
				personIdMap.put(taskPersonKey, personId);
			}
		}

		return personIdMap;
	}

	/**
	 * 解析任务组的园区：逐任务解析设备归属；设备缺失的任务按 queryParkId 同语义标记（删除任务保留重试，下发任务取消），
	 * 返回可继续处理的任务（已填充parkId）。避免原先只处理组内第一个任务、其余滞留的问题。
	 */
	private List<SmtIscDeviceTask> resolveParkAndFilterGroup(List<SmtIscDeviceTask> tasks) {
		List<SmtIscDeviceTask> resolved = new ArrayList<>();
		Map<String, Integer> parkByDevice = new HashMap<>();
		for (SmtIscDeviceTask task : tasks) {
			Integer taskParkId = null;
			String deviceCode = task.getDeviceCode();
			if (StrUtil.isNotBlank(deviceCode)) {
				if (parkByDevice.containsKey(deviceCode)) {
					taskParkId = parkByDevice.get(deviceCode);
				} else {
					SmtDevice device = smtDeviceService.getOne(Wrappers.<SmtDevice>query().lambda()
							.eq(SmtDevice::getId, deviceCode));
					taskParkId = device == null ? null : device.getParkId();
					parkByDevice.put(deviceCode, taskParkId);
				}
			}
			if (taskParkId == null) {
				if (isDeleteAction(task.getAction())) {
					markDeleteTaskForRetry(task, ISCDeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE,
							"删除权限暂未找到设备信息");
				} else {
					handleTaskResult(task, ISCDeviceTaskEnum.DEVICE_DEVICE_NOT_ONLINE, null, null);
					task.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
					task.setUpdateTime(LocalDateTime.now());
					smtIscDeviceTaskService.updateById(task);
					triggerDispatchAggregationIfApplicable(task);
				}
				continue;
			}
			task.setParkId(taskParkId);
			resolved.add(task);
		}
		return resolved;
	}

	private void sendWechatMsg(String msg, String badge) {
		ThreadUtil.execute(() -> {
			try {
				log.info("许昌公众号微信通知：{}，工号：{}", msg, badge);
				WeChatMsgUtil.sendMsg(badge, msg, null, null);
			} catch (Exception e) {
				log.error("ISC消息推送失败:{}", e.getMessage());
			}
		});
	}

	private Integer[] getDeviceChannelNos(String deviceCode) {
		SmtDevice device = smtDeviceService.getById(deviceCode);
		Integer channelNo = device == null ? null : device.getChannelNo();
		if (channelNo == null || channelNo <= 0) {
			log.warn("ISC设备[{}]未配置通道号，暂按默认通道{}处理", deviceCode, DEFAULT_CHANNEL_NO);
			channelNo = DEFAULT_CHANNEL_NO;
		}
		return new Integer[]{channelNo};
	}

	private void markTaskResultBatch(List<SmtIscDeviceTask> taskList, DeviceTaskStatusEnum statusEnum, ISCDeviceTaskEnum taskEnum, String remark, String taskId) {
		for (SmtIscDeviceTask task : taskList) {
			task.setStatus(statusEnum.getCode());
			task.setCode(taskEnum.getCode());
			task.setRemark(StrUtil.isBlank(remark) ? taskEnum.getDesc() : remark);
			task.setIscTaskId(taskId);
			task.setUpdateTime(LocalDateTime.now());
			if (!smtIscDeviceTaskService.updateById(task)) {
				// 任务行可能已被并发删除（如重新下发清理），记录后继续处理其余任务
				log.warn("ISC任务状态回写失败（任务可能已被删除），taskId={}", task.getId());
			}
		}
	}

	private boolean isDeleteAction(Integer action) {
		return DeviceTaskActionEnum.DEL.getCode().equals(action) ||
				DeviceTaskActionEnum.DELAY_DEL.getCode().equals(action);
	}

	private boolean isNoAvailableDownloadData(String errorCode) {
		return ISCDeviceTaskErrorClassifier.isNoAvailableDownloadData(errorCode);
	}

	private boolean shouldVerifyMissingAuthForDelete(String errorCode) {
		return StrUtil.isBlank(errorCode) || isNoAvailableDownloadData(errorCode);
	}

	private String describeIscErrorForRemark(String errorCode) {
		if (StrUtil.isBlank(errorCode)) {
			return "ISC未返回错误码";
		}
		return ISCDeviceTaskErrorClassifier.describeForUser(errorCode);
	}

	private String buildAuthConfigDownFailRemark(String errorCode) {
		return ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getDesc() + "_" + describeIscErrorForRemark(errorCode);
	}

	private String buildMissingDownloadDetailRemark(String errorCode) {
		String remark = ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getDesc() + "_下载完成但ISC未返回该人员下载明细";
		if (StrUtil.isBlank(errorCode)) {
			return remark;
		}
		return remark + "（" + describeIscErrorForRemark(errorCode) + "）";
	}

	private String appendDeleteRetryRemark(String remark, ISCDeviceTaskEnum taskEnum) {
		String baseRemark = StrUtil.isBlank(remark) ? taskEnum.getDesc() : remark;
		return baseRemark + "，删除任务保留并将在1小时后重试";
	}

	private String appendDownloadRetryRemark(String remark, ISCDeviceTaskEnum taskEnum) {
		String baseRemark = StrUtil.isBlank(remark) ? taskEnum.getDesc() : remark;
		return baseRemark + "，任务保留并将在下次调度重试";
	}

	private boolean hasReachedAuthConfigMaxRetryTimes(SmtIscDeviceTask task) {
		return task.getTimes() != null && task.getTimes() >= DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES;
	}

	private String appendRetryExceededRemark(String remark, ISCDeviceTaskEnum taskEnum) {
		String baseRemark = StrUtil.isBlank(remark) ? taskEnum.getDesc() : remark;
		return baseRemark + "，已达到最大重试次数" + DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES + "次，停止自动重试，请人工介入处理";
	}

	private void markTaskRetryExceeded(SmtIscDeviceTask task, ISCDeviceTaskEnum taskEnum, String remark) {
		String retryExceededRemark = appendRetryExceededRemark(remark, taskEnum);
		LocalDateTime now = LocalDateTime.now();
		boolean updated = smtIscDeviceTaskService.update(new LambdaUpdateWrapper<SmtIscDeviceTask>()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.FAIL.getCode())
				.set(SmtIscDeviceTask::getCode, taskEnum.getCode())
				.set(SmtIscDeviceTask::getRemark, retryExceededRemark)
				.set(SmtIscDeviceTask::getIscTaskId, null)
				.set(SmtIscDeviceTask::getUpdateTime, now)
				.set(task.getTimes() != null, SmtIscDeviceTask::getTimes, task.getTimes())
				.eq(SmtIscDeviceTask::getId, task.getId()));
		if (updated) {
			task.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
			task.setCode(taskEnum.getCode());
			task.setRemark(retryExceededRemark);
			task.setIscTaskId(null);
			task.setUpdateTime(now);
			triggerDispatchAggregationIfApplicable(task);
		}
	}

	private void markDeleteTaskAsNoAvailableDownloadDataSuccess(SmtIscDeviceTask task) {
		task.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		task.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
		task.setRemark(DELETE_NO_AVAILABLE_DOWNLOAD_DATA_REMARK);
		task.setUpdateTime(LocalDateTime.now());
		boolean updated = smtIscDeviceTaskService.updateById(task);
		if (updated) {
			smtIscDownRecordService.handleTaskDownRecord(task);
			triggerDispatchAggregationIfApplicable(task);
		}
	}

	private enum DeletePersonLookupStatus {
		FOUND, ABSENT, QUERY_FAILED
	}

	private static final class DeletePersonLookupResult {
		private final DeletePersonLookupStatus status;
		private final String personId;

		private DeletePersonLookupResult(DeletePersonLookupStatus status, String personId) {
			this.status = status;
			this.personId = personId;
		}

		static DeletePersonLookupResult found(String personId) {
			return new DeletePersonLookupResult(DeletePersonLookupStatus.FOUND, personId);
		}

		static DeletePersonLookupResult absent() {
			return new DeletePersonLookupResult(DeletePersonLookupStatus.ABSENT, null);
		}

		static DeletePersonLookupResult queryFailed() {
			return new DeletePersonLookupResult(DeletePersonLookupStatus.QUERY_FAILED, null);
		}
	}

	/**
	 * 删除任务专用的三态人员查询：区分"在册(FOUND)/已删除(ABSENT)/查询失败(QUERY_FAILED)"。
	 * 背景：DHR同步会让ISC先删人并级联清掉其全部权限，此时删除任务已"事实完成"；
	 * 但查询失败不能冒险判成功，否则ISC故障期间会漏删真实权限。
	 * 员工任务按工号+证件号双标识复查，两者都确认不在册才判ABSENT。
	 */
	private DeletePersonLookupResult lookupIscPersonForDelete(SmtIscDeviceTask task) {
		String badge = null;
		String certNo = null;
		if (isTemporaryAuthorizationTask(task)) {
			try {
				certNo = resolveTemporaryAccessCertNo(task);
			} catch (Exception e) {
				log.warn("删除权限任务[{}]解析临时人员证件号异常，保持重试：{}", task.getId(), e.getMessage());
				return DeletePersonLookupResult.queryFailed();
			}
		} else {
			try {
				Result<InternalScheduleStaffIdentityRespDTO> staffInfo = remoteStaffService.getScheduleIdentityStaff(task.getCardNo(),
						SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (staffInfo != null && staffInfo.isSuccess() && staffInfo.getData() != null) {
					badge = staffInfo.getData().getBadge();
					certNo = staffInfo.getData().getCertno();
				}
			} catch (Exception e) {
				log.warn("删除权限任务[{}]查询本地员工信息异常，保持重试：{}", task.getId(), e.getMessage());
				return DeletePersonLookupResult.queryFailed();
			}
		}
		if (StrUtil.isBlank(badge) && StrUtil.isBlank(certNo)) {
			// 没有任何可用标识时无法确认人员状态，不冒险判成功
			return DeletePersonLookupResult.queryFailed();
		}
		if (StrUtil.isNotBlank(badge)) {
			DeletePersonLookupResult byBadge = queryIscPersonByParam(task, "jobNo", badge);
			if (byBadge.status != DeletePersonLookupStatus.ABSENT) {
				return byBadge;
			}
		}
		if (StrUtil.isNotBlank(certNo)) {
			DeletePersonLookupResult byCertNo = queryIscPersonByParam(task, "certificateNo", certNo);
			if (byCertNo.status != DeletePersonLookupStatus.ABSENT) {
				return byCertNo;
			}
		}
		return DeletePersonLookupResult.absent();
	}

	private DeletePersonLookupResult queryIscPersonByParam(SmtIscDeviceTask task, String paramName, String paramValue) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("paramName", paramName);
		params.put("paramValue", new String[]{paramValue});
		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setParkId(task.getParkId());
		dispatcherDTO.setEventType(EventEnum.ISC_PERSON_GET.getCode());
		dispatcherDTO.setData(params);
		Result<String> result;
		try {
			result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
		} catch (Exception e) {
			log.warn("删除权限任务[{}]按{}查询ISC人员异常，保持重试：{}", task.getId(), paramName, e.getMessage());
			return DeletePersonLookupResult.queryFailed();
		}
		if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
			log.info("删除权限任务[{}]按{}查询ISC人员失败，保持重试：{}", task.getId(), paramName,
					result == null ? "接口无响应" : result.getMessage());
			return DeletePersonLookupResult.queryFailed();
		}
		try {
			JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
			if (list == null || list.isEmpty()) {
				// 空列表是ISC"未查到该人员"的正常响应，按不在册处理（双标识复查兜底）
				return DeletePersonLookupResult.absent();
			}
			boolean hasBlankPersonIdRecord = false;
			for (int i = 0; i < list.size(); i++) {
				JSONObject personObj = list.getJSONObject(i);
				String personId = personObj.getStr("personId");
				if (StrUtil.isBlank(personId)) {
					hasBlankPersonIdRecord = true;
					continue;
				}
				Integer status = personObj.getInt("status");
				if (status == null || status >= 0) {
					return DeletePersonLookupResult.found(personId);
				}
			}
			if (hasBlankPersonIdRecord) {
				// 查到了记录但personId解析不出：更可能是响应结构异常，不冒险判人员已删除
				log.warn("删除权限任务[{}]按{}查询ISC返回记录缺少personId，保持重试", task.getId(), paramName);
				return DeletePersonLookupResult.queryFailed();
			}
			// 全部记录处于ISC删除状态（status<0）：等同人员已删除
			return DeletePersonLookupResult.absent();
		} catch (Exception e) {
			log.warn("删除权限任务[{}]解析ISC人员查询响应失败，保持重试：{}", task.getId(), e.getMessage());
			return DeletePersonLookupResult.queryFailed();
		}
	}

	private void markDeleteTaskAsPersonGoneSuccess(SmtIscDeviceTask task) {
		task.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		task.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
		task.setRemark(DELETE_PERSON_GONE_REMARK);
		task.setUpdateTime(LocalDateTime.now());
		boolean updated = smtIscDeviceTaskService.updateById(task);
		if (updated) {
			// 与既有删除成功收敛保持一致：清理本地下发记录，避免本地与ISC状态背离
			smtIscDownRecordService.handleTaskDownRecord(task);
			triggerDispatchAggregationIfApplicable(task);
		}
	}

	private void markDeleteTaskForRetry(SmtIscDeviceTask task, ISCDeviceTaskEnum taskEnum, String remark) {
		if (hasReachedAuthConfigMaxRetryTimes(task)) {
			markTaskRetryExceeded(task, taskEnum, remark);
			return;
		}
		long nextRetryTime = DateUtil.currentSeconds() + DELETE_TASK_RETRY_DELAY_SECONDS;
		String retryRemark = appendDeleteRetryRemark(remark, taskEnum);
		boolean updated = smtIscDeviceTaskService.update(new LambdaUpdateWrapper<SmtIscDeviceTask>()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
				.set(SmtIscDeviceTask::getCode, taskEnum.getCode())
				.set(SmtIscDeviceTask::getRemark, retryRemark)
				.set(SmtIscDeviceTask::getIscTaskId, null)
				.set(SmtIscDeviceTask::getOverTime, nextRetryTime)
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.set(task.getTimes() != null, SmtIscDeviceTask::getTimes, task.getTimes())
				.eq(SmtIscDeviceTask::getId, task.getId()));
		if (updated) {
			task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			task.setCode(taskEnum.getCode());
			task.setRemark(retryRemark);
			task.setIscTaskId(null);
			task.setOverTime(nextRetryTime);
			task.setUpdateTime(LocalDateTime.now());
		}
	}

	private void markDownloadTaskForRetry(SmtIscDeviceTask task, ISCDeviceTaskEnum taskEnum, String remark) {
		if (hasReachedAuthConfigMaxRetryTimes(task)) {
			markTaskRetryExceeded(task, taskEnum, remark);
			return;
		}
		String retryRemark = appendDownloadRetryRemark(remark, taskEnum);
		boolean updated = smtIscDeviceTaskService.update(new LambdaUpdateWrapper<SmtIscDeviceTask>()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
				.set(SmtIscDeviceTask::getCode, taskEnum.getCode())
				.set(SmtIscDeviceTask::getRemark, retryRemark)
				.set(SmtIscDeviceTask::getIscTaskId, null)
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.set(task.getTimes() != null, SmtIscDeviceTask::getTimes, task.getTimes())
				.eq(SmtIscDeviceTask::getId, task.getId()));
		if (updated) {
			task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			task.setCode(taskEnum.getCode());
			task.setRemark(retryRemark);
			task.setIscTaskId(null);
			task.setUpdateTime(LocalDateTime.now());
		}
	}

	private void markTaskFailureBatch(List<SmtIscDeviceTask> taskList, ISCDeviceTaskEnum taskEnum, String remark, String taskId) {
		for (SmtIscDeviceTask task : taskList) {
			if (isDeleteAction(task.getAction())) {
				markDeleteTaskForRetry(task, taskEnum, remark);
			} else {
				markDownloadTaskForRetry(task, taskEnum, remark);
			}
		}
	}

	private boolean handleNoAvailableDeleteDataForDeleteTasks(List<SmtIscDeviceTask> taskList, String taskId, String errorCode) {
		List<SmtIscDeviceTask> deleteTasks = taskList.stream()
				.filter(task -> isDeleteAction(task.getAction()))
				.collect(Collectors.toList());
		if (CollUtil.isEmpty(deleteTasks)) {
			return false;
		}
		Integer fallbackParkId = CollUtil.isEmpty(taskList) ? null : taskList.get(0).getParkId();
		String retryRemark = buildAuthConfigDownFailRemark(errorCode);
		for (SmtIscDeviceTask task : deleteTasks) {
			if (hasIscAuthItem(task, fallbackParkId)) {
				markDeleteTaskForRetry(task, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, retryRemark);
				log.info("删除权限下载任务[{}]复查未确认删除完成，保留删除任务重试，taskId：{}，code：{}",
						task.getId(), taskId, errorCode);
			} else {
				markDeleteTaskAsNoAvailableDownloadDataSuccess(task);
				log.info("删除权限下载任务[{}]无可用下发数据且未查到ISC权限条目，按删除成功处理，taskId：{}，code：{}",
						task.getId(), taskId, errorCode);
			}
		}
		return true;
	}

	private boolean hasIscAuthItem(SmtIscDeviceTask task, Integer fallbackParkId) {
		Integer parkId = task.getParkId() == null ? fallbackParkId : task.getParkId();
		if (parkId == null) {
			SmtDevice device = smtDeviceService.getOne(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getId,
					task.getDeviceCode()));
			parkId = device == null ? null : device.getParkId();
			task.setParkId(parkId);
		}
		String resourceType = ISCDeviceTypeEnum.getByCode(task.getDeviceType());
		String personId = resolveDeleteTaskPersonId(task);
		if (StrUtil.hasBlank(personId, task.getDeviceCode(), resourceType) || parkId == null) {
			log.warn("删除权限任务[{}]缺少权限条目复查参数，保持重试，personId：{}，deviceCode：{}，resourceType：{}，parkId：{}",
					task.getId(), personId, task.getDeviceCode(), resourceType, parkId);
			return true;
		}
		task.setPersonId(personId);
		Map<String, Object> resourceInfo = new HashMap<>(3);
		resourceInfo.put("resourceIndexCode", task.getDeviceCode());
		resourceInfo.put("resourceType", resourceType);
		resourceInfo.put("channelNos", Arrays.stream(getDeviceChannelNos(task.getDeviceCode()))
				.map(String::valueOf)
				.collect(Collectors.toList()));
		Map<String, Object> params = new HashMap<>(5);
		params.put("personIds", Collections.singletonList(personId));
		params.put("resourceInfos", Collections.singletonList(resourceInfo));
		params.put("queryType", resourceType);
		params.put("pageNo", AUTH_ITEM_QUERY_PAGE_NO);
		params.put("pageSize", AUTH_ITEM_QUERY_PAGE_SIZE);

		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setEventType(EventEnum.ISC_AUTH_ITEM_LIST_GET.getCode());
		dispatcherDTO.setData(params);
		Result<String> result;
		try {
			result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
		} catch (Exception e) {
			log.warn("删除权限任务[{}]复查ISC权限条目异常，保持重试：{}", task.getId(), e.getMessage());
			return true;
		}
		if (result == null) {
			log.warn("删除权限任务[{}]复查ISC权限条目响应为空，保持重试", task.getId());
			return true;
		}
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			log.warn("删除权限任务[{}]复查ISC权限条目失败，保持重试：{}", task.getId(), result.getMessage());
			return true;
		}
		try {
			JSONObject dataObj = JSONUtil.parseObj(result.getData());
			Integer total = dataObj.getInt("total");
			Object list = dataObj.get("list");
			if (total == null || !dataObj.containsKey("list")) {
				log.warn("删除权限任务[{}]复查ISC权限条目响应缺少total或list，保持重试：{}", task.getId(), result.getData());
				return true;
			}
			if (total > 0) {
				return true;
			}
			if (total < 0) {
				log.warn("删除权限任务[{}]复查ISC权限条目响应total小于0，保持重试：{}", task.getId(), result.getData());
				return true;
			}
			if (hasAuthItemList(list)) {
				log.warn("删除权限任务[{}]复查ISC权限条目响应total为0但list非空，保持重试：{}", task.getId(), result.getData());
				return true;
			}
			return false;
		} catch (Exception e) {
			log.warn("删除权限任务[{}]解析ISC权限条目响应失败，保持重试：{}", task.getId(), e.getMessage());
			return true;
		}
	}

	private boolean hasAuthItemList(Object list) {
		if (isNullAuthItemList(list)) {
			return false;
		}
		if (list instanceof JSONArray) {
			return !((JSONArray) list).isEmpty();
		}
		if (list instanceof Collection) {
			return !((Collection<?>) list).isEmpty();
		}
		return !JSONUtil.parseArray(list).isEmpty();
	}

	private boolean isNullAuthItemList(Object list) {
		return list == null || "null".equalsIgnoreCase(StrUtil.trim(String.valueOf(list)));
	}

	private boolean isIscTaskTimedOut(SmtIscDeviceTask task) {
		LocalDateTime baseTime = task.getUpdateTime() == null ? task.getCreateTime() : task.getUpdateTime();
		return baseTime != null && baseTime.plusHours(ISC_TASK_TIMEOUT_HOURS).isBefore(LocalDateTime.now());
	}

	private void markTimedOutTaskResultBatch(List<SmtIscDeviceTask> taskList, ISCDeviceTaskEnum taskEnum, String remark, String taskId) {
		List<SmtIscDeviceTask> timedOutTasks = taskList.stream()
				.filter(this::isIscTaskTimedOut)
				.collect(Collectors.toList());
		if (CollUtil.isNotEmpty(timedOutTasks)) {
			markTaskFailureBatch(timedOutTasks, taskEnum, remark, taskId);
		}
	}

	private Map<String, String> resolveDeletePersonIds(List<SmtIscDeviceTask> tasks) {
		Map<String, String> personIdMap = new HashMap<>();
		for (SmtIscDeviceTask task : tasks) {
			String personId = resolveDeleteTaskPersonId(task);
			if (StrUtil.isNotBlank(personId)) {
				personIdMap.put(taskPersonMapKey(task), personId);
			}
		}
		return personIdMap;
	}

	private String resolveDeleteTaskPersonId(SmtIscDeviceTask task) {
		if (task == null) {
			return null;
		}
		if (isTemporaryAccessTask(task)) {
			if (StrUtil.isNotBlank(task.getPersonId())) {
				return task.getPersonId();
			}
			return resolveCurrentIscPersonId(task);
		}
		SmtIscDownRecord downRecord = smtIscDownRecordService.getOne(Wrappers.<SmtIscDownRecord>lambdaQuery()
				.eq(SmtIscDownRecord::getCardNo, task.getCardNo())
				.eq(SmtIscDownRecord::getDeviceCode, task.getDeviceCode())
				.eq(SmtIscDownRecord::getDeviceType, task.getDeviceType())
				.eq(SmtIscDownRecord::getServiceType, task.getServiceType())
				.last("AND ROWNUM = 1"));
		Set<String> localPersonIdentifiers = collectLocalPersonIdentifiers(task, downRecord);
		String downRecordPersonId = downRecord == null ? null : downRecord.getPersonId();
		if (isTrustedOriginalPersonId(localPersonIdentifiers, downRecordPersonId)) {
			return downRecordPersonId;
		}
		if (isTrustedOriginalPersonId(localPersonIdentifiers, task.getPersonId())) {
			return task.getPersonId();
		}
		return resolveCurrentIscPersonId(task);
	}

	private boolean isTrustedOriginalPersonId(Set<String> localPersonIdentifiers, String personId) {
		if (StrUtil.isBlank(personId)) {
			return false;
		}
		return !localPersonIdentifiers.contains(StrUtil.trim(personId));
	}

	private Set<String> collectLocalPersonIdentifiers(SmtIscDeviceTask task, SmtIscDownRecord downRecord) {
		Set<String> identifiers = new HashSet<>();
		addLocalPersonIdentifier(identifiers, task.getCardNo());
		addLocalPersonIdentifier(identifiers, task.getBadge());
		addLocalPersonIdentifier(identifiers, extractBadgeFromGeneral(task.getGeneral()));
		addStaffBadgeIdentifier(identifiers, task.getCardNo());
		if (downRecord != null) {
			addLocalPersonIdentifier(identifiers, downRecord.getCardNo());
			addLocalPersonIdentifier(identifiers, downRecord.getBadge());
			addLocalPersonIdentifier(identifiers, extractBadgeFromGeneral(downRecord.getGeneral()));
			if (!StrUtil.equals(task.getCardNo(), downRecord.getCardNo())) {
				addStaffBadgeIdentifier(identifiers, downRecord.getCardNo());
			}
		}
		return identifiers;
	}

	private void addLocalPersonIdentifier(Set<String> identifiers, String identifier) {
		if (StrUtil.isNotBlank(identifier)) {
			identifiers.add(StrUtil.trim(identifier));
		}
	}

	private void addStaffBadgeIdentifier(Set<String> identifiers, String cardNo) {
		if (StrUtil.isBlank(cardNo)) {
			return;
		}
		try {
			Result<InternalScheduleStaffIdentityRespDTO> staffInfo = remoteStaffService.getScheduleIdentityStaff(cardNo,
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (staffInfo != null && staffInfo.isSuccess() && staffInfo.getData() != null) {
				addLocalPersonIdentifier(identifiers, staffInfo.getData().getBadge());
			}
		} catch (Exception e) {
			log.warn("删除权限任务回查员工工号失败，cardNo={}，message={}", cardNo, e.getMessage());
		}
	}

	private String extractBadgeFromGeneral(String general) {
		if (StrUtil.isBlank(general)) {
			return null;
		}
		int separatorIndex = general.indexOf("-");
		if (separatorIndex <= 0) {
			return null;
		}
		return StrUtil.trim(general.substring(0, separatorIndex));
	}

	/**
	 * 添加或删除权限配置
	 * @param taskList 任务列表
	 * @param isAdd 是否添加 True为添加权限 False为删除权限
	 * @return 返回添加或删除权限的结果
	 */
	private boolean addOrDelAuthConfig(List<SmtIscDeviceTask> taskList, boolean isAdd) {
		taskList = cancelUnsupportedVehicleTasks(taskList);
		if (CollectionUtil.isEmpty(taskList)) {
			return true;
		}

		//通过园区分组
		// Map<Integer, List<SmtIscDeviceTask>> parkTaskMap = taskList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getParkId));
		Map<Integer, List<SmtIscDeviceTask>> parkTaskMap = taskList.stream()
				.filter(task -> task.getParkId() != null)
				.collect(Collectors.groupingBy(SmtIscDeviceTask::getParkId));

		for(Integer parkId : parkTaskMap.keySet()){
			try {
				processParkAuthConfig(parkId, parkTaskMap.get(parkId), isAdd);
			} catch (Exception e) {
				// 单园区异常只影响本园区本轮，任务保持原状态下轮重试，不中断其他园区
				log.error("园区[{}]权限{}处理异常，本轮跳过该园区", parkId, isAdd ? "下发" : "删除", e);
			}
		}
		return true;
	}

	private void processParkAuthConfig(Integer parkId, List<SmtIscDeviceTask> smtIscDeviceTasks, boolean isAdd) {
		{
			// 新增/更新权限需要保证人员和照片在ISC侧存在；删除权限只解析已存在的ISC personId，不能反向创建人员。
			Set<Long> skipTaskIds = new HashSet<>();
			Map<String, String> personIdMap = isAdd ? downAccessPre(smtIscDeviceTasks, skipTaskIds) : resolveDeletePersonIds(smtIscDeviceTasks);
			if (CollUtil.isNotEmpty(skipTaskIds)) {
				log.info("园区[{}]本轮跳过{}个任务（ISC人员批量查询失败，待下轮重试）", parkId, skipTaskIds.size());
			}

            // 通过设备ID分组
			//Map<String, List<SmtIscDeviceTask>> groupByDevice = smtIscDeviceTasks.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getDeviceCode));
			Map<String, List<SmtIscDeviceTask>> groupByDevice = smtIscDeviceTasks.stream()
					.filter(task -> task.getDeviceCode() != null)
					.collect(Collectors.groupingBy(SmtIscDeviceTask::getDeviceCode));

			// 按照设备分类下发
			for (String deviceCode : groupByDevice.keySet()) {
				List<SmtIscDeviceTask> taskListTemp = groupByDevice.get(deviceCode);
				for (SmtIscDeviceTask item : taskListTemp) {
					if (skipTaskIds.contains(item.getId())) {
						continue;
					}
					item.setUpdateTime(LocalDateTime.now());
					item.setTimes(item.getTimes() == null ? 1 : item.getTimes() + 1);

					// 判断人员是否离职
					if (isAdd && !isTemporaryAuthorizationTask(item)) {
						SmtStaff smtStaff = smtStaffOtherService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getId, item.getCardNo()));
						if (smtStaff != null && Objects.equals(smtStaff.getStatus(), StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
							Integer previousStatus = item.getStatus();
							item.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
							item.setCode(ISCDeviceTaskEnum.PERSON_HAVE_RESIGNED.getCode());
							item.setRemark(ISCDeviceTaskEnum.PERSON_HAVE_RESIGNED.getDesc());
							smtIscDeviceTaskService.updateById(item);
							logTaskState("cancel_resigned", parkId, item, previousStatus, item.getRemark());
							triggerDispatchAggregationIfApplicable(item);
							continue;
						}
					}

					String personId = null;
                    if (personIdMap != null) {
                        personId = personIdMap.get(taskPersonMapKey(item));
                    }
					// 判断人员信息是否存在
					if(personId == null || StringUtils.isEmpty(personId)){
						log.info("event=isc_auth_person_lookup task_id={} park_id={} staff_id={} staff_no={} device_code={} action={} reason=person_id_missing",
								item.getId(), parkId, item.getCardNo(), logTaskBadge(item), item.getDeviceCode(), item.getAction());

						if (!isAdd) {
							// 删除任务三态判定：人员在ISC已删除时权限已被级联清理，按删除成功收敛
							DeletePersonLookupResult lookup = lookupIscPersonForDelete(item);
							if (lookup.status == DeletePersonLookupStatus.FOUND) {
								log.info("重新获取到人员信息，继续删除权限：cardNo={}, personId={}", item.getCardNo(), lookup.personId);
								personId = lookup.personId;
							} else if (lookup.status == DeletePersonLookupStatus.ABSENT) {
								log.info("删除权限任务[{}]对应人员已不存在于ISC平台，按删除成功处理", item.getId());
								markDeleteTaskAsPersonGoneSuccess(item);
								continue;
							} else {
								markDeleteTaskForRetry(item, ISCDeviceTaskEnum.PERSON_NOT_EXIST,
										"删除权限暂未解析到ISC人员ID");
								continue;
							}
						} else {
							// 尝试重新查询ISC平台人员信息
							JSONObject existingPerson = this.getPersonDetail(item);
							if (existingPerson != null) {
								String existingPersonId = existingPerson.getStr("personId");
								if (StrUtil.isNotBlank(existingPersonId)) {
									log.info("重新获取到人员信息，继续权限配置：cardNo={}, personId={}", item.getCardNo(), existingPersonId);
									personId = existingPersonId;
								}
							}

							// 如果仍然获取不到人员信息
							if (personId == null || StringUtils.isEmpty(personId)) {
								Integer previousStatus = item.getStatus();
								log.warn("event=isc_auth_person_lookup_failed task_id={} park_id={} staff_id={} staff_no={} device_code={} action={} reason=person_not_found",
										item.getId(), parkId, item.getCardNo(), logTaskBadge(item), item.getDeviceCode(), item.getAction());
								handleTaskResult(item, ISCDeviceTaskEnum.PERSON_NOT_EXIST, null, null);
								item.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
								// 更新任务状态
								item.setUpdateTime(LocalDateTime.now());
								smtIscDeviceTaskService.updateById(item);
								logTaskState("fail_person_not_found", parkId, item, previousStatus, item.getRemark());
								triggerDispatchAggregationIfApplicable(item);
								continue;
							}
						}
					}

					if (isAdd && isTemporaryAuthorizationTask(item) && !hasValidTemporaryAccessWindow(item)) {
						Integer previousStatus = item.getStatus();
						item.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
						item.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_ERROR.getCode());
						item.setRemark("临时人员权限缺少有效起止时间");
						item.setUpdateTime(LocalDateTime.now());
						smtIscDeviceTaskService.updateById(item);
						logTaskState("fail_invalid_access_window", parkId, item, previousStatus, item.getRemark());
						triggerDispatchAggregationIfApplicable(item);
						continue;
					}

					if (isAdd) {
						//添加权限
						byte[] imageByte = smtImageService.getImageBinaryByCode(item.getImageId());
						if (imageByte == null) {
							Integer previousStatus = item.getStatus();
							item.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
							item.setCode(ISCDeviceTaskEnum.FACE_IMG_NOT_EXIST.getCode());
							item.setRemark(ISCDeviceTaskEnum.FACE_IMG_NOT_EXIST.getDesc());
							// 更新任务CODE
							smtIscDeviceTaskService.updateById(item);
							logTaskState("cancel_face_missing", parkId, item, previousStatus, item.getRemark());
							triggerDispatchAggregationIfApplicable(item);
							continue;
						}
						// 判断照片是否在10KB到400KB之间
						if (imageByte.length < 1024 * 10 || imageByte.length > 1024 * 400) {
							Integer previousStatus = item.getStatus();
							item.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
							item.setCode(ISCDeviceTaskEnum.FACE_IMG_SIZE_ERROR.getCode());
							item.setRemark(ISCDeviceTaskEnum.FACE_IMG_SIZE_ERROR.getDesc());
							// 更新任务CODE
							smtIscDeviceTaskService.updateById(item);
							log.info("event=isc_auth_face_invalid task_id={} park_id={} staff_id={} staff_no={} device_code={} image_id={} image_length={} valid_length=10240-409600",
									item.getId(), parkId, item.getCardNo(), logTaskBadge(item), item.getDeviceCode(), item.getImageId(), imageByte.length);
							logTaskState("cancel_face_invalid", parkId, item, previousStatus, item.getRemark());
							triggerDispatchAggregationIfApplicable(item);
							if (item.getTimes() == 1) {
								sendWechatMsg(StrUtil.nullToEmpty(item.getGeneral()).concat("照片不合格，请更换照片"), item.getBadge());
							}
							continue;
						}
					}
					Integer previousStatus = item.getStatus();
					item.setStatus(DeviceTaskStatusEnum.DOING.getCode());
					item.setPersonId(personId);
					// 更新任务状态为处理中
					smtIscDeviceTaskService.updateById(item);
					logTaskState(isAdd ? "ready_for_add_dispatch" : "ready_for_delete_dispatch", parkId, item, previousStatus, null);
				}
				List<SmtIscDeviceTask> readyTasks = taskListTemp.stream()
						.filter(task -> DeviceTaskStatusEnum.DOING.getCode().equals(task.getStatus()))
						.filter(task -> StrUtil.isNotBlank(task.getPersonId()))
						.collect(Collectors.toList());
				if (CollUtil.isEmpty(readyTasks)) {
					continue;
				}
				Map<String, List<SmtIscDeviceTask>> authWindowTaskMap = groupByAuthConfigWindow(readyTasks, isAdd);
				for (List<SmtIscDeviceTask> authWindowTasks : authWindowTaskMap.values()) {
					for (int fromIndex = 0; fromIndex < authWindowTasks.size(); fromIndex += AUTH_CONFIG_PERSON_BATCH_SIZE) {
						List<SmtIscDeviceTask> batchTasks = authWindowTasks.subList(fromIndex, Math.min(fromIndex + AUTH_CONFIG_PERSON_BATCH_SIZE, authWindowTasks.size()));
						List<String> batchPersonIds = batchTasks.stream()
								.map(SmtIscDeviceTask::getPersonId)
								.filter(StrUtil::isNotBlank)
								.collect(Collectors.toList());
						// 人员信息
						Map<String, Object> personInfo = new HashMap<>(2);
						personInfo.put("indexCodes", batchPersonIds);
						personInfo.put("personDataType", "person");

						// 设备信息
						Map<String, Object> deviceInfo = new HashMap<>(3);
						deviceInfo.put("resourceIndexCode", deviceCode);
						deviceInfo.put("resourceType", ISCDeviceTypeEnum.getByCode(batchTasks.get(0).getDeviceType()));
						deviceInfo.put("channelNos", getDeviceChannelNos(deviceCode));
						// 最终参数信息
						Map<String, Object> params = new HashMap<>(2);
						params.put("resourceInfos", new Object[]{deviceInfo});
						params.put("personDatas", new Object[]{personInfo});

						// ISO8601格式
						params.put("startTime", getISO8601Timestamp(resolveAuthConfigStartTime(batchTasks, isAdd)));
						params.put("endTime", getISO8601Timestamp(resolveAuthConfigEndTime(batchTasks, isAdd)));

						// 园区分发
						DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
						dispatcherDTO.setEventId(IdUtil.simpleUUID());
						dispatcherDTO.setParkId(parkId);
						dispatcherDTO.setEventType(isAdd ? EventEnum.ISC_AUTH_CONFIG_ADD.getCode() : EventEnum.ISC_AUTH_CONFIG_DEL.getCode());
						dispatcherDTO.setData(params);
						long dispatchStartTime = System.currentTimeMillis();
						String taskIdSummary = summarizeTaskIds(batchTasks);
						log.info("event=isc_auth_dispatch_request event_id={} park_id={} device_code={} action={} task_count={} task_ids={} payload={}",
								dispatcherDTO.getEventId(), parkId, deviceCode, isAdd ? "ADD" : "DELETE", batchTasks.size(), taskIdSummary,
								IscLogPayloadFormatter.format(dispatcherDTO));
						try {
							Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
							long elapsedMillis = System.currentTimeMillis() - dispatchStartTime;
							log.info("event=isc_auth_dispatch_response event_id={} park_id={} device_code={} action={} task_count={} task_ids={} success={} result_code={} elapsed_ms={} payload={}",
									dispatcherDTO.getEventId(), parkId, deviceCode, isAdd ? "ADD" : "DELETE", batchTasks.size(), taskIdSummary,
									result != null && result.isSuccess(), result == null ? null : result.getCode(), elapsedMillis,
									IscLogPayloadFormatter.format(result));
							if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
								String resultMessage = result == null ? "ISC响应为空" : result.getMessage();
								log.warn("event=isc_auth_dispatch_failed event_id={} park_id={} device_code={} task_count={} task_ids={} result_message={}",
										dispatcherDTO.getEventId(), parkId, deviceCode, batchTasks.size(), taskIdSummary, resultMessage);
								markTaskFailureBatch(batchTasks, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
										ISCDeviceTaskEnum.AUTH_CONFIG_ERROR.getDesc() + "：" + resultMessage, null);
								continue;
							}
							String taskId = JSONUtil.parseObj(result.getData()).getStr("taskId");
							if (StrUtil.isBlank(taskId)) {
								// 不能带着空iscTaskId停在DOING：该状态不会被任何轮询查询命中
								markTaskFailureBatch(batchTasks, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
										ISCDeviceTaskEnum.AUTH_CONFIG_ERROR.getDesc() + "：ISC未返回配置任务ID", null);
								continue;
							}
							// 更新任务结果
							handleTaskResultBatch(batchTasks, ISCDeviceTaskEnum.AUTH_CONFIG_OK, taskId);
						} catch (Exception e) {
							// 异常不能让已置DOING的批次悬空（孤儿态），统一转失败重试
							log.error("event=isc_auth_dispatch_exception event_id={} park_id={} device_code={} action={} task_count={} task_ids={} elapsed_ms={}",
									dispatcherDTO.getEventId(), parkId, deviceCode, isAdd ? "ADD" : "DELETE", batchTasks.size(), taskIdSummary,
									System.currentTimeMillis() - dispatchStartTime, e);
							markTaskFailureBatch(batchTasks, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
									ISCDeviceTaskEnum.AUTH_CONFIG_ERROR.getDesc() + "：下发请求异常 " + e.getMessage(), null);
						}
					}
				}
			}
		}
	}

	/**
	 * 记录任务状态转换，确保单条日志可定位到人员、设备和 ISC 任务。
	 */
	private void logTaskState(String stage, Integer parkId, SmtIscDeviceTask task, Integer previousStatus, String reason) {
		log.info("event=isc_auth_task_state stage={} task_id={} park_id={} staff_id={} staff_no={} person_id={} device_code={} action={} service_type={} isc_task_id={} status_from={} status_to={} retry_times={} reason={}",
				stage, task.getId(), parkId, task.getCardNo(), logTaskBadge(task), task.getPersonId(), task.getDeviceCode(),
				task.getAction(), task.getServiceType(), task.getIscTaskId(), previousStatus, task.getStatus(), task.getTimes(), reason);
	}

	/**
	 * 临时人员任务的 badge 是证件号，日志中必须脱敏；员工任务的 badge 是工号，可完整记录。
	 */
	private String logTaskBadge(SmtIscDeviceTask task) {
		return isTemporaryAuthorizationTask(task) ? IscLogPayloadFormatter.maskCertificate(task.getBadge()) : task.getBadge();
	}

	/**
	 * 批量日志只保留有限任务 ID 摘要，避免单条日志被大量任务 ID 淹没。
	 */
	private String summarizeTaskIds(List<SmtIscDeviceTask> tasks) {
		String taskIds = tasks.stream().limit(ISC_LOG_TASK_ID_LIMIT).map(task -> String.valueOf(task.getId()))
				.collect(Collectors.joining(","));
		if (tasks.size() <= ISC_LOG_TASK_ID_LIMIT) {
			return taskIds;
		}
		return taskIds + "...[truncated=true,total=" + tasks.size() + "]";
	}

	private List<SmtIscDeviceTask> cancelUnsupportedVehicleTasks(List<SmtIscDeviceTask> taskList) {
		if (CollectionUtil.isEmpty(taskList)) {
			return Collections.emptyList();
		}
		List<SmtIscDeviceTask> supportedTasks = new ArrayList<>();
		for (SmtIscDeviceTask task : taskList) {
			if (!isIscVehicleTask(task)) {
				supportedTasks.add(task);
				continue;
			}
			task.setStatus(DeviceTaskStatusEnum.CANCEL.getCode());
			task.setRemark(ISC_VEHICLE_AUTH_UNSUPPORTED_REMARK);
			task.setUpdateTime(LocalDateTime.now());
			smtIscDeviceTaskService.updateById(task);
			log.info("ISC车辆权限不支持下发，已取消任务：taskId={}, cardNo={}, deviceCode={}",
					task.getId(), task.getCardNo(), task.getDeviceCode());
		}
		return supportedTasks;
	}

	private boolean hasValidTemporaryAccessWindow(SmtIscDeviceTask task) {
		return task.getStartTime() != null && task.getOverTime() != null && task.getStartTime() < task.getOverTime();
	}

	private Map<String, List<SmtIscDeviceTask>> groupByAuthConfigWindow(List<SmtIscDeviceTask> readyTasks, boolean isAdd) {
		Map<String, List<SmtIscDeviceTask>> windowTaskMap = new LinkedHashMap<>();
		for (SmtIscDeviceTask readyTask : readyTasks) {
				String windowKey = isAdd && isTemporaryAuthorizationTask(readyTask)
						? "temporary:" + readyTask.getStartTime() + ":" + readyTask.getOverTime()
						: "default";
			windowTaskMap.computeIfAbsent(windowKey, key -> new ArrayList<>()).add(readyTask);
		}
		return windowTaskMap;
	}

	private Date resolveAuthConfigStartTime(List<SmtIscDeviceTask> batchTasks, boolean isAdd) {
		SmtIscDeviceTask firstTask = batchTasks.get(0);
		if (isAdd && isTemporaryAuthorizationTask(firstTask)) {
			return DateUtil.date(firstTask.getStartTime() * 1000);
		}
		return DateTime.now();
	}

	private Date resolveAuthConfigEndTime(List<SmtIscDeviceTask> batchTasks, boolean isAdd) {
		SmtIscDeviceTask firstTask = batchTasks.get(0);
		if (isAdd && isTemporaryAuthorizationTask(firstTask)) {
			return DateUtil.date(firstTask.getOverTime() * 1000);
		}
		return DateUtil.parseDateTime("2030-12-31 23:59:59");
	}

	/**
	 * 传入Data类型日期，返回字符串类型时间（ISO8601标准时间）
	 * @param date
	 * @return
	 */
	private String getISO8601Timestamp(Date date){
		TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		df.setTimeZone(tz);
		return df.format(date);
	}

	/**
	 * 设备权限配置进度处理（调用快捷下载权限接口进行权限下发）
	 * 首先获取任务列表，然后根据任务列表中的设备ID和用户ID，调用快捷下载权限接口进行权限下发
	 */
	@Override
	public void authConfigProcessHandle() {
		List<SmtIscDeviceTask> authConfigList = smtIscDeviceTaskService.list(Wrappers.<SmtIscDeviceTask>lambdaQuery()
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.DOING.getCode())
				.eq(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.AUTH_CONFIG_OK.getCode())
				.isNotNull(SmtIscDeviceTask::getIscTaskId));
		log.info("zhongsj-快捷下载权限配置响应数据-开始 数量-{}", authConfigList.size());
		if (CollUtil.isEmpty(authConfigList)) {
			return;
		}
		Map<String, List<SmtIscDeviceTask>> taskIdMap = authConfigList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getIscTaskId));
		for (String taskId : taskIdMap.keySet()) {
			try {
				processAuthConfigGroup(taskId, taskIdMap.get(taskId));
			} catch (Exception e) {
				// 单组异常不中断其他组：<6h任务下轮重试，超时由markTimedOut兜底
				log.error("ISC权限配置进度组[{}]处理异常，本轮跳过", taskId, e);
			}
		}
	}

	private void processAuthConfigGroup(String taskId, List<SmtIscDeviceTask> groupTasks) {
		{
			List<SmtIscDeviceTask> taskList = resolveParkAndFilterGroup(groupTasks);
			if (CollUtil.isEmpty(taskList)) {
				return;
			}
			Map<String, String> configQuery = new HashMap<>(1);
			configQuery.put("taskId", taskId);
			// 园区分发
			DispatcherDTO<Map> configQueryDTO = new DispatcherDTO<>();
			configQueryDTO.setEventId(IdUtil.simpleUUID());
			configQueryDTO.setParkId(taskList.get(0).getParkId());
			configQueryDTO.setEventType(EventEnum.ISC_AUTH_CONFIG_PROCESS_GET.getCode());
			configQueryDTO.setData(configQuery);
			Result<String> result = remoteDispatcherService.dispatch(configQueryDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("查询权限配置[{}]进度失败：{}", taskId, result.getMessage());
				markTimedOutTaskResultBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
						"查询ISC权限配置进度超过" + ISC_TASK_TIMEOUT_HOURS + "小时仍失败：" + result.getMessage(), taskId);
				return;
			}
			log.info("event=isc_auth_config_progress_response isc_task_id={} task_count={} payload={}", taskId,
					taskList.size(), IscLogPayloadFormatter.format(result.getData()));
			JSONObject configProgressObj = JSONUtil.parseObj(result.getData());
			boolean isFinished = configProgressObj.getBool("isFinished", false);
			if (!isFinished) {
				markTimedOutTaskResultBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
						"ISC权限配置任务超过" + ISC_TASK_TIMEOUT_HOURS + "小时仍未完成", taskId);
				return;
			}
			Boolean isConfigFinished = configProgressObj.getBool("isConfigFinished");
			Integer failedNum = configProgressObj.getInt("failedNum", 0);
			Integer successedNum = configProgressObj.getInt("successedNum", 0);
			if (Boolean.FALSE.equals(isConfigFinished) && failedNum > 0 && successedNum == 0) {
				markTaskFailureBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_ERROR,
						"ISC权限配置完成但全部失败，failedNum=" + failedNum, taskId);
				return;
			}
			Map<String, List<SmtIscDeviceTask>> deviceMap = taskList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getDeviceCode));
			List<Map<String, Object>> deviceList = new ArrayList<>();
			for (String deviceCode : deviceMap.keySet()) {
				Map<String, Object> deviceInfo = new HashMap<>(3);
				deviceInfo.put("resourceIndexCode", deviceCode);
				deviceInfo.put("resourceType", ISCDeviceTypeEnum.getByCode(deviceMap.get(deviceCode).get(0).getDeviceType()));
				deviceInfo.put("channelNos", getDeviceChannelNos(deviceCode));
				deviceList.add(deviceInfo);
			}
			Map<String, Object> downAuth = new HashMap<>(2);
			downAuth.put("taskType", ISCTaskDownTypeEnum.TYPE_5.getCode());
			downAuth.put("resourceInfos", deviceList);
			// 园区分发
			DispatcherDTO<Map> downAuthDTO = new DispatcherDTO<>();
			downAuthDTO.setEventId(IdUtil.simpleUUID());
			downAuthDTO.setParkId(taskList.get(0).getParkId());
			downAuthDTO.setEventType(EventEnum.ISC_AUTH_CONFIG_DOWN.getCode());
			downAuthDTO.setData(downAuth);
			log.info("event=isc_auth_download_request event_id={} isc_task_id={} park_id={} task_count={} payload={}",
					downAuthDTO.getEventId(), taskId, downAuthDTO.getParkId(), taskList.size(), IscLogPayloadFormatter.format(downAuthDTO));
			Result<String> downResult = remoteDispatcherService.dispatch(downAuthDTO, SecurityConstants.FROM_IN);
			if (!downResult.isSuccess() || StrUtil.isBlank(downResult.getData())) {
				log.info("快捷下载权限配置[{}]失败：{}", taskId, downResult.getMessage());
				markTaskFailureBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
						ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getDesc() + "：" + downResult.getMessage(), taskId);
				return;
			}
			log.info("event=isc_auth_download_response isc_task_id={} task_count={} payload={}", taskId, taskList.size(),
					IscLogPayloadFormatter.format(downResult.getData()));
			String downTaskId = JSONUtil.parseObj(downResult.getData()).getStr("taskId");
			if (StrUtil.isBlank(downTaskId)) {
				// 不能带着空iscTaskId停在DOING+202：该状态不会被下载结果轮询命中
				markTaskFailureBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
						ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getDesc() + "：ISC未返回下载任务ID", taskId);
				return;
			}
			log.info("zhongsj-快捷下载权限配置响应数据结束：taskId-{},downTaskId-{}", taskId, downTaskId);
			taskList.forEach(item -> {
				item.setIscTaskId(downTaskId);
				item.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode());
				item.setRemark(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getDesc());
				item.setUpdateTime(LocalDateTime.now());
				log.info("zhongsj-快捷下载权限配置响应数据：taskId-{},id-{}", downTaskId,item.getId());
				smtIscDeviceTaskService.updateById(item);
			});
		}
	}

	/**
	 * 快捷下载权限配置响应数据处理
	 * 首先获取下载权限配置任务列表，然后根据任务列表中的任务id，获取下载权限配置响应数据，最后将响应数据保存到数据库中
	 */
	@Override
	public void authConfigDownResultHandle() {
		// 获取正在进行中的下载权限任务列表
		List<SmtIscDeviceTask> authConfigList = smtIscDeviceTaskService.list(Wrappers.<SmtIscDeviceTask>lambdaQuery()
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.DOING.getCode())
				.eq(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode())
				.isNotNull(SmtIscDeviceTask::getIscTaskId));
		log.info("ISC下载权限进度处理待处理数据量：{}", authConfigList.size());
		// 如果任务列表为空，则返回
		if (CollUtil.isEmpty(authConfigList)) {
			return;
		}
		// 将任务按照ISC任务ID分组
		Map<String, List<SmtIscDeviceTask>> taskIdMap = authConfigList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getIscTaskId));
		// 遍历分组后的任务列表
		for (String taskId : taskIdMap.keySet()) {
			try {
				processAuthConfigDownResultGroup(taskId, taskIdMap.get(taskId));
			} catch (Exception e) {
				// 单组异常不中断其他组：<6h任务下轮重试，超时由markTimedOut兜底
				log.error("ISC权限下载结果组[{}]处理异常，本轮跳过", taskId, e);
			}
		}
	}

	private void processAuthConfigDownResultGroup(String taskId, List<SmtIscDeviceTask> groupTasks) {
		{
			List<SmtIscDeviceTask> taskList = resolveParkAndFilterGroup(groupTasks);
			if (CollUtil.isEmpty(taskList)) {
				return;
			}
			// 记录处理开始时间
			long step1Start = System.currentTimeMillis();
			// 准备请求参数
			Map<String, String> params = new HashMap<>(1);
			params.put("taskId", taskId);
			// 创建园区分发请求对象
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(taskList.get(0).getParkId());
			dispatcherDTO.setEventType(EventEnum.ISC_TASK_PROCESS_GET.getCode());
			dispatcherDTO.setData(params);
			// 发送园区分发请求
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			// 处理请求结果
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("查询下载权限任务[{}]进度失败：{}", taskId, result.getMessage());
				markTimedOutTaskResultBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
						"查询ISC权限下载进度超过" + ISC_TASK_TIMEOUT_HOURS + "小时仍失败：" + result.getMessage(), taskId);
				return;
			}
			log.info("event=isc_auth_download_progress_response isc_task_id={} task_count={} elapsed_ms={} payload={}", taskId,
					taskList.size(), System.currentTimeMillis() - step1Start, IscLogPayloadFormatter.format(result.getData()));
			// 解析请求结果
			Integer downResult = 0;
			String errorCode = null;
			boolean isFinished;
			JSONObject dataObj = JSONUtil.parseObj(result.getData());
			if (null != dataObj.getObj("resourceDownloadProgress") && StrUtil.isNotEmpty(dataObj.getStr("resourceDownloadProgress"))) {
				JSONArray progressArr = dataObj.getJSONArray("resourceDownloadProgress");
				isFinished = true;
				for (int i = 0; i < progressArr.size(); i++) {
					JSONObject progressObj = progressArr.getJSONObject(i);
					isFinished = isFinished && Boolean.TRUE.equals(progressObj.getBool("isDownloadFinished"));
					Integer resourceDownResult = progressObj.getInt("downloadResult");
					if (Integer.valueOf(1).equals(resourceDownResult)) {
						downResult = 1;
						errorCode = progressObj.getStr("errorCode");
					} else if (Integer.valueOf(2).equals(resourceDownResult) && !Integer.valueOf(1).equals(downResult)) {
						downResult = 2;
						errorCode = progressObj.getStr("errorCode");
					}
				}
			} else {
				isFinished = Boolean.TRUE.equals(dataObj.getBool("isDownloadFinished"));
			}
			// 如果任务未完成，则继续处理
			if (!isFinished) {
				log.info("下载权限任务[{}]进度未完成，进度：{}", taskId, dataObj.getDouble("totalPercent"));
				markTimedOutTaskResultBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
						"ISC权限下载任务超过" + ISC_TASK_TIMEOUT_HOURS + "小时仍未完成", taskId);
				return;
			}
			// downloadResult=1代表整体失败；downloadResult=2代表部分失败，仍需查人员明细定位具体任务。
			if (Integer.valueOf(1).equals(downResult)) {
				if (shouldVerifyMissingAuthForDelete(errorCode) && handleNoAvailableDeleteDataForDeleteTasks(taskList, taskId, errorCode)) {
					List<SmtIscDeviceTask> nonDeleteTasks = taskList.stream()
							.filter(task -> !isDeleteAction(task.getAction()))
							.collect(Collectors.toList());
					if (CollUtil.isNotEmpty(nonDeleteTasks)) {
						markTaskFailureBatch(nonDeleteTasks, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
								buildAuthConfigDownFailRemark(errorCode), taskId);
					}
					return;
				}
				markTaskFailureBatch(taskList, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
						buildAuthConfigDownFailRemark(errorCode), taskId);
				log.info("下载权限任务[{}]失败，code：{}", taskId, errorCode);
				return;
			}

			// 将任务按照设备编码分组
			Map<String, List<SmtIscDeviceTask>> groupByDevice = taskList.stream().collect(Collectors.groupingBy(SmtIscDeviceTask::getDeviceCode));
			// 记录处理开始时间
			long step2Start = System.currentTimeMillis();
			// 遍历分组后的任务列表
			for (String deviceCode : groupByDevice.keySet()) {
				Map<String, Object> deviceMap = new HashMap<>(3);
				deviceMap.put("resourceIndexCode", deviceCode);
				deviceMap.put("resourceType", ISCDeviceTypeEnum.getByCode(taskList.get(0).getDeviceType()));
				deviceMap.put("channelNos", getDeviceChannelNos(deviceCode));
				// 记录处理开始时间
				long step4Start = System.currentTimeMillis();
				// 翻页拉取全部下载明细：明细可能超过单页1000条，只取第一页会把后续人员误判为"未返回明细"
				JSONObject detailObj = fetchAllDownloadDetail(taskId, deviceMap, taskList.get(0).getParkId());
				if (detailObj == null) {
					markTimedOutTaskResultBatch(groupByDevice.get(deviceCode), ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL,
							"查询ISC权限下载明细超过" + ISC_TASK_TIMEOUT_HOURS + "小时仍失败", taskId);
					continue;
				}
				log.info("查询下载权限任务耗时：{}ms，taskId-{}，明细条数：{}", System.currentTimeMillis() - step4Start, taskId,
						detailObj.getJSONArray("list") == null ? 0 : detailObj.getJSONArray("list").size());
				if (detailObj.getInt("total") == 0) {
					List<SmtIscDeviceTask> deviceTasks = groupByDevice.get(deviceCode);
					if (shouldVerifyMissingAuthForDelete(errorCode) && handleNoAvailableDeleteDataForDeleteTasks(deviceTasks, taskId, errorCode)) {
						List<SmtIscDeviceTask> nonDeleteTasks = deviceTasks.stream()
								.filter(task -> !isDeleteAction(task.getAction()))
								.collect(Collectors.toList());
						if (CollUtil.isNotEmpty(nonDeleteTasks)) {
							markTaskFailureBatch(nonDeleteTasks,
									ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, "下载完成但ISC未返回人员下载明细", taskId);
						}
						continue;
					}
					markTaskFailureBatch(deviceTasks, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, "下载完成但ISC未返回人员下载明细", taskId);
					continue;
				}

				authConfigDownResultHandleStep3(taskId, deviceCode, detailObj);

				// 将未处理到的任务状态修改为失败 等待下次重试
				List<SmtIscDeviceTask> unhandledDeviceTasks = filterTasksMissingFromDownloadDetail(groupByDevice.get(deviceCode), detailObj);
				String unhandledRemark = buildMissingDownloadDetailRemark(errorCode);
				List<SmtIscDeviceTask> unhandledDeleteTasks = unhandledDeviceTasks.stream()
						.filter(task -> isDeleteAction(task.getAction()))
						.collect(Collectors.toList());
				List<SmtIscDeviceTask> unhandledNonDeleteTasks = unhandledDeviceTasks.stream()
						.filter(task -> !isDeleteAction(task.getAction()))
						.collect(Collectors.toList());
				if (CollUtil.isNotEmpty(unhandledDeleteTasks) && (!shouldVerifyMissingAuthForDelete(errorCode) ||
						!handleNoAvailableDeleteDataForDeleteTasks(unhandledDeleteTasks, taskId, errorCode))) {
					markTaskFailureBatch(unhandledDeleteTasks, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, unhandledRemark, taskId);
				}
				if (CollUtil.isNotEmpty(unhandledNonDeleteTasks)) {
					markTaskFailureBatch(unhandledNonDeleteTasks, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, unhandledRemark, taskId);
				}

			}
			log.info("步骤二耗时：{}", System.currentTimeMillis() - step2Start);
		}
	}

	/**
	 * 翻页拉取某设备在指定下载任务下的全部人员明细，合并为 {total, list}。
	 * @return 合并后的明细对象；任一页查询失败返回 null（调用方按查询失败处理，保留重试/超时兜底）
	 */
	private JSONObject fetchAllDownloadDetail(String taskId, Map<String, Object> deviceMap, Integer parkId) {
		JSONArray mergedList = new JSONArray();
		Integer total = null;
		for (int pageNo = 1; pageNo <= DOWNLOAD_DETAIL_MAX_PAGES; pageNo++) {
			Map<String, Object> detail = new HashMap<>(4);
			detail.put("taskId", taskId);
			detail.put("resourceInfo", deviceMap);
			detail.put("pageNo", pageNo);
			detail.put("pageSize", DOWNLOAD_DETAIL_PAGE_SIZE);
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(parkId);
			dispatcherDTO.setEventType(EventEnum.ISC_TASK_RECORD_DETAIL_GET.getCode());
			dispatcherDTO.setData(detail);
			Result<String> detailResult;
			try {
				detailResult = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			} catch (Exception e) {
				log.warn("查询下载权限任务[{}]明细第{}页异常：{}", taskId, pageNo, e.getMessage());
				return null;
			}
			if (detailResult == null || !detailResult.isSuccess() || StrUtil.isBlank(detailResult.getData())) {
				log.info("查询下载权限任务[{}]明细第{}页失败：{}", taskId, pageNo,
						detailResult == null ? "接口无响应" : detailResult.getMessage());
				return null;
			}
			JSONObject pageObj = JSONUtil.parseObj(detailResult.getData());
			if (total == null) {
				total = pageObj.getInt("total");
			}
			JSONArray pageList = pageObj.getJSONArray("list");
			if (pageList == null || pageList.isEmpty()) {
				break;
			}
			mergedList.addAll(pageList);
			if (pageList.size() < DOWNLOAD_DETAIL_PAGE_SIZE
					|| (total != null && pageNo * DOWNLOAD_DETAIL_PAGE_SIZE >= total)) {
				break;
			}
			if (pageNo == DOWNLOAD_DETAIL_MAX_PAGES) {
				// 截断的明细会让未覆盖人员被误判"未返回明细"，宁可整体失败等下轮重试
				log.warn("查询下载权限任务[{}]明细超过最大页数{}，按查询失败处理等待重试", taskId, DOWNLOAD_DETAIL_MAX_PAGES);
				return null;
			}
		}
		JSONObject merged = new JSONObject();
		merged.put("total", total == null ? mergedList.size() : total);
		merged.put("list", mergedList);
		return merged;
	}

	/**
	 * 步骤三：处理下载权限任务结果
	 * @description: 根据下载权限任务结果，更新任务状态，并更新设备权限信息
	 * @param taskId 任务id
	 * @param deviceCode 设备编码
	 * @param detailObj 任务详情
	 */
	private void authConfigDownResultHandleStep3(String taskId, String deviceCode, JSONObject detailObj) {
		long start = System.currentTimeMillis();
		log.info("步骤三开始");
		JSONArray listArr = detailObj.getJSONArray("list");
		Map<String, DownDetailDTO> downDetailMap = transDownDetail(listArr);
		if (CollectionUtil.isEmpty(downDetailMap)) {
			return;
		}

		List<SmtIscDeviceTask> updateTaskList = smtIscDeviceTaskService.list(Wrappers.<SmtIscDeviceTask>lambdaQuery()
				.eq(SmtIscDeviceTask::getIscTaskId, taskId)
				.eq(SmtIscDeviceTask::getDeviceCode, deviceCode)
				.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.DOING.getCode())
				.eq(SmtIscDeviceTask::getCode, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_OK.getCode())
				.in(SmtIscDeviceTask::getPersonId, downDetailMap.keySet()));

		log.info("event=isc_auth_download_detail_update task_count={} task_ids={}", updateTaskList.size(),
				summarizeTaskIds(updateTaskList));
		if (CollectionUtil.isEmpty(updateTaskList)) {
			return;
		}
		updateTaskList.forEach(updateTask -> {
			DownDetailDTO downDetailDTO = downDetailMap.get(updateTask.getPersonId());
			if (downDetailDTO == null) {
				return;
			}
			String downResultCode = downDetailDTO.getDownResultCode();
			log.info("步骤三downResultCode：{}", downResultCode);
			log.info("步骤三getPersonId：{}", downDetailDTO.getPersonId());
			if ("0".equals(downResultCode)) {
				updateTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
				updateTask.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
				updateTask.setRemark(ISCDeviceTaskEnum.DEVICE_OK.getDesc());
			} else {
				String errorDesc;
				try {
					errorDesc = buildAuthConfigDownFailRemark(downResultCode);
					for (String detailErrorDesc : ISCDeviceTaskErrorClassifier.describeNestedErrors(
							downDetailDTO.getPersonDownloadDetail())) {
						errorDesc += "_" + detailErrorDesc;
					}
				} catch (Exception e) {
					log.info("解析JSON异常");
					errorDesc = ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getDesc() + "_解析海康isc返回的json数据错误";
				}
				if (isDeleteAction(updateTask.getAction())) {
					if (shouldVerifyMissingAuthForDelete(downResultCode) && !hasIscAuthItem(updateTask, null)) {
						markDeleteTaskAsNoAvailableDownloadDataSuccess(updateTask);
						log.info("删除权限任务[{}]无可用下发数据且未查到ISC权限条目，按删除成功处理", updateTask.getId());
						return;
					}
					markDeleteTaskForRetry(updateTask, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, errorDesc);
					log.info("删除权限任务失败，已保留重试：{}", updateTask);
					return;
				}
				markDownloadTaskForRetry(updateTask, ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL, errorDesc);
				log.info("下载权限任务失败，已保留重试：{}", updateTask);
				return;
			}
			updateTask.setUpdateTime(LocalDateTime.now());
			boolean update = smtIscDeviceTaskService.updateById(updateTask);
			log.info("步骤三更新完成：{}", updateTask);
			if (update && "0".equals(downResultCode)) {
				log.info("步骤三结束");
				// 下载明细确认成功后，才维护本地下发记录；删除权限也在这里移除记录。
				smtIscDownRecordService.handleTaskDownRecord(updateTask);
				triggerDispatchAggregationIfApplicable(updateTask);
				log.info("开始处理访客逻辑");
				handelVisitor(updateTask);
			}
		});
		log.info("步骤三结束，耗时：{}ms", System.currentTimeMillis() - start);
	}

	private List<SmtIscDeviceTask> filterTasksMissingFromDownloadDetail(List<SmtIscDeviceTask> taskList, JSONObject detailObj) {
		Set<String> detailPersonIds = extractDownloadDetailPersonIds(detailObj);
		return taskList.stream()
				.filter(task -> StrUtil.isBlank(task.getPersonId()) || !detailPersonIds.contains(task.getPersonId()))
				.collect(Collectors.toList());
	}

	private Set<String> extractDownloadDetailPersonIds(JSONObject detailObj) {
		JSONArray listArr = detailObj.getJSONArray("list");
		if (CollectionUtil.isEmpty(listArr)) {
			return Collections.emptySet();
		}
		Set<String> personIds = new HashSet<>();
		for (int i = 0; i < listArr.size(); i++) {
			String personId = listArr.getJSONObject(i).getStr("personId");
			if (StrUtil.isNotBlank(personId)) {
				personIds.add(personId);
			}
		}
		return personIds;
	}

	/**
	 * 下载权限任务响应数据提取转换
	 *
	 * @param listArr
	 * @return
	 */
	private Map<String, DownDetailDTO> transDownDetail(JSONArray listArr) {
		Map<String, DownDetailDTO> downDetailList = new HashMap<>(16);
		for (int i = 0; i < listArr.size(); i++) {
			DownDetailDTO downDetail = new DownDetailDTO();
			JSONObject downDetailObj = listArr.getJSONObject(i);
			String personId = downDetailObj.getStr("personId");
			if (Objects.isNull(personId)) {
				continue;
			}
			// 下载记录状态（只有成功/失败状态，0:成功,1:失败）
			String downResultCode = downDetailObj.getStr("persondownloadResult");
			downDetail.setPersonId(personId);
			downDetail.setDownResultCode(downResultCode);
			downDetail.setPersonDownloadDetail(downDetailObj.getJSONObject("personDownloadDetail"));
			downDetailList.put(personId, downDetail);
		}
		return downDetailList;
	}

	/**
	 * 处理访客预约成功回调
	 *
	 * @param deviceTask
	 */
	public void handelVisitor(SmtIscDeviceTask deviceTask) {
		if (isIscVehicleTask(deviceTask)) {
			log.info("ISC车辆权限不支持下发，跳过车辆访客删除任务生成，taskId={}, cardNo={}, deviceCode={}",
					deviceTask.getId(), deviceTask.getCardNo(), deviceTask.getDeviceCode());
			return;
		}
		if (!shouldGenerateTemporaryDeleteTask(deviceTask)) {
			return;
		}
		Date deleteTime = resolveTemporaryDeleteTime(deviceTask);
		if (deleteTime == null) {
			log.warn("临时人员权限下发成功后缺少删除任务结束时间，taskId={}, cardNo={}, personId={}",
					deviceTask.getId(), deviceTask.getCardNo(), deviceTask.getPersonId());
			return;
		}
		genDeviceTask(deviceTask, deleteTime);
	}

	private boolean shouldGenerateTemporaryDeleteTask(SmtIscDeviceTask deviceTask) {
		if (deviceTask == null || !DeviceTaskActionEnum.DOWN.getCode().equals(deviceTask.getAction())) {
			return false;
		}
			return isTemporaryAuthorizationTask(deviceTask);
	}

	private Date resolveTemporaryDeleteTime(SmtIscDeviceTask deviceTask) {
			if (isTemporaryAuthorizationTask(deviceTask)
					&& deviceTask.getOverTime() != null) {
			return DateUtil.date(deviceTask.getOverTime() * 1000);
		}
		String cardNo = deviceTask.getCardNo();
		if (StrUtil.isBlank(cardNo) || isVirtualIscCardNo(cardNo)) {
			return null;
		}
		//查询预约记录
		SmtVisitor smtVisitor = smtVisitorService.getById(cardNo);
		if (null != smtVisitor) {
			return smtVisitor.getEndTime();
		}
		//可能是随行人员的人脸下发通知
		SmtFellowVisitor fellowVisitor = smtFellowVisitorMapper.selectById(cardNo);
		if (null != fellowVisitor) {
			smtVisitor = smtVisitorService.getById(fellowVisitor.getVisitorId());
			return smtVisitor == null ? null : smtVisitor.getEndTime();
		}
		//入厂申请人脸下发通知
		Long admittanceFellowId = parseLong(cardNo);
		if (admittanceFellowId == null) {
			return null;
		}
		SmtAdmittanceFellow fellow = smtAdmittanceFellowMapper.selectById(admittanceFellowId);
		if (fellow == null) {
			return null;
		}
		SmtAdmittanceApply apply = smtAdmittanceApplyMapper.selectById(fellow.getVisitorId());
		if (apply == null || apply.getEndTime() == null) {
			return null;
		}
		return Date.from(apply.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
	}

	private boolean isIscVehicleTask(SmtIscDeviceTask deviceTask) {
		if (deviceTask == null) {
			return false;
		}
		return Objects.equals(deviceTask.getDeviceType(), DeviceTaskConstants.CAR)
				|| Objects.equals(deviceTask.getDeviceType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode());
	}

	private boolean isTemporaryAuthorizationTask(SmtIscDeviceTask deviceTask) {
		return isTemporaryAccessTask(deviceTask);
	}

	/**
	 * 生成访客删除任务 包括随行人员
	 *
	 * @param deviceTask
	 * @param endTime
	 */
	public void genDeviceTask(SmtIscDeviceTask deviceTask, Date endTime) {
		//开始生成访客删除任务
		log.info("event=isc_visitor_delete_task_create task_id={} park_id={} device_code={} end_time={}",
				deviceTask.getId(), deviceTask.getParkId(), deviceTask.getDeviceCode(), endTime);
		Long deleteOverTime = isTemporaryAuthorizationTask(deviceTask)
				? endTime.getTime() / 1000
				: ToolUtils.getDateEndTime(endTime).getTime() / 1000;
		//查询是否已生成删除任务
		LambdaQueryWrapper<SmtIscDeviceTask> existingDeleteTaskQuery = new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getDeviceCode, deviceTask.getDeviceCode())
				.eq(SmtIscDeviceTask::getDeviceType, deviceTask.getDeviceType())
				.eq(SmtIscDeviceTask::getServiceType, deviceTask.getServiceType())
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.in(SmtIscDeviceTask::getStatus, Arrays.asList(
						DeviceTaskStatusEnum.INIT.getCode(),
						DeviceTaskStatusEnum.DOING.getCode()));
		if (isTemporaryAccessTask(deviceTask)) {
			if (StrUtil.isNotBlank(deviceTask.getPersonId())) {
				existingDeleteTaskQuery.eq(SmtIscDeviceTask::getPersonId, deviceTask.getPersonId());
			} else if (StrUtil.isNotBlank(deviceTask.getBadge())) {
				existingDeleteTaskQuery.eq(SmtIscDeviceTask::getBadge, deviceTask.getBadge());
			} else {
				existingDeleteTaskQuery.eq(SmtIscDeviceTask::getCardNo, deviceTask.getCardNo());
			}
		} else {
			existingDeleteTaskQuery.eq(SmtIscDeviceTask::getCardNo, deviceTask.getCardNo());
		}
		List<SmtIscDeviceTask> existingDeleteTasks = smtIscDeviceTaskService.list(existingDeleteTaskQuery);
		log.info("检查任务是否已存在：{}", existingDeleteTasks == null ? 0 : existingDeleteTasks.size());
		if (CollectionUtil.isNotEmpty(existingDeleteTasks)) {
			mergeDeleteTaskOverTime(existingDeleteTasks, deleteOverTime);
			return;
		}
		SmtIscDeviceTask newDeviceTask = new SmtIscDeviceTask();
		BeanUtils.copyProperties(deviceTask, newDeviceTask);
		newDeviceTask.setId(null);
		newDeviceTask.setCreateTime(LocalDateTime.now());
		//获取结束时间
		// newDeviceTask.setStartTime(dateEndTime.getTime() / 1000);
		newDeviceTask.setOverTime(deleteOverTime);
		newDeviceTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		newDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		newDeviceTask.setIscTaskId(null);
		newDeviceTask.setUpdateTime(null);
		newDeviceTask.setRemark(null);
		newDeviceTask.setCode(null);
		newDeviceTask.setTimes(null);
		log.info("event=isc_visitor_delete_task_created task_id={} park_id={} device_code={} delete_over_time={}",
				newDeviceTask.getId(), newDeviceTask.getParkId(), newDeviceTask.getDeviceCode(), deleteOverTime);
		smtIscDeviceTaskService.save(newDeviceTask);
	}

	private void mergeDeleteTaskOverTime(List<SmtIscDeviceTask> existingDeleteTasks, Long deleteOverTime) {
		for (SmtIscDeviceTask existingDeleteTask : existingDeleteTasks) {
			if (existingDeleteTask.getOverTime() != null && existingDeleteTask.getOverTime() >= deleteOverTime) {
				continue;
			}
			existingDeleteTask.setOverTime(deleteOverTime);
			existingDeleteTask.setUpdateTime(LocalDateTime.now());
			smtIscDeviceTaskService.updateById(existingDeleteTask);
		}
	}

	private boolean addFace(SmtIscDeviceTask task, String personId) {
		try {
			if (StrUtil.isBlank(task.getImageId())) {
				log.info("人员[{}]缺少人脸图片ID", task.getCardNo());
				return false;
			}
			SmtImage faceImg = smtImageService.getByCode(task.getImageId());
			if (faceImg == null || faceImg.getImage() == null) {
				log.error("人员[{}]人脸图片不存在或为空", task.getCardNo());
				return false;
			}
			Map<String, Object> params = new HashMap<>(2);
			params.put("personId", personId);
			params.put("faceData", Base64.encode(faceImg.getImage()));
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(task.getParkId());
			dispatcherDTO.setEventType(EventEnum.ISC_FACE_ADD.getCode());
			dispatcherDTO.setData(params);
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				// 特殊处理：如果人脸已存在，视为成功
				if (result.getMessage() != null && result.getMessage().contains("PersonFace Exists")) {
					log.info("人员[{}]人脸已存在，跳过添加，personId：{}", task.getCardNo(), personId);
					return true;
				}
				log.info("添加人脸图片至ISC平台失败, ID:{}, {}", task.getCardNo(), result.getMessage());
				return false;
			}
			log.info("人员[{}]人脸添加成功，personId：{}", task.getCardNo(), personId);
			return true;
		} catch (Exception e) {
			log.error("添加人脸图片至ISC平台异常, ID:{}, ", task.getCardNo(), e);
			return false;
		}
	}

	private void recordIscFaceRetry(SmtIscDeviceTask task) {
		try {
			if (isTemporaryAuthorizationTask(task)) {
				log.warn("临时人员不按员工工号提交ISC人脸同步重试，taskId={}, cardNo={}", task.getId(), task.getCardNo());
				return;
			}
			String badge = task.getBadge();
			if (StrUtil.isBlank(badge)) {
				Result<InternalScheduleStaffIdentityRespDTO> staffInfo = remoteStaffService.getScheduleIdentityStaff(task.getCardNo(),
						SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (staffInfo != null && staffInfo.isSuccess() && staffInfo.getData() != null) {
					badge = staffInfo.getData().getBadge();
				}
			}
			if (StrUtil.isBlank(badge)) {
				log.warn("ISC人脸失败重试记录缺少人员标识，taskId={}, cardNo={}", task.getId(), task.getCardNo());
				return;
			}
			Result<Boolean> result = remoteStaffService.syncIscPersonFace(badge, task.getParkId(), task.getImageId(), SecurityConstants.FROM_IN);
			if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
				log.warn("ISC人脸失败已提交平台侧重试，badge={}, taskId={}, result={}", badge, task.getId(), result);
			}
		} catch (Exception e) {
			log.error("记录ISC人脸失败重试任务异常，taskId={}, cardNo={}", task.getId(), task.getCardNo(), e);
		}
	}

	private boolean modifyFace(SmtIscDeviceTask task, String faceId) {
		try {
			if (StrUtil.isBlank(task.getImageId())) {
				log.info("人员[{}]缺少人脸图片ID", task.getCardNo());
				return false;
			}
			SmtImage faceImg = smtImageService.getByCode(task.getImageId());
			if (faceImg == null || faceImg.getImage() == null) {
				log.error("人员[{}]人脸图片不存在或为空", task.getCardNo());
				return false;
			}
			Map<String, Object> params = new HashMap<>(2);
			params.put("faceId", faceId);
			params.put("faceData", Base64.encode(faceImg.getImage()));
			// 园区分发
			DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setParkId(task.getParkId());
			dispatcherDTO.setEventType(EventEnum.ISC_FACE_UPDATE.getCode());
			dispatcherDTO.setData(params);
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("更新人脸图片至ISC平台失败, ID:{}, {}", task.getCardNo(), result.getMessage());
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("更新人脸图片至ISC平台异常, ID:{}, ", task.getCardNo(), e);
			return false;
		}
	}

	@Override
	public void delAccess() {
		// 记录开始时间
		long begin = System.currentTimeMillis();
		// 记录成功数量
		int success = 0;
		cancelPendingIscVehicleTasks();

		// 创建任务列表
		List<SmtIscDeviceTask> taskList = new ArrayList<>();

		// 获取系统时区
		TimeZone timeZone = TimeZone.getDefault();

		// 获取当前服务器时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(timeZone);
		String currentTime = sdf.format(new Date());

		// 查询正常删除任务
		long currentSeconds = DateUtil.currentSeconds();
		stopExceededRetryAuthTasks();
		log.info("开始-查询ISC正常删除任务TaskList，时间戳：{} 时区：{} 当前服务器时间: {}", currentSeconds, timeZone, currentTime);
		IPage<SmtIscDeviceTask> normalTaskList = smtIscDeviceTaskService.getDel(newDeviceTaskPage(), currentSeconds,
				DeviceTaskConstants.CARD);
		log.info("结束-查询ISC正常删除任务TaskList，任务数量：{}", pageRecords(normalTaskList).size());
		// 获取正常删除任务列表
		log.info("开始-获取ISC正常删除任务列表");
		List<SmtIscDeviceTask> normalList = new ArrayList<>();
		normalList.addAll(pageRecords(normalTaskList));
		log.info("结束-获取ISC正常删除任务列表，任务数量：{}", normalList.size());
		// 将正常删除任务列表添加到任务列表中
		taskList.addAll(normalList);

		// 查询延迟删除任务
		currentSeconds = DateUtil.currentSeconds();
		log.info("开始-查询ISC延迟删除任务TaskList，时间戳：{}", currentSeconds);
		IPage<SmtIscDeviceTask> delayTaskList = smtIscDeviceTaskService.getDelayDel(newDeviceTaskPage(), currentSeconds,
				DeviceTaskConstants.CARD);
		log.info("结束-查询ISC延迟删除任务TaskList，任务数量：{}", pageRecords(delayTaskList).size());
		// 获取延迟删除任务列表
		log.info("开始-获取ISC延迟删除任务列表");
		List<SmtIscDeviceTask> delayList = new ArrayList<>();
		delayList.addAll(pageRecords(delayTaskList));
		log.info("结束-获取ISC延迟删除任务列表，任务数量：{}", delayList.size());
		// 将延迟删除任务列表添加到任务列表中
		taskList.addAll(delayList);

		// 记录日志，开始ISC人员权限删除任务，总数、正常任务、延迟任务
		log.info("开始-ISC人员权限删除任务，总数：{}, 正常任务: {}, 延迟任务: {}", taskList.size(),
				normalList.size(), delayList.size());
		// 调用addOrDelAuthConfig方法，删除ISC人员权限
		boolean update = addOrDelAuthConfig(taskList, false);
		// 如果删除成功，记录成功数量
		if (update) {
			success += taskList.size();
		}
		// 记录日志，完成ISC人员权限删除任务，耗时、成功、失败
		log.info("完成-ISC人员权限删除任务，耗时：{}，成功：{}，失败：{}", System.currentTimeMillis() - begin, success,
				taskList.size() - success);
	}

	private Page<SmtIscDeviceTask> newDeviceTaskPage() {
		Page<SmtIscDeviceTask> page = new Page<>();
		page.setCurrent(1);
		page.setSize(500);
		return page;
	}

	private void cancelPendingIscVehicleTasks() {
		LambdaUpdateWrapper<SmtIscDeviceTask> updateWrapper = Wrappers.<SmtIscDeviceTask>lambdaUpdate()
				.set(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.CANCEL.getCode())
				.set(SmtIscDeviceTask::getRemark, ISC_VEHICLE_AUTH_UNSUPPORTED_REMARK)
				.set(SmtIscDeviceTask::getUpdateTime, LocalDateTime.now())
				.in(SmtIscDeviceTask::getDeviceType, DeviceTaskConstants.CAR, DeviceTypeEnum.DEVICE_TYPE_3.getCode())
				.in(SmtIscDeviceTask::getStatus,
						DeviceTaskStatusEnum.INIT.getCode(),
						DeviceTaskStatusEnum.DOING.getCode(),
						DeviceTaskStatusEnum.FAIL.getCode(),
						DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
		if (smtIscDeviceTaskService.update(updateWrapper)) {
			log.info("已取消历史ISC车辆权限任务，原因：{}", ISC_VEHICLE_AUTH_UNSUPPORTED_REMARK);
		}
	}

	@Override
	public void syncDevice() {
		log.info("开始-ISC设备同步任务，开始时间: {}", DateUtils.now());
		Result<List<SmtParkDTO>> parkListRes = remoteParkService.getParkList(SecurityConstants.FROM_IN);
		if (!parkListRes.isSuccess() || CollectionUtil.isEmpty(parkListRes.getData())) {
			log.info("查询园区列表失败：{}", parkListRes.getMessage());
			return;
		}
		for (SmtParkDTO park : parkListRes.getData()) {
			Integer current = 1;
			Integer size = 50;
			//本地设备列表
			List<SmtDevice> localDeviceList = smtDeviceService.list(Wrappers.<SmtDevice>lambdaQuery()
					.eq(SmtDevice::getIsSync, DeviceSyncEnum.YES.getCode())
					.eq(SmtDevice::getParkId, park.getId())
					.eq(SmtDevice::getDeviceType, ISCDeviceTypeEnum.TYPE_1.getCode())
					.orderByDesc(SmtDevice::getCreateTime));
			//远程设备列表
			Map<String, JSONObject> remoteDeviceMap = new HashMap<>();
			for (; ; ) {
				Result<String> result = getDeviceListData(current, size, ISCDeviceTypeEnum.TYPE_1, park.getId());
				if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
					log.info("获取园区[{}]设备列表信息失败：{}", park.getId(), result.getMessage());
					break;
				}
				JSONObject dataJson = JSONUtil.parseObj(result.getData());
				JSONArray deviceArr = dataJson.getJSONArray("list");
				if (Objects.isNull(deviceArr) || deviceArr.isEmpty()) {
					log.info("获取园区[{}]设备列表信息失败,列表为空：{}", park.getId(), result.getMessage());
					break;
				}
				Integer total = dataJson.getInt("total");
				if (total == null) {
					break;
				}
				for (int i = 0; i < deviceArr.size(); i++) {
					try {
						JSONObject item = deviceArr.getJSONObject(i);
						String deviceId = item.getStr("indexCode");
						String devTypeDesc = item.getStr("devTypeDesc");
						// accessDeviceMode为支持人脸的设备型号
						if (StrUtil.isBlank(deviceId) || !accessDeviceMode.contains(devTypeDesc)) {
							continue;
						}
						remoteDeviceMap.put(deviceId, item);
					}catch (Exception e){}
				}
				if (!hasNext(current, size, total)) {
					break;
				}
				current++;
			}
			// 对比本地设备与远程设备列表差异
			log.info("本地设备列表：{}", localDeviceList);
			log.info("园区设备列表：{}", remoteDeviceMap);
			Map<String, SmtDevice> updDeviceMap = new HashMap<>(16);
			List<SmtDevice> otherUpdList = new ArrayList<>();
			for (SmtDevice device : localDeviceList) {
				// 更新设备信息
				// 更新此设备连接状态为离线
				device.setConnectStatus(DeviceConstants.OFF_LINE);
				if (remoteDeviceMap.containsKey(device.getId())) {
					JSONObject item = remoteDeviceMap.get(device.getId());
					device.setChannelNo(item.getInt("channelNo"));
					device.setDeviceCode(item.getStr("devSerialNum"));
					device.setDeviceName(item.getStr("name"));
					device.setIsSync(DeviceSyncEnum.YES.getCode());
					device.setUpdateTime(LocalDateTime.now());
					updDeviceMap.put(device.getId(), device);
					// 移除已存在的设备，剩下的就是新增的设备
					remoteDeviceMap.remove(device.getId());
				} else {
					otherUpdList.add(device);
				}
			}
			if (CollUtil.isNotEmpty(updDeviceMap)) {
				log.info("园区已存在设备更新设备列表：{}", updDeviceMap);
				syncDeviceDetail(updDeviceMap, park.getId());
				for (SmtDevice device : updDeviceMap.values()) {
					smtDeviceService.updateById(device);
				}
			}
			if (CollUtil.isNotEmpty(otherUpdList)) {
				log.info("园区不存在设备更新设备列表：{}", otherUpdList);
				for (SmtDevice device : otherUpdList) {
					smtDeviceService.updateById(device);
				}
			}
			log.info("移除后园区设备列表：{}", remoteDeviceMap);
			// 新增的设备同步
			Map<String, SmtDevice> addDeviceMap = new HashMap<>(16);
			remoteDeviceMap.values().forEach(item -> {
				SmtDevice device = new SmtDevice();
				device.setId(item.getStr("indexCode"));
				device.setParkId(park.getId());
				device.setDeviceName(item.getStr("name"));
				device.setDeviceType(ISCDeviceTypeEnum.TYPE_1.getCode());
				device.setDeviceCode(item.getStr("devSerialNum"));
				device.setIsSync(DeviceSyncEnum.YES.getCode());
				device.setEnableStatus(DeviceConstants.ENABLE);
				device.setCreateTime(LocalDateTime.now());
				addDeviceMap.put(device.getId(), device);
			});
			if (CollUtil.isNotEmpty(addDeviceMap)) {
				log.info("园区新增设备更新设备列表：{}", addDeviceMap);
				List<SmtDevice> successList = syncDeviceDetail(addDeviceMap, park.getId());
				// 获取设备详情异常了不新增
				if (CollUtil.isNotEmpty(successList)) {
					for (SmtDevice device : successList) {
						smtDeviceService.save(device);
					}
				}
			}
		}
		log.info("完成-ISC设备同步任务，开始时间: {}", DateUtils.now());
	}

	private Result<String> getDeviceListData(Integer current, Integer size, ISCDeviceTypeEnum type, Integer parkId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("pageNo", current);
		params.put("pageSize", size);
		// 园区分发
		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		switch (type) {
			case TYPE_1:
				dispatcherDTO.setEventType(EventEnum.ISC_ACCESS_DEVICE_GET.getCode());
				break;
		}
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setData(params);
		return remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
	}

	/**
	 * 查询测温列表
	 *
	 * @return
	 */
	@Override
	public void getTemperature() {
		ZoneOffset offset = ZoneOffset.of("+08:00");
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		String startTime = OffsetDateTime.of(LocalDateTime.now().plusMinutes(-20), offset).format(df);
		String endTime = OffsetDateTime.of(LocalDateTime.now(), offset).format(df);
		List<IscTemperatureDTO> temperatureList = new ArrayList<>();
		int i = 1;
		for (; ; ) {
			List<IscTemperatureDTO> result = this.getTemp(i, startTime, endTime);
			temperatureList.addAll(result);
			if (CollUtil.isEmpty(result)) {
				break;
			}
			i++;
		}
		if (CollUtil.isEmpty(temperatureList)) {
			return;
		}
		//log.info("读取温度{}：",temperatureList.toString());
		remoteSnapPersonService.checkTemperature(temperatureList, SecurityConstants.FROM_IN);
	}

	private List<IscTemperatureDTO> getTemp(Integer current, String startTime, String endTime) {
		List<IscTemperatureDTO> userList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>(6);
		params.put("pageNo", current);
		params.put("pageSize", 1000);
		params.put("sort", "alarmTime");
		params.put("order", "asc");
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		// 园区分发
		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.ISC_TEMPERATURE_GET.getCode());
		dispatcherDTO.setParkId(hfParkId);
		dispatcherDTO.setData(params);
		Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
//		ResponseEntity<String> result1 = new RestTemplate().postForEntity("http://10.0.20.113:8082/dispatcher/dispatch",
//				dispatcherDTO, String.class);
//		String result = result1.getBody();
//		Result<Boolean> result2 = JSONUtil.toBean(result, Result.class);
		if (!result.isSuccess() || !result.getCode().equals(0)) {
			log.error("从ISC平台获取温度参数失败：{}", result.getMessage());
			return userList;
		}
		if (StrUtil.isBlank(result.getData())) {
			return userList;
		}
		JSONObject dataJson = JSONUtil.parseObj(result.getData());
		String listStr = dataJson.getStr("list");
		if (StrUtil.isEmpty(listStr)) {
			return userList;
		}
		JSONArray listArr = dataJson.getJSONArray("list");
		for (int i = 0; i < listArr.size(); i++) {
			IscTemperatureDTO temperature = new IscTemperatureDTO();
			JSONObject item = listArr.getJSONObject(i);
			if (StringUtils.isEmpty(item.getStr("certNo"))) {
				continue;
			}
			temperature.setAlarmTime(item.getStr("alarmTime"));
			temperature.setCertNo(item.getStr("certNo"));
			temperature.setResourceName(item.getStr("resourceName"));
			temperature.setTemp(item.getStr("temp"));
			userList.add(temperature);
		}
		//根据身份证号去重
		List<IscTemperatureDTO> trueList = userList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
				new TreeSet<>(Comparator.comparing(IscTemperatureDTO::getCertNo))), ArrayList::new));
		return trueList;
	}

	/**
	 * 获取ISC平台设备详情信息
	 */
	private List<SmtDevice> syncDeviceDetail(Map<String, SmtDevice> deviceMap, Integer parkId) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("pageSize", deviceMap.keySet().size());
		params.put("indexCodes", deviceMap.keySet());
		// 园区分发
		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setEventType(EventEnum.ISC_ACCESS_DEVICE_STATUS_GET.getCode());
		dispatcherDTO.setData(params);
		try {
			Result<String> result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
				log.info("获取设备详情信息失败：{}", result.getMessage());
				return Collections.emptyList();
			}
			Map<Integer,List<String>> parkBadges = new HashMap<>();
			JSONObject dataJson = JSONUtil.parseObj(result.getData());
			JSONArray dataList = dataJson.getJSONArray("list");
			log.info("ISC平台设备详情列表：{}", dataList);
			List<SmtDevice> successList = new ArrayList<>();
			for (int i = 0; i < dataList.size(); i++) {
				JSONObject item = dataList.getJSONObject(i);
				SmtDevice device = deviceMap.get(item.getStr("indexCode"));

				//查询园区模板消息模板
				if(!parkBadges.containsKey(device.getParkId())){
					List<String> badges = smtMsgTempService.getBadgeByParkId(device.getParkId());
					parkBadges.put(device.getParkId(),badges);
				}

				// 设备厂家：1-海康；2-大华；
				device.setDeviceVendor(1);
				device.setDeviceIp(item.getStr("ip"));
				device.setDevicePort(item.getInt("port"));
				// 在线状态，0离线，1在线
				device.setConnectStatus(ISCDeviceStatusEnum.getStatusByCode(item.getInt("online")));

				deviceOfflineMsg(device,parkBadges.get(device.getParkId()));

				successList.add(device);
			}
			return successList;
		} catch (Exception e) {
			log.error("获取设备[{}]详情异常：", deviceMap.keySet(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * 设备离线/上线 消息处理
	 * @param device
	 * @param badges
	 */
	private void deviceOfflineMsg(SmtDevice device,List<String> badges){
		try {
			if (DeviceConstants.OFF_LINE.equals(device.getConnectStatus())) {
				// 设备离线
				// 微信通知设备离线 并记录redis key
				if (CollectionUtil.isNotEmpty(badges)) {
					ValueOperations<String, String> value = redisTemplate.opsForValue();
					String redisKey = SMAT_DEVICE_OFFLINE_MSG + device.getId();
					String val = value.get(redisKey);
					if (StringUtils.isEmpty(val)) {
						// 发送通知消息
						for (String badge : badges) {
							sendWechatMsg(device.getDeviceName().concat("已离线,请及时查看详细信息！"), badge);
						}
						value.set(redisKey, DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
					}
				}
			} else if (DeviceConstants.ON_LINE.equals(device.getConnectStatus())) {
				// 设备上线
				// 如果已经记录过通知记录 则删除key
				ValueOperations<String, String> value = redisTemplate.opsForValue();
				String redisKey = SMAT_DEVICE_OFFLINE_MSG + device.getId();
				String val = value.get(redisKey);
				if (!StringUtils.isEmpty(val)) {
					// 发送通知消息
					for (String badge : badges) {
						sendWechatMsg(device.getDeviceName().concat("已恢复上线，请知悉！"), badge);
					}

					redisTemplate.delete(redisKey);
				}
			}
		} catch (Exception e){
			log.error("设备离线消息通知失败,",e);
		}
	}

	private boolean hasNext(Integer current, Integer size, Integer total) {
		Integer totalPage = total / size + (total % size == 0 ? 0 : 1);
		return current < totalPage;
	}
}
