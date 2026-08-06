package com.tce.smart.platform.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtFaceImgTask;
import com.tce.smart.platform.core.entity.SmtFaceImgTaskDetails;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtFaceImgTaskDetailsMapper;
import com.tce.smart.platform.core.mapper.SmtFaceImgTaskMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtStaffOtherService;
import com.tce.smart.platform.service.ISCPersonService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class ISCPersonServiceImpl implements ISCPersonService {

	private static final String ISC_PERSON_FACE_RETRY_TASK_NAME = "ISC人员照片同步失败重试";
	private static final int RETRY_PAGE_SIZE = 50;
	private static final int MAX_RETRY_TIMES = 6;
	private static final String RETRY_PREFIX = "[retry=";
	private static final String ISC_VIRTUAL_CARD_PREFIX = "999";
	private static final String ISC_CARD_NO_PATTERN = "[0-9A-Z]{8,20}";

	@Autowired
	private RemoteDispatcherService remoteDispatcherService;

	@Autowired
	private SmtStaffOtherService smtStaffOtherService;

	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private SmtFaceImgTaskMapper smtFaceImgTaskMapper;

	@Autowired
	private SmtFaceImgTaskDetailsMapper smtFaceImgTaskDetailsMapper;

	@Value("${smart.org.xc-hpo-index-code}")
	private String xcHpoOrgIndexCode;

	@Value("${smart.org.hf-index-code}")
	private String hfOrgIndexCode;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public Boolean updateISCPersonFace(String badge, Integer parkId, byte[] faceImg) {
		return updateISCPersonFace(badge, parkId, faceImg, null);
	}

	@Override
	public Boolean updateISCPersonFace(String badge, Integer parkId, byte[] faceImg, String imageId) {
		if (StrUtil.isNotBlank(imageId) && (faceImg == null || faceImg.length == 0)) {
			recordIscPersonFaceFailure(badge, parkId, imageId, "人脸图片不存在或读取失败");
			return Boolean.FALSE;
		}
		IscSyncResult syncResult = updateISCPersonFaceInternal(badge, parkId, faceImg);
		if (!syncResult.success) {
			recordIscPersonFaceFailure(badge, parkId, imageId, syncResult.reason);
		}
		return syncResult.success;
	}

	@Override
	public Boolean syncISCPersonCard(String badge, Integer parkId, String cardNo) {
		if (isBlankOrVirtualCard(cardNo)) {
			return Boolean.TRUE;
		}
		if (!isValidIscCardNo(cardNo)) {
			log.warn("ISC实体卡号格式非法，跳过同步：badge={}, parkId={}, cardNo={}", badge, parkId, cardNo);
			return Boolean.FALSE;
		}
		IscPersonQueryResult queryResult = ensureIscPerson(badge, parkId);
		if (!queryResult.success || queryResult.notFound) {
			return Boolean.FALSE;
		}
		String personId = queryResult.personDetail.getStr("personId");
		if (StrUtil.isBlank(personId)) {
			log.warn("ISC人员缺少personId，不能同步卡号：badge={}, parkId={}, cardNo={}", badge, parkId, cardNo);
			return Boolean.FALSE;
		}
		Result<String> result = dispatch(parkId, EventEnum.ISC_CARD_ADD, buildAddCardParams(personId, cardNo));
		if (result == null || !result.isSuccess()) {
			log.error("同步ISC人员卡号失败，工号：{}，园区：{}，卡号：{}，错误：{}", badge, parkId, cardNo,
					result == null ? "接口无响应" : result.getMessage());
			return Boolean.FALSE;
		}
		log.info("同步ISC人员卡号成功，工号：{}，园区：{}，卡号：{}", badge, parkId, cardNo);
		return Boolean.TRUE;
	}

	@Override
	public Boolean deleteISCPersonCard(String badge, Integer parkId, String cardNo) {
		if (isBlankOrVirtualCard(cardNo)) {
			return Boolean.TRUE;
		}
		if (!isValidIscCardNo(cardNo)) {
			log.warn("ISC实体卡号格式非法，跳过删除：badge={}, parkId={}, cardNo={}", badge, parkId, cardNo);
			return Boolean.FALSE;
		}
		IscPersonQueryResult queryResult = queryISCPersonByBadge(badge, parkId);
		if (!queryResult.success || queryResult.notFound || queryResult.personDetail == null) {
			log.warn("未查询到ISC人员，拒绝仅按卡号删除人员卡片：badge={}, parkId={}, cardNo={}", badge, parkId, cardNo);
			return Boolean.FALSE;
		}
		String personId = queryResult.personDetail.getStr("personId");
		if (StrUtil.isBlank(personId)) {
			log.warn("ISC人员缺少personId，拒绝删除人员卡片：badge={}, parkId={}, cardNo={}", badge, parkId, cardNo);
			return Boolean.FALSE;
		}
		Result<String> result = dispatch(parkId, EventEnum.ISC_CARD_DELETE, buildDeleteCardParams(personId, cardNo));
		if (result == null || !result.isSuccess()) {
			log.error("删除ISC人员卡号失败，工号：{}，园区：{}，卡号：{}，错误：{}", badge, parkId, cardNo,
					result == null ? "接口无响应" : result.getMessage());
			return Boolean.FALSE;
		}
		log.info("删除ISC人员卡号成功，工号：{}，园区：{}，卡号：{}", badge, parkId, cardNo);
		return Boolean.TRUE;
	}

	private Map<String, Object> buildAddCardParams(String personId, String cardNo) {
		Map<String, Object> card = new HashMap<>(3);
		card.put("cardNo", cardNo);
		card.put("personId", personId);
		card.put("cardType", 1);
		Map<String, Object> params = new HashMap<>(1);
		params.put("cardList", Collections.singletonList(card));
		return params;
	}

	private Map<String, Object> buildDeleteCardParams(String personId, String cardNo) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("cardNumber", cardNo);
		params.put("personId", personId);
		return params;
	}

	@Override
	public void retryFailedPersonFaceSync() {
		List<SmtFaceImgTask> tasks = smtFaceImgTaskMapper.selectList(Wrappers.<SmtFaceImgTask>lambdaQuery()
				.eq(SmtFaceImgTask::getTaskName, ISC_PERSON_FACE_RETRY_TASK_NAME));
		if (tasks == null || tasks.isEmpty()) {
			return;
		}
		List<Long> taskIds = tasks.stream().map(SmtFaceImgTask::getId).collect(java.util.stream.Collectors.toList());
		Page<SmtFaceImgTaskDetails> page = new Page<>(1, RETRY_PAGE_SIZE);
		List<SmtFaceImgTaskDetails> details = smtFaceImgTaskDetailsMapper.selectPage(page,
				Wrappers.<SmtFaceImgTaskDetails>lambdaQuery()
						.in(SmtFaceImgTaskDetails::getTaskId, taskIds)
						.eq(SmtFaceImgTaskDetails::getStatus, DeviceDownStatusEnum.FAIL.getCode())
						.and(wrapper -> wrapper.notLike(SmtFaceImgTaskDetails::getRemark, RETRY_PREFIX + MAX_RETRY_TIMES + "]")
								.or().isNull(SmtFaceImgTaskDetails::getRemark))
						.orderByDesc(SmtFaceImgTaskDetails::getCreateTime)).getRecords();
		if (details == null || details.isEmpty()) {
			return;
		}
		for (SmtFaceImgTaskDetails detail : details) {
			retryOneFailedPersonFaceSync(detail);
		}
		for (SmtFaceImgTask task : tasks) {
			refreshRetryTaskSuccessNum(task.getId());
		}
	}

	private IscSyncResult updateISCPersonFaceInternal(String badge, Integer parkId, byte[] faceImg) {
		try {
			if (StrUtil.isBlank(badge) || parkId == null) {
				log.warn("ISC平台同步人员人脸缺少必要参数：badge={}, parkId={}", badge, parkId);
				return IscSyncResult.fail("缺少工号或园区ID");
			}
			log.info("ISC平台同步人员和人脸开始，badge={}, parkId={}", badge, parkId);
			SmtStaff staff = smtStaffOtherService.getOne(new LambdaQueryWrapper<SmtStaff>()
					.eq(SmtStaff::getBadge, badge)
					.last("AND ROWNUM = 1"));
			IscPersonQueryResult queryResult = queryISCPersonByBadge(badge, parkId);
			if (!queryResult.success) {
				return IscSyncResult.fail(queryResult.reason);
			}
			if (queryResult.notFound) {
				return addISCPerson(staff, badge, parkId, faceImg)
						? IscSyncResult.success()
						: IscSyncResult.fail("新增ISC人员失败");
			}
			JSONObject personDetail = queryResult.personDetail;
			Integer status = personDetail.getInt("status");
			if (status != null && status < 0) {
				log.warn("ISC人员[{}]已删除，不能直接复用personId，请先在ISC侧恢复或清理重复人员", badge);
				return IscSyncResult.fail("ISC人员已删除，不能复用personId");
			}
			String personId = personDetail.getStr("personId");
			if (StrUtil.isBlank(personId)) {
				log.warn("ISC人员[{}]缺少personId，响应：{}", badge, personDetail);
				return IscSyncResult.fail("ISC人员缺少personId");
			}
			boolean personUpdated = updateISCPerson(staff, personId, parkId);
			if (!personUpdated) {
				return IscSyncResult.fail("更新ISC人员信息失败");
			}
			if (faceImg == null || faceImg.length == 0) {
				return IscSyncResult.success();
			}
			JSONArray photoArr = personDetail.getJSONArray("personPhoto");
			String faceId = (photoArr != null && !photoArr.isEmpty()) ? photoArr.getJSONObject(0).getStr("personPhotoIndexCode") : null;
			boolean faceUpdated = StrUtil.isBlank(faceId) ? addISCFace(badge, personId, faceImg, parkId) : updateISCFace(badge, personId, faceId, faceImg, parkId);
			return faceUpdated ? IscSyncResult.success() : IscSyncResult.fail("同步ISC人脸失败");
		} catch (Exception e) {
			log.error("同步ISC人员人脸异常：工号={}", badge, e);
			return IscSyncResult.fail("同步ISC人员人脸异常：" + e.getMessage());
		}
	}

	private IscPersonQueryResult queryISCPersonByBadge(String badge, Integer parkId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("paramName", "jobNo");
		params.put("paramValue", new String[]{badge});
		Result<String> result = dispatch(parkId, EventEnum.ISC_PERSON_GET, params);
		if (result == null || !result.isSuccess() || StrUtil.isBlank(result.getData())) {
			String message = result == null ? "ISC人员查询接口无响应" : result.getMessage();
			log.info("从ISC平台查询人员[{}]失败：{}", badge, message);
			return IscPersonQueryResult.fail(message);
		}
		JSONArray list = JSONUtil.parseObj(result.getData()).getJSONArray("list");
		if (list == null || list.isEmpty()) {
			log.info("ISC平台未查询到人员，工号：{}", badge);
			return IscPersonQueryResult.notFound();
		}
		return IscPersonQueryResult.found(list.getJSONObject(0));
	}

	private IscPersonQueryResult ensureIscPerson(String badge, Integer parkId) {
		IscPersonQueryResult queryResult = queryISCPersonByBadge(badge, parkId);
		if (!queryResult.success || !queryResult.notFound) {
			return queryResult;
		}
		SmtStaff staff = findStaffByBadge(badge);
		if (!addISCPerson(staff, badge, parkId, null)) {
			return IscPersonQueryResult.fail("新增ISC人员失败");
		}
		queryResult = queryISCPersonByBadge(badge, parkId);
		if (queryResult.success && !queryResult.notFound) {
			return queryResult;
		}
		Map<String, Object> person = new HashMap<>(1);
		person.put("personId", badge);
		return IscPersonQueryResult.found(JSONUtil.parseObj(person));
	}

	private boolean isBlankOrVirtualCard(String cardNo) {
		return StrUtil.isBlank(cardNo) || cardNo.startsWith(ISC_VIRTUAL_CARD_PREFIX);
	}

	private boolean isValidIscCardNo(String cardNo) {
		return StrUtil.isNotBlank(cardNo) && cardNo.matches(ISC_CARD_NO_PATTERN);
	}

	private Boolean addISCPerson(SmtStaff staff, String badge, Integer parkId, byte[] faceImg) {
		if (staff == null) {
			log.warn("本地未查询到员工[{}]，无法向ISC新增人员", badge);
			return Boolean.FALSE;
		}
		Map<String, Object> params = buildPersonParams(staff, parkId);
		if (faceImg != null && faceImg.length > 0) {
			Map<String, Object> face = new HashMap<>(1);
			face.put("faceData", Base64.encode(faceImg));
			params.put("faces", new Object[]{face});
		}
		Result<String> result = dispatch(parkId, EventEnum.ISC_PERSON_ADD, params);
		if (result == null || !result.isSuccess()) {
			log.error("新增人员至ISC平台失败，工号：{}，错误：{}", badge, result == null ? "接口无响应" : result.getMessage());
			return Boolean.FALSE;
		}
		log.info("新增人员至ISC平台成功，工号：{}", badge);
		return Boolean.TRUE;
	}

	private boolean updateISCPerson(SmtStaff staff, String personId, Integer parkId) {
		if (staff == null) {
			return true;
		}
		Map<String, Object> params = buildPersonParams(staff, parkId);
		params.put("personId", personId);
		Result<String> result = dispatch(parkId, EventEnum.ISC_PERSON_UPDATE, params);
		if (result == null || !result.isSuccess()) {
			log.error("更新ISC人员信息失败，工号：{}，personId：{}，错误：{}", staff.getBadge(), personId, result == null ? "接口无响应" : result.getMessage());
			return false;
		}
		log.info("更新ISC人员信息成功，工号：{}，personId：{}", staff.getBadge(), personId);
		return true;
	}

	private Map<String, Object> buildPersonParams(SmtStaff staff, Integer parkId) {
		Map<String, Object> params = new HashMap<>(12);
		params.put("personId", staff.getBadge());
		params.put("personName", staff.getName());
		params.put("gender", staff.getSex() == null ? 0 : staff.getSex().equals(0) ? 1 : 2);
		params.put("orgIndexCode", xcParkId.equals(parkId) ? xcHpoOrgIndexCode : hfOrgIndexCode);
		params.put("birthday", staff.getBirth());
		params.put("phoneNo", staff.getPhone());
		params.put("email", staff.getEmail());
		params.put("certificateType", "111");
		params.put("certificateNo", staff.getCertno());
		params.put("jobNo", staff.getBadge());
		return params;
	}

	private boolean addISCFace(String badge, String personId, byte[] faceImg, Integer parkId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("personId", personId);
		params.put("faceData", Base64.encode(faceImg));
		Result<String> result = dispatch(parkId, EventEnum.ISC_FACE_ADD, params);
		if (result == null || !result.isSuccess()) {
			if (result != null && result.getMessage() != null && result.getMessage().contains("PersonFace Exists")) {
				log.info("ISC人员人脸已存在，跳过新增，工号：{}，personId：{}", badge, personId);
				return true;
			}
			log.error("添加人脸图片至ISC平台失败，工号：{}，personId：{}，错误：{}", badge, personId, result == null ? "接口无响应" : result.getMessage());
			return false;
		}
		log.info("添加人脸图片至ISC平台成功，工号：{}，personId：{}", badge, personId);
		return true;
	}

	private boolean updateISCFace(String badge, String personId, String faceId, byte[] faceImg, Integer parkId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("faceId", faceId);
		params.put("faceData", Base64.encode(faceImg));
		Result<String> result = dispatch(parkId, EventEnum.ISC_FACE_UPDATE, params);
		if (result == null || !result.isSuccess()) {
			log.error("更新ISC人脸图片失败，工号：{}，personId：{}，faceId：{}，错误：{}", badge, personId, faceId, result == null ? "接口无响应" : result.getMessage());
			return false;
		}
		log.info("更新ISC人脸图片成功，工号：{}，personId：{}，faceId：{}", badge, personId, faceId);
		return true;
	}

	private void recordIscPersonFaceFailure(String badge, Integer parkId, String imageId, String reason) {
		if (StrUtil.isBlank(badge) || parkId == null) {
			return;
		}
		try {
			SmtStaff staff = findStaffByBadge(badge);
			SmtFaceImgTask task = getOrCreateRetryTask(parkId);
			SmtFaceImgTaskDetails detail = findOpenRetryDetail(task.getId(), staff, badge);
			String imgCode = StrUtil.isNotBlank(imageId) ? imageId : staff == null ? null : staff.getFacePicId();
			String remark = buildRetryRemark(0, reason);
			if (detail == null) {
				detail = SmtFaceImgTaskDetails.builder()
						.taskId(task.getId())
						.staffId(staff == null ? null : staff.getId())
						.imgName(badge)
						.imgCode(imgCode)
						.status(DeviceDownStatusEnum.FAIL.getCode())
						.remark(remark)
						.createTime(LocalDateTime.now())
						.build();
				smtFaceImgTaskDetailsMapper.insert(detail);
				task.setTotalNum(task.getTotalNum() == null ? 1 : task.getTotalNum() + 1);
				smtFaceImgTaskMapper.updateById(task);
				return;
			}
			detail.setStaffId(staff == null ? detail.getStaffId() : staff.getId());
			detail.setImgCode(StrUtil.isBlank(imgCode) ? detail.getImgCode() : imgCode);
			detail.setRemark(remark);
			detail.setCreateTime(LocalDateTime.now());
			smtFaceImgTaskDetailsMapper.updateById(detail);
		} catch (Exception e) {
			log.error("记录ISC人员照片同步失败任务异常，工号：{}", badge, e);
		}
	}

	private void retryOneFailedPersonFaceSync(SmtFaceImgTaskDetails detail) {
		int retryTimes = parseRetryTimes(detail.getRemark());
		if (retryTimes >= MAX_RETRY_TIMES) {
			detail.setRemark(buildRetryRemark(MAX_RETRY_TIMES, "已达到最大重试次数，等待人工处理"));
			smtFaceImgTaskDetailsMapper.updateById(detail);
			return;
		}
		SmtStaff staff = detail.getStaffId() == null ? null : smtStaffOtherService.getById(detail.getStaffId());
		if (staff == null && StrUtil.isNotBlank(detail.getImgName())) {
			staff = findStaffByBadge(detail.getImgName());
		}
		String badge = staff == null ? detail.getImgName() : staff.getBadge();
		String imageCode = staff != null && StrUtil.isNotBlank(staff.getFacePicId()) ? staff.getFacePicId() : detail.getImgCode();
		byte[] faceImg = null;
		if (StrUtil.isNotBlank(imageCode)) {
			try {
				faceImg = smtImageService.getImageBinaryByCode(imageCode);
			} catch (Exception e) {
				log.error("读取待重试ISC人脸图片异常，工号：{}，图片：{}", badge, imageCode, e);
			}
			if (faceImg == null || faceImg.length == 0) {
				int nextRetryTimes = retryTimes + 1;
				detail.setRemark(buildRetryRemark(nextRetryTimes, "人脸图片不存在或读取失败"));
				detail.setCreateTime(LocalDateTime.now());
				smtFaceImgTaskDetailsMapper.updateById(detail);
				return;
			}
		}
		IscSyncResult result = updateISCPersonFaceInternal(badge, getTaskParkId(detail.getTaskId()), faceImg);
		if (result.success) {
			detail.setStatus(DeviceDownStatusEnum.SUCCESS.getCode());
			detail.setRemark("ISC人员照片同步成功");
			detail.setCreateTime(LocalDateTime.now());
			smtFaceImgTaskDetailsMapper.updateById(detail);
			return;
		}
		int nextRetryTimes = retryTimes + 1;
		detail.setRemark(buildRetryRemark(nextRetryTimes, result.reason));
		detail.setCreateTime(LocalDateTime.now());
		smtFaceImgTaskDetailsMapper.updateById(detail);
	}

	private SmtFaceImgTask getOrCreateRetryTask(Integer parkId) {
		List<SmtFaceImgTask> tasks = smtFaceImgTaskMapper.selectList(Wrappers.<SmtFaceImgTask>lambdaQuery()
				.eq(SmtFaceImgTask::getTaskName, ISC_PERSON_FACE_RETRY_TASK_NAME)
				.eq(SmtFaceImgTask::getParkId, parkId)
				.orderByDesc(SmtFaceImgTask::getCreateTime));
		if (tasks != null && !tasks.isEmpty()) {
			return tasks.get(0);
		}
		SmtFaceImgTask task = SmtFaceImgTask.builder()
				.taskName(ISC_PERSON_FACE_RETRY_TASK_NAME)
				.parkId(parkId)
				.totalNum(0)
				.successNum(0)
				.createTime(LocalDateTime.now())
				.build();
		smtFaceImgTaskMapper.insert(task);
		return task;
	}

	private SmtFaceImgTaskDetails findOpenRetryDetail(Long taskId, SmtStaff staff, String badge) {
		LambdaQueryWrapper<SmtFaceImgTaskDetails> wrapper = Wrappers.<SmtFaceImgTaskDetails>lambdaQuery()
				.eq(SmtFaceImgTaskDetails::getTaskId, taskId)
				.eq(SmtFaceImgTaskDetails::getStatus, DeviceDownStatusEnum.FAIL.getCode())
				.orderByDesc(SmtFaceImgTaskDetails::getCreateTime);
		if (staff != null) {
			wrapper.eq(SmtFaceImgTaskDetails::getStaffId, staff.getId());
		} else {
			wrapper.eq(SmtFaceImgTaskDetails::getImgName, badge);
		}
		List<SmtFaceImgTaskDetails> details = smtFaceImgTaskDetailsMapper.selectList(wrapper);
		return details == null || details.isEmpty() ? null : details.get(0);
	}

	private SmtStaff findStaffByBadge(String badge) {
		if (StrUtil.isBlank(badge)) {
			return null;
		}
		return smtStaffOtherService.getOne(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getBadge, badge)
				.last("AND ROWNUM = 1"));
	}

	private Integer getTaskParkId(Long taskId) {
		SmtFaceImgTask task = smtFaceImgTaskMapper.selectById(taskId);
		return task == null ? xcParkId : task.getParkId();
	}

	private void refreshRetryTaskSuccessNum(Long taskId) {
		Integer successNum = smtFaceImgTaskDetailsMapper.selectCount(Wrappers.<SmtFaceImgTaskDetails>lambdaQuery()
				.eq(SmtFaceImgTaskDetails::getTaskId, taskId)
				.eq(SmtFaceImgTaskDetails::getStatus, DeviceDownStatusEnum.SUCCESS.getCode()));
		SmtFaceImgTask task = smtFaceImgTaskMapper.selectById(taskId);
		if (task != null) {
			task.setSuccessNum(successNum);
			smtFaceImgTaskMapper.updateById(task);
		}
	}

	private int parseRetryTimes(String remark) {
		if (StrUtil.isBlank(remark)) {
			return 0;
		}
		int start = remark.indexOf(RETRY_PREFIX);
		if (start < 0) {
			return 0;
		}
		int numberStart = start + RETRY_PREFIX.length();
		int end = remark.indexOf("]", numberStart);
		if (end < 0) {
			return 0;
		}
		try {
			return Integer.parseInt(remark.substring(numberStart, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String buildRetryRemark(int retryTimes, String reason) {
		String retryDesc = retryTimes >= MAX_RETRY_TIMES ? "已达最大重试次数" : "等待重试";
		String message = StrUtil.blankToDefault(reason, "未知错误");
		return "ISC人员照片同步失败" + RETRY_PREFIX + retryTimes + "]，" + retryDesc + "：" + message;
	}

	private static class IscSyncResult {
		private final boolean success;
		private final String reason;

		private IscSyncResult(boolean success, String reason) {
			this.success = success;
			this.reason = reason;
		}

		private static IscSyncResult success() {
			return new IscSyncResult(true, null);
		}

		private static IscSyncResult fail(String reason) {
			return new IscSyncResult(false, reason);
		}
	}

	private static class IscPersonQueryResult {
		private final boolean success;
		private final boolean notFound;
		private final JSONObject personDetail;
		private final String reason;

		private IscPersonQueryResult(boolean success, boolean notFound, JSONObject personDetail, String reason) {
			this.success = success;
			this.notFound = notFound;
			this.personDetail = personDetail;
			this.reason = reason;
		}

		private static IscPersonQueryResult found(JSONObject personDetail) {
			return new IscPersonQueryResult(true, false, personDetail, null);
		}

		private static IscPersonQueryResult notFound() {
			return new IscPersonQueryResult(true, true, null, null);
		}

		private static IscPersonQueryResult fail(String reason) {
			return new IscPersonQueryResult(false, false, null, reason);
		}
	}

	private Result<String> dispatch(Integer parkId, EventEnum eventEnum, Map<String, Object> params) {
		DispatcherDTO<Map> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setEventType(eventEnum.getCode());
		dispatcherDTO.setData(params);
		return remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}
}
