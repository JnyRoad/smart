package com.tce.smart.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppWechatAuthAccessToken;
import com.tce.smart.app.mapper.AppWechatAuthAccessTokenMapper;
import com.tce.smart.app.service.AppWechatAuthAccessTokenService;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信公众号授权令牌服务实现类
 *
 * @author mckaywu
 *
 */
@Service
@Slf4j
public class AppWechatAuthAccessTokenServiceImpl
		extends ServiceImpl<AppWechatAuthAccessTokenMapper, AppWechatAuthAccessToken>
		implements AppWechatAuthAccessTokenService {


}
