package com.tce.smart.common.security.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.common.core.constant.AuthConstants;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.util.WebUtils;
import com.tce.smart.common.security.exception.NotStrongPasswordException;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 用户详细信息
 */
@Slf4j
@Service
//@AllArgsConstructor
public class SmartUserDetailsServiceImpl implements SmartUserDetailsService {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Resource
    private RemoteUserService remoteUserService;
    @Resource
    private CacheManager cacheManager;
    @Resource
    private RemoteDictService remoteDictService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 用户密码登录
     *
     * @param username 用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    //@SneakyThrows
    public UserDetails loadUserByUsername(String username) {
        log.info("{}开始登录", username);

        // 检测账号失败次数是否达到最大次数
        String redisLockKey = AuthConstants.REDIS_KEY_PREFIX + username;
        Object codeObj = redisTemplate.opsForValue().get(redisLockKey);
        log.info("检测账号登录限制key: {} value: {}" + redisLockKey, codeObj);
        if (codeObj != null && Integer.valueOf(codeObj.toString()) >= AuthConstants.MAX_LOGIN_ATTEMPTS) {
            throw new TCEException("账号连续失败5次,锁定账号10分钟");
        }

        Cache cache = cacheManager.getCache("user_details");
        if (cache != null && cache.get(username) != null) {
            SmartUser userDetails = (SmartUser) cache.get(username).get();
            return userDetails;
        }

		Boolean isCheckOk = false;
		Boolean isSysUser = false;
		String errorMsg = "server error,login fail";

		//判断登录账号是否为平台账号
		Result<List<SysDict>> sysUserRs = remoteDictService.findByType(SecurityConstants.SYS_DEFAULT_USER,
				SecurityConstants.FROM_IN);
		if (sysUserRs.isSuccess() && CollectionUtil.isNotEmpty(sysUserRs.getData())) {
			for (SysDict tempSysDict : sysUserRs.getData()) {
				if (String.valueOf(username).equals(tempSysDict.getValue())) {
					isSysUser = true;
					isCheckOk = true;
					break;
				}
			}
		}

		//如果是后台账号 还需要在这里判断密码是否正确
		if(isCheckOk){
			isCheckOk = false;	//先置为false
			Result<UserInfo> userInfo = remoteUserService.info(username, SecurityConstants.FROM_IN);
			if(userInfo.isSuccess()){
				Map<String, String> paramMap = HttpUtil.decodeParamMap(WebUtils.getRequest().getQueryString(), CharsetUtil.UTF_8);
				String password = paramMap.get(SecurityConstants.PASSWORD);

				if(ENCODER.matches(password,userInfo.getData().getSysUser().getPassword())){
					//如果密码能匹配 则继续登录流程
					isCheckOk = true;
				} else {
					Result<UserInfo> result = remoteUserService.info(username, SecurityConstants.FROM_IN);
					if (!result.isSuccess() || Objects.isNull(result.getData())) {
						throw new TCEException("登陆失败,用户信息为空");
					}
					return getUserDetails(result,false);
				}
			} else {
				throw new TCEException("登陆失败," + userInfo.getMsg());
			}
		}

		if(!isSysUser) {
			Result<?> rs = simpleLogin(username);
			isCheckOk = rs.isSuccess();
			errorMsg = rs.getMsg();
		}

		UserDetails userDetails;
		if (isCheckOk) {
			Result<UserInfo> result = remoteUserService.info(username, SecurityConstants.FROM_IN);
			if (!result.isSuccess() || Objects.isNull(result.getData())) {
				throw new TCEException("登陆失败,用户信息为空");
			}

			//在这里判断用户的密码是否为强密码
			//这里可以判断表SYS_DICT中type=sys_default_user的用户和其他用户
			//是否强密码
			boolean isStrongPwd = SecurityUtils.isStrongPwd(username,WebUtils.getRequest());
			userDetails = getUserDetails(result,isStrongPwd);

			//设置缓存
			if(isStrongPwd){
				Objects.requireNonNull(cache).put(username, userDetails);
			} else {
				JSONObject object = new JSONObject();
				String key = "isStrongPwd";
				object.put(key,false);
				throw new NotStrongPasswordException(object.toString());
			}
		} else {
			throw new TCEException(errorMsg);
		}
		return userDetails;
	}

	/**
	 * 根据社交登录code 登录
	 *
	 * @param inStr TYPE@CODE
	 * @return UserDetails
	 * @throws UsernameNotFoundException
	 */
	@Override
	//@SneakyThrows
	public UserDetails loadUserBySocial(String inStr) {
		return getUserDetails(remoteUserService.social(inStr, SecurityConstants.FROM_IN),true);
	}

