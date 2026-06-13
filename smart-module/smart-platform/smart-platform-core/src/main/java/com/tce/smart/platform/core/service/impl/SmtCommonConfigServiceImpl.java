package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigEditReqDTO;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.commonconfig.*;
import com.tce.smart.platform.core.entity.SmtCommonConfig;
import com.tce.smart.platform.core.mapper.SmtCommonConfigMapper;
import com.tce.smart.platform.core.service.SmtCommonConfigService;
import com.tce.smart.tool.enums.ConfigBusinessEnum;
import com.tce.smart.tool.enums.ConfigBusinessTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
@Service
public class SmtCommonConfigServiceImpl extends ServiceImpl<SmtCommonConfigMapper, SmtCommonConfig> implements SmtCommonConfigService {


	@Override
	public List<SmtCommonConfig> getList(CommonConfigQueryReqDTO queryDTO) {
		return this.list(Wrappers.<SmtCommonConfig>query().lambda()
				.in(Objects.nonNull(queryDTO.getBusinessTypes()), SmtCommonConfig::getBusinessType, queryDTO.getBusinessTypes())
				.eq(Objects.nonNull(queryDTO.getParkId()), SmtCommonConfig::getParkId, queryDTO.getParkId())
				.in(Objects.nonNull(queryDTO.getConfigTypes()), SmtCommonConfig::getConfigType, queryDTO.getConfigTypes())
				.orderByAsc(SmtCommonConfig::getConfigType));
	}

	@Override
	public SmtCommonConfig getByType(Integer businessType, Integer configType, Integer parkId) {
		return this.getOne(Wrappers.<SmtCommonConfig>query().lambda()
				.eq(SmtCommonConfig::getBusinessType, businessType)
				.eq(Objects.nonNull(parkId), SmtCommonConfig::getParkId, parkId)
				.eq(SmtCommonConfig::getConfigType, configType));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editConfig(CommonConfigEditReqDTO editReqDTO) {
		SmtCommonConfig config = BeanUtils.transform(SmtCommonConfig.class, editReqDTO);
		this.remove(Wrappers.<SmtCommonConfig>lambdaQuery()
				.eq(SmtCommonConfig::getBusinessType, editReqDTO.getBusinessType())
				.eq(Objects.nonNull(editReqDTO.getParkId()), SmtCommonConfig::getParkId, editReqDTO.getParkId())
				.eq(SmtCommonConfig::getConfigType, editReqDTO.getConfigType()));
		return this.saveOrUpdate(config);
	}

	@Override
	public Boolean batchEditConfig(List<CommonConfigEditReqDTO> editReqDTO) {
		if(CollUtil.isEmpty(editReqDTO)) {
			return Boolean.FALSE;
		}
		editReqDTO.forEach(edit -> {
			this.editConfig(edit);
		});
		return Boolean.TRUE;
	}

	@Override
	public ConfigVisitorApprovalDTO getVisitorApprove(Integer parkId) {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.VISITOR.getCode(),
				ConfigBusinessTypeEnum.VISITOR.getCode(), parkId);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigVisitorApprovalDTO.class);
	}

	@Override
	public ConfigVisitorNoticeDTO getVisitorNotice(Integer parkId) {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.VISITOR.getCode(),
				ConfigBusinessTypeEnum.NOTICE.getCode(), parkId);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigVisitorNoticeDTO.class);
	}

	@Override
	public ConfigVisitorNoticeDTO getAdmittanceNotice(Integer parkId) {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(),
				ConfigBusinessTypeEnum.NOTICE.getCode(), parkId);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigVisitorNoticeDTO.class);
	}

	@Override
	public ConfigVisitorHealthDTO getVisitorHealth(Integer parkId) {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.VISITOR.getCode(),
				ConfigBusinessTypeEnum.HEALTH_CODE.getCode(), parkId);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigVisitorHealthDTO.class);
	}

	@Override
	public ConfigSettlementLastDayDTO getLeaveSettlementApprove(Integer parkId) {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.LEAVE_SETTLEMENT.getCode(),
				ConfigBusinessTypeEnum.LEAVE_SETTLEMENT.getCode(), parkId);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigSettlementLastDayDTO.class);
	}

	@Override
	public ConfigSettlementLogDeleteDTO getSettlementDeleteDay() {
		SmtCommonConfig config =  this.getByType(ConfigBusinessEnum.LEAVE_SETTLEMENT.getCode(),
				ConfigBusinessTypeEnum.DELETE_DAYS.getCode(), null);
		if(Objects.isNull(config)) {
			return null;
		}
		return JSONUtil.parseObj(config.getValue()).toBean(ConfigSettlementLogDeleteDTO.class);
	}
}
