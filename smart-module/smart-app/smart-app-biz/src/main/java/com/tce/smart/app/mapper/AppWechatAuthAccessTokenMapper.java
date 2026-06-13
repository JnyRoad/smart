package com.tce.smart.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppWechatAuthAccessToken;

/**
 * 微信公招号授权令牌
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@Mapper
public interface AppWechatAuthAccessTokenMapper extends BaseMapper<AppWechatAuthAccessToken> {

}