	/**
	 * 根据人脸登录
	 *
	 * @param userName
	 * @return UserDetails
	 * @throws UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserByFace(String userName) {
		UserDetails userDetails = getUserDetails(remoteUserService.info(userName, SecurityConstants.FROM_IN), true);
		Cache cache = cacheManager.getCache("user_details");
		Objects.requireNonNull(cache).put(userDetails.getUsername(), userDetails);
		return userDetails;
	}

	@Override
	public UserDetails loadUserByMobile(String mobile){
		UserDetails userDetails = getUserDetails(remoteUserService.queryByMobile(mobile, SecurityConstants.FROM_IN), true);
		Cache cache = cacheManager.getCache("user_details");
		Objects.requireNonNull(cache).put(userDetails.getUsername(), userDetails);
		return userDetails;
	}

	@Override
	public UserDetails loadUserByBadge(String badge) {
		Result<Boolean> result = remoteUserService.socialLogin(badge, SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
		Result<UserInfo> userRes = remoteUserService.info(badge, SecurityConstants.FROM_IN);
		if (!userRes.isSuccess() || Objects.isNull(userRes.getData())) {
			throw new TCEException("登陆失败,用户信息为空");
		}
		UserDetails userDetails = getUserDetails(userRes, true);
		//设置缓存
		Cache cache = cacheManager.getCache("user_details");
		Objects.requireNonNull(cache).put(userDetails.getUsername(), userDetails);
		return userDetails;
	}

	/**
	 * 构建userdetails
	 *
	 * @param result 用户信息
	 * @return
	 */
	private UserDetails getUserDetails(Result<UserInfo> result,boolean isStrongPwd) {
		if (result == null || result.getData() == null) {
			throw new UsernameNotFoundException("用户不存在");
		}

		UserInfo info = result.getData();
		Set<String> dbAuthsSet = new HashSet<>();
		if (ArrayUtil.isNotEmpty(info.getRoles())) {
			// 获取角色
			Arrays.stream(info.getRoles()).forEach(roleId -> dbAuthsSet.add(SecurityConstants.ROLE + roleId));
			// 获取资源
			dbAuthsSet.addAll(Arrays.asList(info.getPermissions()));

		}
		Collection<? extends GrantedAuthority> authorities
				= AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
		SysUser user = info.getSysUser();
		boolean enabled = StrUtil.equals(user.getLockFlag(), CommonConstants.STATUS_NORMAL);

		//查询用户园区配置
		List<Integer> parkIdList = new ArrayList<>();
		try{
			Result<List<Integer>> listUserParkRs = remoteUserService.listUserPark(user.getUserId(), SecurityConstants.FROM_IN);
			log.debug("remoteUserService.listUserPark=====params[{}]======rs===>{}",user.getUserId(),listUserParkRs);
			if(listUserParkRs.isSuccess() && Objects.nonNull(listUserParkRs.getData())){
				parkIdList.addAll(listUserParkRs.getData());
			}
		}catch (Exception e){
			log.error("查询用户园区配置异常",e);
		}

		// 构造security用户
		return new SmartUser(user.getUserId(), user.getDeptId(), user.getUsername(),parkIdList, SecurityConstants.BCRYPT + user.getPassword(), enabled,
				true, true, !CommonConstants.STATUS_LOCK.equals(user.getLockFlag()), authorities,isStrongPwd,info.getSalaryTypeName());
	}

	private Result simpleLogin(String username){
		Result rs = new Result();
		HttpServletRequest request = WebUtils.getRequest();
		String uri = request.getRequestURI();
		// 是登录请求，调用第三方登陆验证
		if (StringUtils.containsAnyIgnoreCase(uri, SecurityConstants.OAUTH_TOKEN_URL)) {
			Map<String, String> paramMap = HttpUtil.decodeParamMap(request.getQueryString(), CharsetUtil.UTF_8);
			String password = paramMap.get(SecurityConstants.PASSWORD);
			Result<Boolean> loginRs = remoteUserService.simpleLogin(username, password, SecurityConstants.FROM_IN);
// 			if(!loginRs.isSuccess() ||  Objects.isNull(loginRs.getData()) || !loginRs.getData()) {
// 				rs =JSONUtil.toBean(loginRs.getMsg(),Result.class);
// //				throw new com.tce.smart.common.core.exception.TCEException(-1,rs.getMsg());
// 			}else {
// 				rs = loginRs;
// 			}
			rs = loginRs;
		}

		return rs;
	}
}
