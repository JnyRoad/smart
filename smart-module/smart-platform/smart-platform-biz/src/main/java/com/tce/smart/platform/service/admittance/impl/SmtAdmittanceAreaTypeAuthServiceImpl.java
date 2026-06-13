package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceAuthEditReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceAreaTypeAuth;
import com.tce.smart.platform.core.mapper.SmtAdmittanceAreaTypeAuthMapper;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaTypeAuthService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.AdmittanceOaAreaEnum;
import com.tce.smart.tool.util.ToolUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
@Service
public class SmtAdmittanceAreaTypeAuthServiceImpl extends ServiceImpl<SmtAdmittanceAreaTypeAuthMapper, SmtAdmittanceAreaTypeAuth> implements SmtAdmittanceAreaTypeAuthService {


	@Override
	public List<SmtAdmittanceAreaTypeAuth> getAuthByType(String areaTypeId, Integer authType, Integer parkId) {
		if (StrUtil.isEmpty(areaTypeId)) {
			return null;
		}
		List<Integer> ids = ToolUtils.splitInt(areaTypeId);
		return this.list(Wrappers.<SmtAdmittanceAreaTypeAuth>query().lambda().eq(SmtAdmittanceAreaTypeAuth::getParkId, parkId)
				.in(SmtAdmittanceAreaTypeAuth::getAreaTypeId, ids)
				.eq(Objects.nonNull(authType), SmtAdmittanceAreaTypeAuth::getAuthType, authType));
	}

	@Override
	public Boolean editAuth(List<AdmittanceAuthEditReqDTO> reqDTOs) {
		if (CollUtil.isEmpty(reqDTOs)) {
			return Boolean.FALSE;
		}
		//删除
		this.remove(Wrappers.<SmtAdmittanceAreaTypeAuth>query().lambda()
				.eq(SmtAdmittanceAreaTypeAuth::getParkId, reqDTOs.get(0).getParkId()));
		//新增
		for (AdmittanceAuthEditReqDTO reqDTO : reqDTOs) {
			if (CollUtil.isEmpty(reqDTO.getAuthLists())) {
				continue;
			}
			List<SmtAdmittanceAreaTypeAuth> authList = reqDTO.getAuthLists().stream().map(auth -> {
				SmtAdmittanceAreaTypeAuth typeAuth = SmtAdmittanceAreaTypeAuth.builder()
						.authId(auth.getAuthId()).authName(auth.getAuthName()).parkId(reqDTO.getParkId())
						.authType(auth.getAuthType()).areaTypeId(reqDTO.getAreaTypeId()).build();
				return typeAuth;
			}).collect(Collectors.toList());
			this.saveBatch(authList);
		}
		return Boolean.TRUE;
	}

	@Override
	public List<AdmittanceAuthRespDTO> getList(Integer parkId) {
		//获得所有OA区域类型
		List<Map<String, Object>> areaType = AdmittanceOaAreaEnum.getTypeList();
		List<AdmittanceAuthRespDTO> respDTOS = new ArrayList<>();
		for (Map<String, Object> map : areaType) {
			Integer typeId = Integer.parseInt(map.get("code").toString());
			AdmittanceAuthRespDTO resp = new AdmittanceAuthRespDTO();
			resp.setTypeName(map.get("desc").toString());
			resp.setParkId(parkId);
			resp.setAreaTypeId(typeId);
			resp.setFactoryType((Integer) map.get("factoryDesc"));
			//获得区域类型相关联权限
			List<SmtAdmittanceAreaTypeAuth> authList = this.list(Wrappers.<SmtAdmittanceAreaTypeAuth>query().lambda()
					.eq(SmtAdmittanceAreaTypeAuth::getParkId, parkId).eq(SmtAdmittanceAreaTypeAuth::getAreaTypeId, typeId));
			if (CollUtil.isNotEmpty(authList)) {
				List<AdmittanceAuthRespDTO.AuthList> authLists = authList.stream().map(auth -> {
					AdmittanceAuthRespDTO.AuthList authReq = new AdmittanceAuthRespDTO.AuthList();
					authReq.setAuthId(auth.getAuthId());
					authReq.setAuthName(auth.getAuthName());
					authReq.setAuthType(auth.getAuthType());
					return authReq;
				}).collect(Collectors.toList());
				resp.setAuthLists(authLists);
			}
			respDTOS.add(resp);
		}
		return respDTOS;
	}

	@Override
	public String getAuthNameByAreaId(Integer parkId, String areaId) {
		List<SmtAdmittanceAreaTypeAuth> authList = this.getAuthByType(areaId, null, parkId);
		if (CollUtil.isNotEmpty(authList)) {
			List<String> authName = authList.stream().map(SmtAdmittanceAreaTypeAuth::getAuthName).collect(Collectors.toList());
			return StringUtils.join(authName, SymbolConstants.BRANCH);
		}
		return null;
	}
}
