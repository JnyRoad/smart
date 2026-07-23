package com.tce.smart.app.service.fore.impl;

import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.dto.fore.CheckPerfectCardDto;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.emun.EmpInfoCompState;
import com.tce.smart.app.entity.AppIdentityCollect;
import com.tce.smart.app.service.AppIdentityCollectService;
import com.tce.smart.app.service.IOcrService;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.app.service.fore.PerfectInfoService;
import com.tce.smart.app.vo.fore.CheckPerfectCardVo;
import com.tce.smart.app.vo.wechat.PerfectInfoVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.req.StaffPerfectReqDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 短信服务实现类
 *
 * @author mingkai.wu
 * @date 2019-05-09 14:44:56
 */
@Service
@Slf4j
public class PerfectInfoServiceImpl implements PerfectInfoService {

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private AppIdentityCollectService identityCollectService;

	@Autowired
	private IOcrService ocrService;

	@Autowired
	private DeviceManageService deviceManageService;

	@Autowired
	private RemoteStaffService remoteStaffService;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Override
	public Result<Boolean> checkPerfectFace() {
		Result<Boolean> result = remoteStaffService.checkPerfectInfo(SecurityUtils.getUser().getUsername());
		log.info("员工资料完整性校验完成 scene=check-perfect-info success={}", result.isSuccess());
		return result;
	}

	@Override
	public PerfectInfoVo readIdCardPhoto(PerfectInfoAo ocrAo) {
		// OCR识别身份证照片
		OcrIdCardDto ocrIdCardDto = ocrService.readIdCardFontImg(ocrAo.getIdentificationPhoto());
		if (Objects.isNull(ocrIdCardDto)
				|| StringUtils.isBlank(ocrIdCardDto.getName())
				|| StringUtils.isBlank(ocrIdCardDto.getIdentityCard())) {

			throw new TCEException("未能识别到身份证信息");
		}

		String badge = SecurityUtils.getUser().getUsername();

		ocrIdCardDto.setStaffId(badge);
		ocrIdCardDto.setIdCardFrontPhoto(ocrAo.getIdentificationPhoto());
		// 保存身份证信息
		Integer perfectId = identityCollectService.insertOrUpdate(ocrIdCardDto);

		PerfectInfoVo perfectInfoVo = new PerfectInfoVo();
		perfectInfoVo.setPerfectId(perfectId);// 信息采集id
		perfectInfoVo.setName(ocrIdCardDto.getName());
		perfectInfoVo.setIdentification(ocrIdCardDto.getIdentityCard());

		return perfectInfoVo;
	}

	@Override
	public CheckPerfectCardVo checkOcrInfo(CheckPerfectCardDto checkPerfectCardDto) {
		String badge = SecurityUtils.getUser().getUsername();
		// 查询员工信息
			Result<InternalStaffIdentityRespDTO> identityStaffResponse = remoteStaffInternalService.getIdentityStaff(badge,
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "ocr-compare");
		if (!identityStaffResponse.isSuccess() || Objects.isNull(identityStaffResponse.getData())) {
			throw new TCEException("查询员工信息失败");
		}

		InternalStaffIdentityRespDTO identityStaff = identityStaffResponse.getData();
		String certno = identityStaff.getCertno();
		String staffName = identityStaff.getName();
		// 检查姓名
		if (StringUtil.isNullOrEmpty(staffName) || !staffName.equals(checkPerfectCardDto.getName())) {
			throw new TCEException("姓名不匹配");
		}
		// 预留身份证比较
		if (StringUtil.isNullOrEmpty(certno) || !certno.equals(checkPerfectCardDto.getIdentityCard())) {
			throw new TCEException("身份证不匹配");
		}

		// 查询身份证采集信息
		try {
			if (Objects.isNull(identityCollectService.getById(checkPerfectCardDto.getPerfectId()))) {
				throw new TCEException("获取身份证识别信息异常");
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("获取身份证信息异常", e);
			throw new TCEException("获取身份证信息异常");
		}

		CheckPerfectCardVo checkPerfectCardVo = new CheckPerfectCardVo();
		checkPerfectCardVo.setPerfectId(checkPerfectCardDto.getPerfectId());

		return checkPerfectCardVo;
	}

	@Override
	public Boolean comparePhoto(PerfectInfoAo ocrAo) {
		if (Objects.isNull(ocrAo)
				|| Objects.isNull(ocrAo.getPerfectId())
				|| StringUtil.isNullOrEmpty(ocrAo.getFacePhoto())
				|| StringUtil.isNullOrEmpty(ocrAo.getDeviceNo())) {

			throw new TCEException("认证信息不全");
		}

		AppIdentityCollect appIdentityCollect = null;
		// 查询身份证采集信息
		try {
			appIdentityCollect = identityCollectService.getById(ocrAo.getPerfectId());
		} catch (Exception e) {
			log.error("获取身份证信息异常", e);
			throw new TCEException("获取身份证信息异常");
		}

		String facePhoto = ocrAo.getFacePhoto();

		Result<FaceFeaturesDTO> result = remoteAlgorithmService.getFaceFeatures(facePhoto, SecurityConstants.FROM_IN);

		log.info("人脸资料存储完成 scene=perfect-face success={}", result.isSuccess());
		if (result.isSuccess() && !StringUtil.isNullOrEmpty(result.getData().getFaceFeature())) {
			AppIdentityCollect updatePo = new AppIdentityCollect();
			updatePo.setId(appIdentityCollect.getId());
			updatePo.setFaceImage(ocrAo.getFacePhoto());
			updatePo.setCollectFlag(EmpInfoCompState.USED.getCode());
			identityCollectService.updateById(updatePo);

			// 更新人脸、身份证照片信息
			StaffPerfectReqDTO perfectDTO = new StaffPerfectReqDTO();
			perfectDTO.setBadge(appIdentityCollect.getStaffId());
			perfectDTO.setCertnoPic(appIdentityCollect.getFrontImage());
			perfectDTO.setFacePic(ocrAo.getFacePhoto());

			// 更新员工人脸信息
			Result<Boolean> perfectFaceRs = remoteStaffService.perfectFace(perfectDTO);
			if (!perfectFaceRs.isSuccess()) {
				throw new TCEException("完善人脸信息异常");
			}

			// 更新此设备未用户默认设备
			deviceManageService.bindDevice(SecurityUtils.getUser().getUsername(), ocrAo.getDeviceNo());
		} else {
			throw new TCEException("人脸检测失败，请重新拍照");
		}

		return true;
	}
}
