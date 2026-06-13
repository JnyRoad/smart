package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.platform.api.dto.req.SmtSecurityBuReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityBuRespDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtSecurityBu;
import com.tce.smart.platform.core.mapper.SmtSecurityBuMapper;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtSecurityBuService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fushiping
 * @date 2022/5/26 0026 11:32
 **/
@Service
@AllArgsConstructor
public class SmtSecurityBuServiceImpl extends ServiceImpl<SmtSecurityBuMapper, SmtSecurityBu> implements SmtSecurityBuService {

	private final SmtParkBuService smtParkBuService;

	private final SmtDeviceAuthorityService smtDeviceAuthorityService;

	private static final Logger log = LoggerFactory.getLogger(SmtSecurityBuServiceImpl.class);

	@Override
	public List<SmtSecurityBuRespDTO> getBuList(Integer parkId) {
		List<SmtSecurityBuRespDTO> respDTOS = new ArrayList<>();
		List<OvwYscompRespDTO> smtParkBus = smtParkBuService.getAllByParkId(parkId);
		if(CollUtil.isEmpty(smtParkBus)) {
			return respDTOS;
		}
		respDTOS = smtParkBus.stream().map(bus -> {
			SmtSecurityBuRespDTO resp = new SmtSecurityBuRespDTO();
			resp.setCompId(bus.getCompid().toString());
			resp.setCompName(bus.getCompAbbr());
			resp.setParkId(parkId);
			resp.setSecurityId(getRelationSecurity(bus.getCompid().toString(), parkId));
			return resp;
		}).collect(Collectors.toList());
		return respDTOS;
	}

	@Override
	public List<SmtSecurityBuRespDTO.SecurityList> getRelationSecurity(String buId, Integer parkId){
		List<SmtSecurityBu> smtSecurityBus = this.list(Wrappers.<SmtSecurityBu>lambdaQuery()
				.eq(SmtSecurityBu::getCompId, buId).eq(SmtSecurityBu::getParkId, parkId));
		if(CollUtil.isNotEmpty(smtSecurityBus)) {
			List<SmtSecurityBuRespDTO.SecurityList> securityLists = new ArrayList<>();
			smtSecurityBus.forEach(bu -> {
				SmtSecurityBuRespDTO.SecurityList securityList = new SmtSecurityBuRespDTO.SecurityList();
				SmtDeviceAuthority authority = smtDeviceAuthorityService.getById(bu.getSecurityId());
				securityList.setId(authority.getId());
				securityList.setName(authority.getAuthorityName());
				securityLists.add(securityList);
			});
			return securityLists;
		}
		return null;
	}

	@Override
	public List<Integer> getRelationSecuritys(String buId, List<Integer> parkIds){
		if(CollUtil.isEmpty(parkIds)) {
			log.warn("获取BU安全权限失败：园区ID列表为空, buId={}", buId);
			return null;
		}

		log.debug("查询BU安全权限：buId={}, parkIds={}", buId, parkIds);
		List<SmtSecurityBu> smtSecurityBus = this.list(Wrappers.<SmtSecurityBu>lambdaQuery()
				.eq(SmtSecurityBu::getCompId, buId).in(SmtSecurityBu::getParkId, parkIds));

		if(CollUtil.isNotEmpty(smtSecurityBus)) {
			List<Integer> securityIds = smtSecurityBus.stream().map(SmtSecurityBu::getSecurityId).collect(Collectors.toList());
			log.debug("找到BU安全权限：buId={}, securityIds={}", buId, securityIds);
			return securityIds;
		}

		log.warn("未找到BU安全权限配置：buId={}, parkIds={}", buId, parkIds);
		return null;
	}

	@Override
	public Boolean editRelation(List<SmtSecurityBuReqDTO> reqDTOS) {
		if(CollUtil.isEmpty(reqDTOS)) {
			return Boolean.FALSE;
		}
		this.remove(Wrappers.<SmtSecurityBu>lambdaQuery().eq(SmtSecurityBu::getParkId, reqDTOS.get(0).getParkId()));
		for (SmtSecurityBuReqDTO reqDTO : reqDTOS) {
			if(CollUtil.isEmpty(reqDTO.getSecurityId())) {
				continue;
			}
			reqDTO.getSecurityId().forEach(securityId -> {
				SmtSecurityBu bu = new SmtSecurityBu();
				bu.setCompId(reqDTO.getCompId());
				bu.setSecurityId(securityId);
				bu.setParkId(reqDTO.getParkId());
				this.save(bu);
			});
		}
		return Boolean.TRUE;
	}

}
