package com.tce.smart.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.mapper.SysUserMapper;
import com.tce.smart.admin.service.MobileService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.LoginTypeEnum;
import com.tce.smart.common.core.model.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 手机登录相关业务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileServiceImpl implements MobileService {
	private final RedisTemplate redisTemplate;
	private final SysUserMapper userMapper;


	/**
	 * 发送手机验证码
	 * TODO: 调用短信网关发送验证码,测试返回前端
	 *
	 * @param mobile mobile
	 * @return code
	 */
	@Override
	public Result<Boolean> sendSmsCode(String mobile) {
		List<SysUser> userList = userMapper.selectList(Wrappers
			.<SysUser>query().lambda()
			.eq(SysUser::getPhone, mobile));

		if (CollUtil.isEmpty(userList)) {
			log.info("手机号未注册:{}", mobile);
			return new Result<>(Boolean.FALSE, "手机号未注册");
		}

		Object codeObj = redisTemplate.opsForValue().get(CommonConstants.DEFAULT_CODE_KEY + mobile);

		if (codeObj != null) {
			log.info("手机号验证码未过期:{}，{}", mobile, codeObj);
			return new Result<>(Boolean.FALSE, "验证码发送过频繁");
		}

		String code = RandomUtil.randomNumbers(Integer.parseInt(SecurityConstants.CODE_SIZE));
		log.debug("手机号生成验证码成功:{},{}", mobile, code);
		redisTemplate.opsForValue().set(
			CommonConstants.DEFAULT_CODE_KEY + LoginTypeEnum.SMS.getType() + "@" + mobile
			, code, SecurityConstants.CODE_TIME, TimeUnit.SECONDS);
		return new Result<>(Boolean.TRUE, code);
	}
}
