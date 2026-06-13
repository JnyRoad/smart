package com.tce.smart.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppIcbcConfig;

/**
 * 工商银行接入配置Mapper
 *
 * @author mkwu
 * @date 2019-08-23
 */
@Mapper
public interface AppIcbcConfigMapper extends BaseMapper<AppIcbcConfig> {

	List<AppIcbcConfig> getEnabledConfig();
}
