package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtOrganizeRelationMapper;
import com.tce.smart.platform.core.mapper.SmtParkBuMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.schedule.service.platform.SmtStaffTaskService;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.util.HuaweiOBSUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sunfujian
 * @since 2021/9/9 20:19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtStaffTaskServiceImpl extends ServiceImpl<SmtStaffMapper, SmtStaff> implements SmtStaffTaskService {

	private static final int DEFAULT_STAFF_PHOTO_LOOKBACK_DAYS = 5;

	private static final ZoneOffset STAFF_PHOTO_TIME_ZONE = ZoneOffset.of("+8");

	private final SmtImageService smtImageService;

	private final RemoteStaffService remoteStaffService;

	private final SmtParkBuMapper smtParkBuService;

	private final SmtOrganizeRelationMapper smtOrganizeRelationService;

	@Value("${smart.image.staff-photo-url:}")
	private String syncStaffPhotoUrl;

	@Value("${smart.image.staff-photo-lookback-days:5}")
	private Integer syncStaffPhotoLookbackDays;

	@Value("${smart.xc-park-id}")
	private Integer xcParkId;

	@Override
	public void syncXCStaffPhoto() {
		if (StrUtil.isBlank(syncStaffPhotoUrl)) {
			throw new SmartException("未配置员工图片同步服务地址");
		}
		int lookbackDays = getSyncStaffPhotoLookbackDays();
		LocalDateTime endDateTime = currentTime();
		LocalDateTime startDateTime = endDateTime.minusDays(lookbackDays);
		long startTime = toStaffPhotoTimestamp(startDateTime);
		long endTime = toStaffPhotoTimestamp(endDateTime);
		String urlTemp = syncStaffPhotoUrl + "/?s=App.Person_Face.GetByTimeRange&pageNo=%s&pageSize=%s&startTime=%s&endTime=%s";
		Integer pageNo = 1;
		Integer pageSize = 50;

		for (;;pageNo++) {
			log.info("请求图库人脸数据参数, pageNo:{},startTime:{},endTime:{},lookbackDays:{}",
					pageNo, startTime, endTime, lookbackDays);
			String url = String.format(urlTemp, pageNo, pageSize, startTime, endTime);
			String data = postStaffPhotoUrl(url);
			log.info("请求图库人脸数据参数-urlTemp={}", url);
			JSONObject respObj = JSONUtil.parseObj(data);
			log.info("请求图库人脸数据结果, code:{}", respObj.getInt("ret"));
			if (HttpStatus.HTTP_OK != respObj.getInt("ret")) {
				log.info("同步许昌员工图片接口请求失败:{}", respObj.getStr("msg"));
				throw new SmartException("同步许昌员工图片接口请求失败");
			}
			JSONArray personArr = respObj.getJSONObject("data").getJSONArray("persons");
			if (personArr == null || personArr.isEmpty()) {
				log.info("同步许昌员工图片接口返回数据为空");
				break;
			}
			for (int i = 0; i < personArr.size(); i++) {
				String faceData = personArr.getJSONObject(i).getStr("faceData");
				if (StrUtil.isBlank(faceData)) {
					log.info("人脸数据为空");
					continue;
				}
				String empNo = personArr.getJSONObject(i).getStr("empNo");
				log.info("许昌人脸同步任务员工工号为：{}", empNo);
				if (StrUtil.isBlank(empNo)) {
					log.info("员工号数据为空");
					continue;
				}
				// 查询在职、实习、试用人员
				SmtStaff staff = getOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, empNo)
						.in(SmtStaff::getStatus,
								StaffStatusEnum.STAFF_STATUS_IN.getCode(),
								StaffStatusEnum.STAFF_STATUS_TTRY.getCode(),
								StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode(),
								StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode()), false);
				downFaceToDevice(staff, faceData);
			}
			Integer total = respObj.getJSONObject("data").getInt("total");
			log.info("请求图库人脸数据总数：{}", total);
			if (!hasNext(total, pageSize, pageNo)) {
				break;
			}
		}
	}

	private int getSyncStaffPhotoLookbackDays() {
		if (syncStaffPhotoLookbackDays == null || syncStaffPhotoLookbackDays <= 0) {
			return DEFAULT_STAFF_PHOTO_LOOKBACK_DAYS;
		}
		return syncStaffPhotoLookbackDays;
	}

	private long toStaffPhotoTimestamp(LocalDateTime dateTime) {
		return dateTime.toInstant(STAFF_PHOTO_TIME_ZONE).toEpochMilli();
	}

	protected LocalDateTime currentTime() {
		return LocalDateTime.now();
	}

	protected String postStaffPhotoUrl(String url) {
		return HttpUtil.post(url, "");
	}

	private boolean hasNext(Integer total, Integer pageSize, Integer pageNo) {
		if (total == null) {
			return false;
		}
		Integer totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		return pageNo < totalPage;
	}

	@Override
	public void syncStaffNoPhoto(Integer type) {
		LocalDateTime yesterday = LocalDateTime.now().plusDays(-1L);
		// 查询超过一天之后还未有图片的在职、实习、试用员工
		// 获取图片方式，1-图库接口，2-共享目录
		List<SmtParkBu> buList = smtParkBuService.selectList(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getParkId, xcParkId));
		List<String> bus = buList.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
		List<SmtOrganizeRelation> relations = smtOrganizeRelationService.selectList(Wrappers.<SmtOrganizeRelation>lambdaQuery().eq(SmtOrganizeRelation::getParkId, xcParkId));
		if (CollUtil.isNotEmpty(relations)) {
			List<String> relationBus = relations.stream().map(org -> org.getId().toString()).collect(Collectors.toList());
			bus.addAll(relationBus);
		}

		if (type == 1) {
			List<SmtStaff> noPhotoStaffList = list(Wrappers.<SmtStaff>lambdaQuery().in(SmtStaff::getStatus,
					StaffStatusEnum.STAFF_STATUS_IN.getCode(),
					StaffStatusEnum.STAFF_STATUS_TTRY.getCode(),
					StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode(),
					StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode())
					.isNull(SmtStaff::getFacePicId).lt(SmtStaff::getCreateTime, yesterday)
					.in(SmtStaff::getCompId, bus)
					.orderByDesc(SmtStaff::getCreateTime));
			for (SmtStaff staff : noPhotoStaffList) {
				log.info("同步许昌员工图片接口请求参数:{}", staff.getBadge());
				updateStaffPhotoByBadge(staff);
			}
		} else {
			List<SmtStaff> noPhotoStaffList = list(Wrappers.<SmtStaff>lambdaQuery().in(SmtStaff::getStatus,
					StaffStatusEnum.STAFF_STATUS_IN.getCode(),
					StaffStatusEnum.STAFF_STATUS_TTRY.getCode(),
					StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode(),
					StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode())
					.isNull(SmtStaff::getFacePicId).lt(SmtStaff::getCreateTime, yesterday)
					.notIn(SmtStaff::getCompId, bus)
					.orderByDesc(SmtStaff::getCreateTime));
			for (SmtStaff staff : noPhotoStaffList) {
				updateStaffPhotoFromShare(staff);
			}
		}
	}

	//人脸同步任务by 工号
	@Override
	public Boolean updateStaffPhotoXc(Integer badge){
		SmtStaff staff = this.getOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, badge.toString()));
		if (staff == null) {
			log.info("员工不存在");
			return Boolean.FALSE;
		}
		updateStaffPhotoByBadge(staff);
		return Boolean.TRUE;
	}

	private void updateStaffPhotoFromShare(SmtStaff staff) {
		if (StrUtil.isBlank(staff.getCertno())) {
			return;
		}
		//获取人脸图片内容
		// String dhrImage = ToolUtils.readRemoteImgToBase64(staff.getCertno());
		String dhrImage = HuaweiOBSUtil.readRemoteImgToBase64(staff.getCertno());
		if (StringUtils.isEmpty(dhrImage)) {
			log.info("从OBS获取员工[{} {} {}]图片失败", staff.getBadge(), staff.getName(), staff.getCertno());
			return;
		}
		downFaceToDevice(staff, dhrImage);
	}

	public void downFaceToDevice(SmtStaff staff, String faceData) {
		if (staff == null) {
			return;
		}
		Integer handleType = DeviceTaskActionEnum.DOWN.getCode();
		log.info("处理员工[{}]的图片信息,{}", staff.getBadge(), staff.getFacePicId());
		if (StringUtils.isNotEmpty(staff.getFacePicId())) {
			handleType = DeviceTaskActionEnum.UPDATE.getCode();
			//获取图片base64内容
			String imageBase64ByCode = smtImageService.getImageBase64ByCode(staff.getFacePicId());
			if (StrUtil.isNotBlank(imageBase64ByCode)) {
				String faceMd5 = SecureUtil.md5(imageBase64ByCode);
				String remoteMd5 = SecureUtil.md5(faceData);
				if (faceMd5.equals(remoteMd5)) {
					log.info("员工[{}]图片未变化", staff.getBadge());
					// 人脸图片和远程图库的图片一致,则不变动
					return;
				}
			} else {
				log.info("员工[{}]原始图片不存在", staff.getBadge());
			}
		}
		log.info("员工[{}]图片同步-dhr", staff.getBadge());
		// 保存新人脸图
		String facePicId = smtImageService.saveImage(0, faceData, SmtImageEnum.TYPE_STAFF_FACE.getCode());
		if (StringUtils.isBlank(facePicId)) {
			throw new TCEException("保存人脸图片异常");
		}
		// 更新数据库人脸图片ID
		SmtStaff updateStaff = new SmtStaff();
		updateStaff.setId(staff.getId());
		updateStaff.setFacePicId(facePicId);
		this.updateById(updateStaff);

		SmtStaffDTO remoteAddTask = new SmtStaffDTO();
		remoteAddTask.setId(staff.getId());
		remoteAddTask.setBadge(staff.getBadge());
		remoteAddTask.setName(staff.getName());
		remoteAddTask.setCompId(staff.getCompId());
		//更新人脸、身份证证照片信息
		remoteAddTask.setFacePicId(facePicId);
		//下发闸机
		log.info("员工图片同步-下发闸机：{}，下发方式：{}", remoteAddTask, handleType);
		remoteStaffService.addDeviceTask(remoteAddTask, handleType);
	}

	private void updateStaffPhotoByBadge(SmtStaff staff) {
		if (StrUtil.isBlank(syncStaffPhotoUrl)) {
			throw new SmartException("未配置员工图片同步服务地址");
		}
		try {
			if (StrUtil.isBlank(staff.getBadge())) {
				return;
			}
			String url = String.format("%s/?s=App.Person_Face.GetByIdCard&idCard=%s", syncStaffPhotoUrl, staff.getCertno());
			String post = HttpUtil.post(url, "");
			JSONObject respObj = JSONUtil.parseObj(post);
			if (HttpStatus.HTTP_OK != respObj.getInt("ret")) {
				log.info("获取员工[{}]图片失败:{}",staff.getBadge(), respObj.getStr("msg"));
				return;
			}
			JSONArray personArr = respObj.getJSONObject("data").getJSONArray("persons");
			if (personArr == null || personArr.isEmpty()) {
				log.info("获取员工[{}]图片数据返回为空",staff.getBadge());
				return;
			}
			String faceData = personArr.getJSONObject(0).getStr("faceData");
			if (StrUtil.isBlank(faceData)) {
				log.info("获取员工[{}]人脸图片数据为空",staff.getBadge());
				return;
			}
			downFaceToDevice(staff, faceData);
		} catch (Exception e) {
			log.error("同步员工[{}]图片异常: ", staff.getBadge(), e);
		}
	}

}
