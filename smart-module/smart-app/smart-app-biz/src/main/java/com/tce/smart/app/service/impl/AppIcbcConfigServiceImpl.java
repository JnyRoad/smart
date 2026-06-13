package com.tce.smart.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppIcbcConfig;
import com.tce.smart.app.mapper.AppIcbcConfigMapper;
import com.tce.smart.app.service.AppIcbcConfigService;

/**
 * 工商银行接入配置服务接口
 *
 * @author mkwu
 * @date 2019-08-23
 */
@Service
public class AppIcbcConfigServiceImpl extends ServiceImpl<AppIcbcConfigMapper, AppIcbcConfig>
		implements AppIcbcConfigService {

	@Override
	public List<AppIcbcConfig> getCurrentConfig() {
//		QueryWrapper<AppIcbcConfig> queryWrapper = new QueryWrapper<>();
//		queryWrapper.lambda()
//			.eq(AppIcbcConfig::getDeleteFlag, DeleteState.NORMOL.getCode())//未删除
//			.eq(AppIcbcConfig::getEnabled, EnabledState.ENABLED.getCode());//启用
//		AppIcbcConfig appIcbcConfig = this.getOne(queryWrapper);
		return this.baseMapper.getEnabledConfig();
	}

}
