package com.tce.smart.platform.wrapper.admittance;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceFellowRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceVehicleRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaFlowRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.*;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaTypeAuthService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.QRCodeUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class AdmittanceDetailtWrapper extends BaseWrapper<SmtAdmittanceApply, AdmittanceApplyDetailRespDTO> {

	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtAdmittanceVehicleService smtAdmittanceVehicleService;
	@Autowired
	private SmtOutDormitoryStaffService smtOutDormitoryStaffService;
	@Autowired
	private SmtOaAreaTypeService smtOaAreaTypeService;

    @Override
    protected AdmittanceApplyDetailRespDTO warp(SmtAdmittanceApply bean) throws IOException {
		AdmittanceApplyDetailRespDTO resp = BeanUtils.transform(AdmittanceApplyDetailRespDTO.class, bean);
		Long applyId = bean.getId();
		resp.setStartDays(DateUtils.format(bean.getStartTime()));
		resp.setStartTimes(DateUtils.convert("HH:mm", bean.getStartTime()));
		resp.setEndDays(DateUtils.format(bean.getEndTime()));
		resp.setEndTimes(DateUtils.convert("HH:mm", bean.getEndTime()));
		resp.setIsPhoto("否");
		resp.setStatusDesc(ApproveListStateEnum.desc(bean.getStatus()));
		resp.setVisitTypeDesc("普通来访");
		resp.setVisitCauseType("短期来访");
		resp.setVisitorPhone(bean.getVisitorPhone());
		if(AdmittanceTypeEnum.PERSON.getCode().equals(bean.getApplyType())) {
			resp.setCauseDesc(AdmittanceCauseEnum.desc(bean.getCause()));
		}
		if(AdmittanceTypeEnum.CAR.getCode().equals(bean.getApplyType())) {
			resp.setCauseDesc(AdmittanceCarCauseEnum.desc(bean.getCause()));
		}

		resp.setThingDesc(AdmittanceCarryItemsEnum.desc(bean.getThing()));
		resp.setPersonTypeDesc(AdmittancePersonTypeEnum.desc(bean.getPersonType()));
		List<Integer> typeList = ToolUtils.splitInt(bean.getAreaType());
		resp.setAreaType(typeList);
		SmtOaAreaType type = smtOaAreaTypeService.getByValue(bean.getPermitFactoryType(),
				OaSelectItemTypeEnum.ADMITTANCE_FACTORY_TYPE.getCode());
		if(Objects.nonNull(type)) {
			resp.setPermitFactoryTypeDesc(type.getTypeName());
		}

		if(StrUtil.isNotEmpty(bean.getReceptionistBadge())) {
			//查询被访人信息
			SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(bean.getReceptionistBadge());
			resp.setReceptionistDept(staff.getDepName());
			//获取随行人员信息
			resp.setFellowVisitorList(smtAdmittanceFellowService.getRespByApplyId(applyId));
		}
		//获取车辆信息
		if(SmtVisitorEnum.IS_VEHICLE.getType().equals(bean.getIsVehicle())) {
			resp.setVehicleList(smtAdmittanceVehicleService.getRespByApplyId(applyId));
		}
		resp.setDelFlag(0);
		if(LocalDateTime.now().isAfter(bean.getEndTime())) {
			resp.setDelFlag(2);
		}
		if(StrUtil.isNotEmpty(bean.getSmsCode()) && LocalDateTime.now().isBefore(bean.getEndTime())) {
			try {
				resp.setQrCode(QRCodeUtils.wordsCreateQRCode(bean.getSmsCode()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//获得oa审批流程
		List<FlowVO> vos = new ArrayList<>();
		smtOutDormitoryStaffService.getOAProcessFlow(bean.getProcessId(), vos);
		List<OaFlowRespDTO> oaFlowResp = BeanUtils.batchTransform(OaFlowRespDTO.class, vos);
		resp.setProcessList(oaFlowResp);
		return resp;
    }
}
