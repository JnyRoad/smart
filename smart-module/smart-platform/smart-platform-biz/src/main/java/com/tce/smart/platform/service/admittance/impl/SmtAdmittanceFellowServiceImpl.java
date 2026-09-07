package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceFellowReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceFellowRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.admittance.AdmittanceRecordQrCode;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.tool.enums.AdmittancePersonCertTypeEnum;
import com.tce.smart.tool.util.ToolUtils;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 入厂申请预约随行人员表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:13
 */
@Service
public class SmtAdmittanceFellowServiceImpl extends ServiceImpl<SmtAdmittanceFellowMapper, SmtAdmittanceFellow> implements SmtAdmittanceFellowService {

	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtImageService smtImageService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveFellow(List<AdmittanceFellowReqDTO> fellowReq, Long applyId) {
		if (CollUtil.isEmpty(fellowReq)) {
			throw new SmartException("来访人员为空");
		}
		fellowReq.forEach(fellow -> {
			SmtAdmittanceFellow smtAdmittanceFellow = BeanUtils.transform(SmtAdmittanceFellow.class, fellow);
			smtAdmittanceFellow.setVisitorId(applyId);
			this.save(smtAdmittanceFellow);
		});
		return Boolean.TRUE;
	}

	@Override
	public List<SmtAdmittanceFellow> getByApplyId(Long applyId) {
		return this.list(Wrappers.<SmtAdmittanceFellow>query().lambda().eq(SmtAdmittanceFellow::getVisitorId, applyId));
	}

	@Override
	public Boolean isExistFellow(Long personId) {
		SmtAdmittanceFellow fellow = this.getById(personId);
		return Objects.isNull(fellow) ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public List<AdmittanceFellowRespDTO> getRespByApplyId(Long applyId) {
		List<SmtAdmittanceFellow> fellowList = this.getByApplyId(applyId);
		List<AdmittanceFellowRespDTO> fellowRespDTOS = new ArrayList<>();
		fellowList.forEach(fellow -> {
			AdmittanceFellowRespDTO fellowRespDTO = BeanUtils.transform(AdmittanceFellowRespDTO.class, fellow);
			fellowRespDTO.setRecordQrCode(AdmittanceRecordQrCode.create(fellow.getId()));
			if(StringUtils.isNotEmpty(fellow.getCertNo())) {
				fellowRespDTO.setGender(ToolUtils.getGenderByIdCard(fellow.getCertNo()).getDesc());
			}
			if (StringUtils.isNotEmpty(fellow.getFellowPhotoId())) {
				fellowRespDTO.setFellowPhotoIdUrl(imageService.buildImageUrl(fellow.getFellowPhotoId()));
				fellowRespDTO.setFellowPhotoByte(smtImageService.getImageBinaryByCode(fellow.getFellowPhotoId()));
			}
			fellowRespDTO.setCertTypeDesc(AdmittancePersonCertTypeEnum.desc(fellow.getCertType()));
			fellowRespDTOS.add(fellowRespDTO);
		});
		return fellowRespDTOS;
	}
}
