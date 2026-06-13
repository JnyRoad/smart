package com.tce.smart.platform.wrapper.admittance;


import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyCodeDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.admittance.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.*;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.QRCodeUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class AdmittanceCodeDetailWrapper extends BaseWrapper<SmtAdmittanceApply, AdmittanceApplyCodeDetailRespDTO> {

	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtAdmittanceVehicleService smtAdmittanceVehicleService;
	@Autowired
	private SmtAdmittanceAreaTypeAuthService smtAdmittanceAreaTypeAuthService;
	@Autowired
	private SmtOaAreaTypeService smtOaAreaTypeService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;


	@Override
	protected AdmittanceApplyCodeDetailRespDTO warp(SmtAdmittanceApply bean) throws IOException {
		AdmittanceApplyCodeDetailRespDTO resp = BeanUtils.transform(AdmittanceApplyCodeDetailRespDTO.class, bean);
		Long applyId = bean.getId();
		resp.setStartTime(DateUtils.convert(bean.getStartTime()));
		resp.setEndTime(DateUtils.convert(bean.getEndTime()));
		if(AdmittanceTypeEnum.PERSON.getCode().equals(bean.getApplyType())) {
			resp.setCauseDesc(AdmittanceCauseEnum.desc(bean.getCause()));
		}
		if(AdmittanceTypeEnum.CAR.getCode().equals(bean.getApplyType())) {
			resp.setCauseDesc(AdmittanceCarCauseEnum.desc(bean.getCause()));
		}
		resp.setVisitorPhotoIdUrl(imageService.buildImageUrl(bean.getVisitorPhotoId()));
		resp.setThingDesc(AdmittanceCarryItemsEnum.desc(bean.getThing()));
		SmtPark park = smtParkService.getById(bean.getParkId());
		List<SmtAdmittanceAreaTypeAuth> authRelation = smtAdmittanceAreaTypeAuthService.getAuthByType(bean.getAreaType(), null, bean.getParkId());
//		String authRelations = StrUtil.join(SymbolConstants.BRANCH, authRelation.stream().map(SmtAdmittanceAreaTypeAuth::getAuthName).collect(Collectors.toList()));
		resp.setParkName(park.getParkName());
//		resp.setPermitAreaDesc(authRelations);
		resp.setIsPhoto("否");
		resp.setStatusDesc(ApproveListStateEnum.desc(bean.getStatus()));
		resp.setVisitTypeDesc("普通来访");
		resp.setVisitCauseType("短期来访");
		//resp.setRemotePath(smtAdmittanceApplyService.getRemoteUrl());
		try {
			resp.setQrCode(QRCodeUtils.wordsCreateQRCode(bean.getSmsCode()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setPersonTypeDesc(AdmittancePersonTypeEnum.desc(bean.getPersonType()));
		//获得申请进入区域
//		resp.setPermitAreaDesc(bean.getPermitArea());
//		resp.setPermitAreaTypeDesc(smtAdmittanceAreaTypeAuthService.getAuthNameByAreaId(bean.getParkId(), bean.getAreaType()));
		List<Integer> typeList = ToolUtils.splitInt(bean.getAreaType());
		resp.setAreaType(typeList);
		SmtOaAreaType type = smtOaAreaTypeService.getByValue(bean.getPermitFactoryType(),
				OaSelectItemTypeEnum.ADMITTANCE_FACTORY_TYPE.getCode());
		if(Objects.nonNull(type)) {
			resp.setPermitFactoryTypeDesc(type.getTypeName());
		}
		if(StrUtil.isNotEmpty(bean.getReceptionistBadge())) {
			//获取随行人员信息
			resp.setFellowVisitorList(smtAdmittanceFellowService.getRespByApplyId(applyId));
			//查询被访人信息
			SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(bean.getReceptionistBadge());
			resp.setReceptionistDept(staff.getDepName());
		}

		//code二维码信息
		resp.setDelFlag(0);
		if(LocalDateTime.now().isAfter(bean.getEndTime())) {
			resp.setDelFlag(2);
		}
		//获取车辆信息
		if (SmtVisitorEnum.IS_VEHICLE.getType().equals(bean.getIsVehicle())) {
			resp.setVehicleList(smtAdmittanceVehicleService.getRespByApplyId(applyId));
		}
		return resp;
	}
}
