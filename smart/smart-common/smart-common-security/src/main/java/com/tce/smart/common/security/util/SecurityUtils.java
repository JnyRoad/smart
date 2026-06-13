package com.tce.smart.common.security.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.service.SmartUser;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 安全工具类
 *
 */
@Slf4j
@UtilityClass
public class SecurityUtils {
	/**
	 * 获取Authentication
	 */
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 获取用户
	 *
	 * @param authentication
	 * @return SmartUser
	 * <p>
	 * 获取当前用户的全部信息 EnableSmartResourceServer true
	 * 获取当前用户的用户名 EnableSmartResourceServer false
	 */
	public SmartUser getUser(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof SmartUser) {
			return (SmartUser) principal;
		}
		return null;
	}

	/**
	 * 获取用户
	 */
	public SmartUser getUser() {
		Authentication authentication = getAuthentication();
		return getUser(authentication);
	}

	/**
	 * 获取用户角色信息
	 *
	 * @return 角色集合
	 */
	public List<Integer> getRoles() {
		Authentication authentication = getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		List<Integer> roleIds = new ArrayList<>();
		authorities.stream()
				.filter(granted -> StrUtil.startWith(granted.getAuthority(), SecurityConstants.ROLE))
				.forEach(granted -> {
					String id = StrUtil.removePrefix(granted.getAuthority(), SecurityConstants.ROLE);
					roleIds.add(Integer.parseInt(id));
				});
		return roleIds;
	}

	/**
	 * 判断密码是否为强密码
	 * @param request
	 * @return
	 */
	public static Boolean isStrongPwd(String username,HttpServletRequest request){
		String uri = request.getRequestURI();
		if (StringUtils.containsAnyIgnoreCase(uri, SecurityConstants.OAUTH_TOKEN_URL)) {
			Map<String, String> paramMap = HttpUtil.decodeParamMap(request.getQueryString(), CharsetUtil.UTF_8);
			String password = paramMap.get(SecurityConstants.PASSWORD);
			if(!checkPassword(password)){
				log.info("账号{}密码为非强密码",username);
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查密码是否是强密码
	 * 判断规则:
	 * 	1、密码不少于8位
	 *
	 * 2、包含数字、大写字母、小写字母
	 *
	 * 3、数字及字母接连或重复不超过3个
	 *
	 * @param password
	 * @return
	 */
	public static Boolean checkPassword(String password){
		if(password.length() < 8){
			//长度小于8位
			return false;
		} else if(!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$")){
			//没有包含数字、大写字母、小写字母 3种字符
			return false;
		}
		boolean isStrong = true;
		for(int i = 0;i < password.toCharArray().length - 2;i++){
			char c1 = password.charAt(i);
			char c2 = password.charAt(i+1);
			char c3 = password.charAt(i+2);
			//先判断c1是否为数字或字母
			if(!Character.isDigit(c1) && !Character.isLowerCase(c1) && !Character.isUpperCase(c2)){
				//非数字和字母
				continue;
			}
			if(c1 == c2 && c1 == c3){
				//三个相同的字符
				isStrong = false;
				break;
			} else if((c2 - c1) == 1 && (c3 - c2) == 1){
				//三个连续的字符
				isStrong = false;
				break;
			}
		}
		return isStrong;
	}

}
