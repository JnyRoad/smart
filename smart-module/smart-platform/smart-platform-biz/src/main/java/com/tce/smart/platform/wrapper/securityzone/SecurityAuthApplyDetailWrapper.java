package com.tce.smart.platform.wrapper.securityzone;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.OaFlowRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.service.SmtOutDormitoryStaffService;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OaAreaRelationWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityAuthApplyDetailWrapper extends BaseWrapper<SmtSecurityAuthApply, SecurityAuthApplyDetailRespDTO> {

    @Autowired
    private SmtSecurityTaskDetailsService smtSecurityTaskDetailsService;
    @Autowired
    private SmtOutDormitoryStaffService smtOutDormitoryStaffService;
    @Autowired
    private SmtOaAreaTypeService smtOaAreaTypeService;
    @Autowired
    private SmtSecurityAreaService smtSecurityAreaService;

    @Override
    protected SecurityAuthApplyDetailRespDTO warp(SmtSecurityAuthApply bean) throws IOException {
        SecurityAuthApplyDetailRespDTO resp = BeanUtils.transform(SecurityAuthApplyDetailRespDTO.class, bean);
        Long applyId = bean.getId();
        List<Integer> typeList = ToolUtils.splitInt(bean.getAreaType());
        resp.setAreaType(typeList);
        resp.setAreaTypeName(smtSecurityAreaService.list(typeList).stream().map(SmtSecurityArea::getDesc).collect(Collectors.joining(SymbolConstants.COMMA)));
//		List<String> str = new ArrayList<>();
//		typeList.forEach(type -> {
//			str.add(SecurityOaAreaEnum.desc(type));
//		});
//		resp.setAreaTypeName(StringUtils.join(SymbolConstants.COMMA, str));
        resp.setDeviceStatusDesc(DeviceDownStatusEnum.desc(bean.getDeviceStatus()));
        resp.setSuccessNum(smtSecurityTaskDetailsService.getCount(bean.getId(), DeviceDownStatusEnum.SUCCESS.getCode()));
        resp.setFailNum(smtSecurityTaskDetailsService.getCount(applyId, DeviceDownStatusEnum.FAIL.getCode()));
        resp.setOaStatusDesc(ApproveListStateEnum.desc(bean.getOaStatus()));
        List<FlowVO> vos = new ArrayList<>();
        smtOutDormitoryStaffService.getOAProcessFlow(bean.getProcessId(), vos);
        List<OaFlowRespDTO> oaFlowResp = BeanUtils.batchTransform(OaFlowRespDTO.class, vos);
        resp.setOaFlow(oaFlowResp);
        return resp;
    }
}
