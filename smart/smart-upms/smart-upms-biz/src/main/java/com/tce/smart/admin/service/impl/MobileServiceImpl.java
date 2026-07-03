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
	 *
	 * <p>安全说明：验证码是敏感凭据，只写入 redis 并由短信网关下发，绝不回显到响应体或日志。
	 * 该接口在免鉴权白名单内（permitAll），一旦把验证码放进响应，任何人调用即可直接拿到码，
	 * 短信校验将形同虚设。</p>
	 *
	 * @param mobile mobile
	 * @return 是否已受理下发（不含验证码内容）
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

		// 限流读写必须使用同一 key，否则“发送过频繁”检查永远命中不到刚写入的值，可被无限刷码
		String codeKey = CommonConstants.DEFAULT_CODE_KEY + LoginTypeEnum.SMS.getType() + "@" + mobile;

		Object codeObj = redisTemplate.opsForValue().get(codeKey);
		if (codeObj != null) {
			// 安全：不打印 redis 中的验证码明文，避免日志泄露（CWE-532）
			log.info("手机号验证码未过期:{}", mobile);
			return new Result<>(Boolean.FALSE, "验证码发送过频繁");
		}

		String code = RandomUtil.randomNumbers(Integer.parseInt(SecurityConstants.CODE_SIZE));
		// 安全：只记录手机号，绝不打印验证码明文（CWE-532）
		log.debug("手机号生成验证码成功:{}", mobile);
		redisTemplate.opsForValue().set(codeKey, code, SecurityConstants.CODE_TIME, TimeUnit.SECONDS);

		// TODO: 调用短信网关下发验证码（当前仅写入 redis，尚未真正发送短信）
		// 安全：验证码只写 redis，不得回显给调用方
		return new Result<>(Boolean.TRUE, "验证码已发送");
	}
}
