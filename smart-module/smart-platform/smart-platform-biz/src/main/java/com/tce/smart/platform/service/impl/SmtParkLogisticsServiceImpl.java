package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtParkLogistics;
import com.tce.smart.platform.core.mapper.SmtParkLogisticsMapper;
import com.tce.smart.platform.service.SmtParkLogisticsService;
import com.tce.smart.tool.exception.TCEException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
@Service
public class SmtParkLogisticsServiceImpl extends ServiceImpl<SmtParkLogisticsMapper, SmtParkLogistics> implements SmtParkLogisticsService {

	@Override
	public SmtParkLogistics getByParkId(Integer parkId) {
		return baseMapper.selectOne(Wrappers.<SmtParkLogistics>query().lambda().eq(SmtParkLogistics::getParkId, parkId));
	}

	@Override
	public Boolean removeByParkId(Integer parkId) {
		return this.remove(Wrappers.<SmtParkLogistics>query().lambda().eq(SmtParkLogistics::getParkId, parkId));
	}

	private SmtParkLogistics getCompanyById(String CompanyId) {
		return baseMapper.selectOne(Wrappers.<SmtParkLogistics>query().lambda().eq(SmtParkLogistics::getCompanyId, CompanyId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveParkLogistics(Integer parkId, String logisticId) {
		//判断物流中心是否已经被注册
		SmtParkLogistics reSmtParkLogistics = this.getCompanyById(logisticId);
		if(Objects.nonNull(reSmtParkLogistics)) {
			if(!reSmtParkLogistics.getParkId().equals(parkId)) {
				throw new TCEException("该物流中心已关联其他园区！");
			}
		}
		this.removeByParkId(parkId);
		SmtParkLogistics smtParkLogistics = new SmtParkLogistics();
		smtParkLogistics.setParkId(parkId);
		smtParkLogistics.setCompanyId(logisticId);
		smtParkLogistics.setCreateTime(LocalDateTime.now());
		return this.save(smtParkLogistics);
	}
}
