package com.tce.smart.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppIdentityCollect;

/**
 * 身份证信息采集表
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Mapper
public interface AppIdentityCollectMapper extends BaseMapper<AppIdentityCollect> {

}
