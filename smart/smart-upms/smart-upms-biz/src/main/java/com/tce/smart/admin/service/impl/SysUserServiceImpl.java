package com.tce.smart.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.dto.*;
import com.tce.smart.admin.api.entity.*;
import com.tce.smart.admin.api.feign.RemoteEvwEmphrYsService;
import com.tce.smart.admin.api.feign.RemoteParkService;
import com.tce.smart.admin.api.feign.RemoteStaffService;
import com.tce.smart.admin.api.vo.MenuVO;
import com.tce.smart.admin.api.vo.UserVO;
import com.tce.smart.admin.mapper.SysUserMapper;
import com.tce.smart.admin.service.*;
import com.tce.smart.common.core.constant.AuthConstants;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.LoginResult;
import com.tce.smart.common.core.model.NowUser;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.model.ResultData;
import com.tce.smart.common.core.util.*;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Autowired
    private CacheManager cacheManager;
	@Autowired
    private SysMenuService sysMenuService;
	@Autowired
    private SysRoleService sysRoleService;
	@Autowired
    private SysDeptService sysDeptService;
	@Autowired
    private SysUserRoleService sysUserRoleService;
	@Autowired
	private SysUserParkService sysUserParkService;
	@Autowired
    private SysDeptRelationService sysDeptRelationService;
    @Value("${spring.yuto-secsytem.login.sign-on}")
    private String loginUrl;
    @Value("${spring.yuto-secsytem.password.update-url}")
    private String updatePwdnUrl;
    @Value("${spring.yuto-secsytem.login.sign-token}")
    private String loginToken;
    @Value("${spring.yuto-secsytem.password.update-token}")
    private String updateToken;
	@Value("${env.test:true}")
	private boolean isTest;
	@Value("${security.default-reset-password:}")
	private String defaultResetPassword;
	@Value("${security.auth-code.encode-key:}")
	private String authCodeEncodeKey;
    @Autowired
	private StringRedisTemplate stringRedisTemplate;

    @Autowired
	private RemoteStaffService remoteStaffService;

	@Autowired
	private RemoteParkService remoteParkService;

    @Autowired
    private SysDictService sysDictService;

	@Autowired
    private RemoteEvwEmphrYsService remoteEvwEmphrYsService;

	/**
	 * 保存用户信息
	 *
	 * @param userDto DTO 对象
	 * @return success/fail
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveUser(UserDTO userDto) {
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDto, sysUser);
		sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
		sysUser.setPassword(ENCODER.encode(userDto.getPassword()));
		sysUser.setCreateTime(DateUtils.localDateTime());
		baseMapper.insert(sysUser);

		//保存用户角色信息
		boolean isSaveUserRoleSuccess = sysUserRoleService.saveUserRoleBatch(sysUser.getUserId(), userDto.getRole());
		// 保存用户园区信息
		boolean isSaveUserParkSuccess = sysUserParkService.saveUserParkBatch(sysUser.getUserId(), userDto.getPark());

		if (isSaveUserRoleSuccess && isSaveUserParkSuccess) {
			String logingName = SecurityUtils.getUser().getUsername();
			List<SysDict> sysUserRs = sysDictService.findByType(SecurityConstants.SYS_DEFAULT_USER);
			Boolean isSysUser = false;
			if (CollectionUtil.isNotEmpty(sysUserRs)) {
				for (SysDict tempSysDict : sysUserRs) {
					if (String.valueOf(logingName).equals(tempSysDict.getValue())) {
						isSysUser = true;
						break;
					}
				}
			}

			if (isSysUser) {
				sysDictService.saveDict(SecurityConstants.SYS_DEFAULT_USER, "自定义用户", sysUser.getUsername());
			}
		}
		return isSaveUserRoleSuccess;
	}

	/**
	 * 通过查用户的全部信息
	 *
	 * @param sysUser 用户
	 * @return
	 */
	@Override
	public UserInfo findUserInfo(SysUser sysUser) {
		UserInfo userInfo = new UserInfo();
		userInfo.setSysUser(sysUser);
		//设置角色列表  （ID）
		List<SysRole> sysRoles = sysRoleService.findRolesByUserId(sysUser.getUserId());
		if(CollectionUtils.isNotEmpty(sysRoles)) {
			List<RoleDTO> roleList = sysRoles.stream()
					.map(role -> {
								RoleDTO userRole = new RoleDTO();
								userRole.setRoleId(role.getRoleId());
								userRole.setRoleName(role.getRoleName());
								userRole.setRoleType(role.getDsType());
								return userRole;
							}
					).collect(Collectors.toList());
			List<Integer> roleIds = sysRoles
					.stream()
					.map(SysRole::getRoleId)
					.collect(Collectors.toList());
			userInfo.setRoles(ArrayUtil.toArray(roleIds, Integer.class));
			userInfo.setRoleList(roleList);
			//设置权限列表（menu.permission）
			Set<String> permissions = new HashSet<>();
			roleIds.forEach(roleId -> {
				List<String> permissionList = sysMenuService.findMenuByRoleId(roleId)
						.stream()
						.filter(menuVo -> StringUtils.isNotBlank(menuVo.getPermission()))
						.map(MenuVO::getPermission)
						.collect(Collectors.toList());
				permissions.addAll(permissionList);
			});
			userInfo.setPermissions(ArrayUtil.toArray(permissions, String.class));
		}

		//查询员工计薪类型
		Result<EvwEmphrYsRespDTO> dataResult = remoteEvwEmphrYsService.info(sysUser.getUsername(), SecurityConstants.FROM_IN);
		if(dataResult.isSuccess() && Objects.nonNull(dataResult.getData())){
			userInfo.setSalaryTypeName(dataResult.getData().getSalarytypeName());
		}
		return userInfo;
	}

	/**
	 * 分页查询用户信息（含有角色信息）
	 *
	 * @param page    分页对象
	 * @param userDTO 参数列表
	 * @return
	 */
	@Override
	public IPage getUsersWithRolePage(Page page, UserDTO userDTO) {
		IPage<UserVO> userRolePage;
		if(SecurityUtils.getUser().getUsername().equals(SecurityConstants.ADMIN)) {
			userRolePage  = baseMapper.getUserVosPage(page, userDTO, null);
		}else {
			userRolePage = baseMapper.getUserVosPage(page, userDTO, SecurityConstants.ADMIN);
		}
		if (Objects.nonNull(userRolePage) && CollectionUtils.isNotEmpty(userRolePage.getRecords())) {
			List<SysUserPark> sysUserParkList;
			//设置用户园区
			for (UserVO tempUserVO : userRolePage.getRecords()) {
				List<SmtParkDTO> tempSmtParkList = new ArrayList<>();
				sysUserParkList = sysUserParkService.getUserParkList(tempUserVO.getUserId());
				if (CollectionUtils.isNotEmpty(sysUserParkList)) {
					for (SysUserPark tmepSysUserPark : sysUserParkList) {
						Result<SmtParkDTO> getPakrResult = remoteParkService.getPakrById(tmepSysUserPark.getParkId(), SecurityConstants.FROM_IN);
						log.info("remoteParkService.getPakrById===params[{}]===rs==={}",tmepSysUserPark.getParkId(),getPakrResult);
						if (Objects.nonNull(getPakrResult) && Objects.nonNull(getPakrResult.getData())) {
							tempSmtParkList.add(getPakrResult.getData());
						}
					}
				}
				tempUserVO.setParkList(tempSmtParkList);
			}
		}


		return userRolePage;
	}

	/**
	 * 通过ID查询用户信息
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	@Override
	public UserVO selectUserVoById(Integer id) {
		return baseMapper.getUserVoById(id);
	}

	/**
	 * 删除用户
	 *
	 * @param sysUser 用户
	 * @return Boolean
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = "user_details", key = "#sysUser.username")
	public Result<Boolean> deleteUserById(SysUser sysUser) {
		if(Objects.isNull(sysUser)){
			log.warn("用户信息为空");
			return new Result<>(Boolean.FALSE, "用户信息为空");
		}
		if(sysUser.getUsername().equals(SecurityConstants.ADMIN)){
			log.warn("不能删除管理员帐号:{}", sysUser.getUsername());
			return new Result<>(Boolean.FALSE, "不能删除管理员帐号");
		}

		//删除用户角色
		sysUserRoleService.deleteByUserId(sysUser.getUserId());
		//删除用户园区
		sysUserParkService.deleteByUserId(sysUser.getUserId());
		//删除用户
		this.removeById(sysUser.getUserId());
		return new Result<>(Boolean.TRUE);
	}

	@Override
	@CacheEvict(value = "user_details", key = "#userDto.username")
	public Result<Boolean> updateUserInfo(UserDTO userDto) {
		UserVO userVO = baseMapper.getUserVoByUsername(userDto.getUsername());
		if(null == userVO){
			return new Result<>(false);
		}
		SysUser sysUser = new SysUser();
		if (StrUtil.isNotBlank(userDto.getPassword())
				&& StrUtil.isNotBlank(userDto.getNewpassword1())) {
			if (ENCODER.matches(userDto.getPassword(), userVO.getPassword())) {
				sysUser.setPassword(ENCODER.encode(userDto.getNewpassword1()));
			} else {
				log.warn("原密码错误，修改密码失败:{}", userDto.getUsername());
				return new Result<>(Boolean.FALSE, "原密码错误，修改失败");
			}

			if(!SecurityUtils.checkPassword(userDto.getNewpassword1())){
				//新密码为非强密码
				log.warn("新密码为非强密码，修改失败:{}",userDto.getUsername());
				return new Result<>(Boolean.FALSE, "新密码为非强密码，修改失败");
			}
		}
		sysUser.setPhone(userDto.getPhone());
		sysUser.setUserId(userVO.getUserId());
		sysUser.setAvatar(userDto.getAvatar());
		sysUser.setUpdateTime(DateUtils.localDateTime());
		return new Result<>(this.updateById(sysUser));
	}

	@Override
	public Boolean verifyMobile(String mobile) {
		List<SysUser> users = baseMapper.selectList(Wrappers.<SysUser>query()
				.lambda().eq(SysUser::getPhone, mobile).eq(SysUser::getDelFlag,0)
		);
		if (Objects.isNull(users) || users.size() < 1) {
			Result<List<SmtStaffDTO>> listResult = remoteStaffService.queryMobile(mobile, SecurityConstants.FROM_IN);
			if(listResult.isSuccess()){
				List<SmtStaffDTO> data = listResult.getData();
				if(data.size() > 1){
					throw new TCEException("验证码登录失败，此手机号码关联多个员工信息");
				}else if(data.size() == 1){
					SysUser sysUser = new SysUser();
					sysUser.setUsername(data.get(0).getBadge());
					String pwd = data.get(0).getCertno().substring(data.get(0).getCertno().length() - 6);
					sysUser.setPassword(ENCODER.encode(pwd));
					sysUser.setCreateTime(LocalDateTime.now());
					sysUser.setUpdateTime(LocalDateTime.now());
					sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
					sysUser.setLockFlag(CommonConstants.STATUS_NORMAL);
					sysUser.setPhone(mobile);
					this.baseMapper.insert(sysUser);
					// 添加默认员工角色
					SysUserRole userRole = new SysUserRole();
					userRole.setRoleId(SecurityConstants.ROLE_ORDINARY_ID);
					userRole.setUserId(sysUser.getUserId());
					sysUserRoleService.save(userRole);
				}else{
					throw new TCEException("手机号码未查找到员工信息");
				}
			}else{
				throw new TCEException("验证码登录失败");
			}
		}else if(users.size() > 1){
			throw new TCEException("验证码登录失败，此手机号码关联多个员工信息");
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = "user_details", key = "#userDto.username")
	public Boolean updateUser(UserDTO userDto) {
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDto, sysUser);
		sysUser.setUpdateTime(LocalDateTime.now());
		if (StrUtil.isNotBlank(userDto.getPassword())) {
			if(!SecurityUtils.checkPassword(userDto.getPassword())){
				//新密码为非强密码
				log.warn("新密码为非强密码，修改失败:{}",userDto.getUsername());
				throw new TCEException("密码为非强密码，修改失败");
			}
			sysUser.setPassword(ENCODER.encode(userDto.getPassword()));
		}
		this.updateById(sysUser);

		// 更新用户角色
		sysUserRoleService.updateUserRole(sysUser.getUserId(),userDto.getRole());

		// 更新用户园区
		sysUserParkService.updateUserPark(sysUser.getUserId(),userDto.getPark());

//		//更新用户信息缓存
//		Cache cache = cacheManager.getCache("user_details");
//		if (cache != null && cache.get(userDto.getUsername()) != null) {
//			SmartUser userDetails = (SmartUser) cache.get(userDto.getUsername()).get();
//			//查询用户园区配置
//			List<Integer> parkIdList = new ArrayList<>();
//			try{
//				Result<List<Integer>> listUserParkRs = remoteUserService.listUserPark(sysUser.getUserId(), SecurityConstants.FROM_IN);
//				log.debug("远程获取用户关联园区Id,userId={},响应={}",sysUser.getUserId(),listUserParkRs);
//				if(listUserParkRs.isSuccess() && Objects.nonNull(listUserParkRs.getData())){
//					parkIdList.addAll(listUserParkRs.getData());
//				}
//			}catch (Exception e){
//				log.error("查询用户园区配置异常",e);
//			}
//
//			userDetails.setParkIdList(parkIdList);
//			cache.put(userDto.getUsername(),userDetails);
//			log.info("更新用户信息:{}",userDto.getUsername());
//		}
		return Boolean.TRUE;
	}

	/**
	 * 查询上级部门的用户信息
	 *
	 * @param username 用户名
	 * @return Result
	 */
	@Override
	public List<SysUser> listAncestorUsers(String username) {
		SysUser sysUser = this.getOne(Wrappers.<SysUser>query().lambda()
				.eq(SysUser::getUsername, username));

		SysDept sysDept = sysDeptService.getById(sysUser.getDeptId());
		if (sysDept == null) {
			return null;
		}

		Integer parentId = sysDept.getParentId();
		return this.list(Wrappers.<SysUser>query().lambda()
				.eq(SysUser::getDeptId, parentId));
	}

	/**
	 * 获取当前用户的子部门信息
	 *
	 * @return 子部门列表
	 */
	private List<Integer> getChildDepts() {
		Integer deptId = SecurityUtils.getUser().getDeptId();
		//获取当前部门的子部门
		return sysDeptRelationService
				.list(Wrappers.<SysDeptRelation>query().lambda()
						.eq(SysDeptRelation::getAncestor, deptId))
				.stream()
				.map(SysDeptRelation::getDescendant)
				.collect(Collectors.toList());
	}

	@CacheEvict(value = "user_details", key = "#user.username")
	public Boolean reset(SysUser user){
		if (StrUtil.isBlank(defaultResetPassword)) {
			throw new TCEException("默认重置密码未配置");
		}
		user.setPassword(ENCODER.encode(defaultResetPassword));
		return updateById(user);
	}
	@Override
	public Boolean reset(Integer id){
		SysUser user = this.getOne(Wrappers.<SysUser>query().lambda()
				.eq(SysUser::getUserId, id));
		if (user == null) {
			return false;
		}
		return reset(user);
	}

	@CacheEvict(value = "user_details", key = "#user.username")
	public Boolean freeze(SysUser user) {
		user.setLockFlag(CommonConstants.STATUS_LOCK);
		return updateById(user);
	}

	@Override
	public Boolean freeze(Integer id) {
		SysUser user = this.getOne(Wrappers.<SysUser>query().lambda()
				.eq(SysUser::getUserId, id));
		if (user == null) {
			return false;
		}
		return freeze(user);
	}

	@Override
	public Boolean simpleLogin(String username, String password) {
		Boolean isSuccess = false;
		if (StringUtils.isNotBlank(loginUrl)) {
			try {
				Cache cache = cacheManager.getCache("user_details");
				if (cache != null && cache.get(username) != null) {
					return Boolean.TRUE;
				}
				//检测是否为临时人员，临时人员不同通过裕同接口检测
				Boolean isTemp = false;
				SmtStaffDTO queryStaffRs = this.checkStaff(username);
				if(queryStaffRs.getStatus() == 4) {
					isTemp = Boolean.TRUE;
				}
				Map<String, String> param = new HashMap<>();
				param.put("UserName", username);
				param.put("YPassWord", password);
				param.put("SysCode", "APP-EYUTO");
				param.put("TokenID",loginToken);
				String newUri = UriComponentsBuilder.fromHttpUrl(loginUrl).replaceQuery(HttpUtil.toParams(param))
						.build(true).toString();

				LoginResult result = null;
				//开发或测试时 关闭登陆裕同的流程
				if(!isTest && !isTemp){
					HttpResponse response = HttpUtils.createGet(newUri).execute();
					result = HttpUtils.parse(response, LoginResult.class);
				} else {
					//测试用
					result = new LoginResult();
					result.setType(1);
					result.setErrorcode(0);

					NowUser nowUser = new NowUser();
					nowUser.setMobile(queryStaffRs.getPhone());

					ResultData resultData = new ResultData();
					resultData.setNowUser(nowUser);
					result.setResultdata(JSONUtil.toJsonStr(resultData));
				}

				assert result != null;
				if (result.getType().equals(1) && result.getErrorcode().equals(0)) {

					ResultData resultData = JSONUtil.toBean(result.getResultdata(), ResultData.class);
					NowUser nowUser = resultData.getNowUser();
					SysUser sysUser = this.baseMapper
							.selectOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
					if (Objects.isNull(sysUser)) {
						sysUser = new SysUser();
						sysUser.setUsername(username);
						sysUser.setPassword(ENCODER.encode(password));
						if(queryStaffRs.getStatus().equals(4)) {
							String pwd = queryStaffRs.getCertno().substring(queryStaffRs.getCertno().length() - 6);
							if(!password.equals(pwd) ) {
								throw new TCEException("账号或密码错误");
							}
							sysUser.setPassword(ENCODER.encode(pwd));
						}
						sysUser.setCreateTime(LocalDateTime.now());
						sysUser.setUpdateTime(LocalDateTime.now());
						sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
						sysUser.setLockFlag(CommonConstants.STATUS_NORMAL);
						sysUser.setPhone(nowUser.getMobile());
						this.baseMapper.insert(sysUser);
						// 添加默认员工角色
						SysUserRole userRole = new SysUserRole();
						userRole.setRoleId(SecurityConstants.ROLE_ORDINARY_ID);
						userRole.setUserId(sysUser.getUserId());
						sysUserRoleService.save(userRole);

//						try {
//							Result<Boolean> synStaffFaceImageRs =  remoteStaffService.synStaffFaceImage(username);
//							log.info("remoteStaffService.synStaffFaceImage result=[{}],username={}", synStaffFaceImageRs,username);
//						}catch(Exception e) {
//							log.error("创建用户登录同步人脸图片到裕同C6失败,username={}",e,username);
//						}
					}
					//这里是为啥要这么写
					if(!isTest && !isTemp) {
						sysUser.setPassword(ENCODER.encode(password));
						this.baseMapper.updateById(sysUser);
					}

					try {
						// 初始化帐号App权限
						Result<Boolean> inintLoginAuthRs = remoteStaffService.inintLoginAuth(username);
						log.info("remoteStaffService.inintLoginAuth result=[{}],username={}", inintLoginAuthRs,username);

					}catch(Exception e) {
						log.error("初始化员工App权限异常",e);
					}

					return Boolean.TRUE;
				} else {
					throw new TCEException("YUTO认证系统登陆失败");
				}
			} catch (TCEException tce) {
				throw tce;
			} catch (Exception e) {
				log.error("登陆服务异常",e);
				throw new TCEException("登录服务异常");
			}
		}

		return isSuccess;
	}

	@Override
	public Boolean socialLogin(String username) {
		try {
			Cache cache = cacheManager.getCache("user_details");
			if (cache != null && cache.get(username) != null) {
				return Boolean.TRUE;
			}
			SmtStaffDTO queryStaffRs = this.checkStaff(username);
			SysUser sysUser = this.baseMapper
					.selectOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
			if (Objects.isNull(sysUser)) {
				sysUser = new SysUser();
				sysUser.setUsername(username);
				sysUser.setCreateTime(LocalDateTime.now());
				sysUser.setUpdateTime(LocalDateTime.now());
				sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
				sysUser.setLockFlag(CommonConstants.STATUS_NORMAL);
				String pwd = queryStaffRs.getCertno().substring(queryStaffRs.getCertno().length() - 6);
				sysUser.setPassword(ENCODER.encode(pwd));
				sysUser.setPhone(queryStaffRs.getPhone());
				this.baseMapper.insert(sysUser);
				// 添加默认员工角色
				SysUserRole userRole = new SysUserRole();
				userRole.setRoleId(SecurityConstants.ROLE_ORDINARY_ID);
				userRole.setUserId(sysUser.getUserId());
				sysUserRoleService.save(userRole);
			}
			try {
				// 初始化帐号App权限
				Result<Boolean> inintLoginAuthRs = remoteStaffService.inintLoginAuth(username);
				log.info("remoteStaffService.inintLoginAuth result=[{}],username={}", inintLoginAuthRs,username);
			} catch(Exception e) {
				log.error("初始化员工App权限异常",e);
			}
			return Boolean.TRUE;
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("登陆服务异常",e);
			throw new TCEException("登录服务异常");
		}
	}

	@CacheEvict(value = "user_details", key = "#username")
	@Override
	public Boolean updatePwd(String username,String password,String updateAuthCode){
		Boolean isSuccess = false;

		String redisKey = SecurityConstants.APP_PWD_UPDATE_AUTHCODE + username;
		log.info("updatePwd.fundAuthCode.key={}",redisKey);
		String authCodeObject = stringRedisTemplate.opsForValue().get(redisKey);
		if (StringUtils.isEmpty(authCodeObject)) {
			log.error("获取密码修改授权码异常");
			throw new TCEException("本次修改授权失败");
		}

		if(!SecurityUtils.checkPassword(password)){
			//新密码为非强密码
			log.warn("新密码为非强密码，修改失败");
			throw new TCEException("新密码为非强密码，修改失败");
		}

		//校验修改授权码
		JSONObject authCodeJson = (JSONObject) JSONUtil.parse(authCodeObject);
		String deUpdateAuthCode = null;
		try {
			deUpdateAuthCode = URLDecoder.decode(EncDecryUtils.decryptAES(updateAuthCode, authCodeEncodeKey));
		}catch(Exception e) {
			log.error("密码修改授权码解密异常",e);
			throw new TCEException("授权码解密异常");
		}

		if (!authCodeJson.getStr(SecurityConstants.PWD_UPDATE_AUTHCODE_SUB_KEY).equals(deUpdateAuthCode)) {
			throw new TCEException("修改密码失败，人脸识别过于频繁");
		}

		Boolean isTemp = false;
		//判断是否临时人员，临时人员不必通过裕同接口检测
		SmtStaffDTO queryStaffRs = this.checkStaff(username);
		if(queryStaffRs.getStatus() == 4) {
			isTemp = Boolean.TRUE;
		}
		if(!isTemp) {
			Map<String, String> param = new HashMap<>();
			param.put("UserName", username);
			param.put("YPassWord", password);
			param.put("TokenID", updateToken);
			String newUri = UriComponentsBuilder.fromHttpUrl(updatePwdnUrl)
					.replaceQuery(HttpUtil.toParams(param))
					.build(true)
					.toString();

			//发送裕同统一登录系统
			HttpResponse response = HttpUtils.createGet(newUri).execute();
			log.info("统一登录系统密码更新响应状态:{}", response.getStatus());
			LoginResult result = HttpUtils.parse(response, LoginResult.class);
			assert result != null;
			if (result.getType().equals(1) && result.getErrorcode().equals(0)) {
				isSuccess = this.updateSysUser(password, username);
			}
			return isSuccess;
		}

		//临时人员能够直接设置密码
		SysUser sysUser = this.baseMapper.selectOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
		if (Objects.isNull(sysUser)) {
			sysUser = new SysUser();
			sysUser.setUsername(username);
			sysUser.setPassword(ENCODER.encode(password));
			sysUser.setCreateTime(LocalDateTime.now());
			sysUser.setUpdateTime(LocalDateTime.now());
			sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
			sysUser.setLockFlag(CommonConstants.STATUS_NORMAL);
			sysUser.setPhone(queryStaffRs.getPhone());
			this.baseMapper.insert(sysUser);
			// 添加默认员工角色
			SysUserRole userRole = new SysUserRole();
			userRole.setRoleId(SecurityConstants.ROLE_ORDINARY_ID);
			userRole.setUserId(sysUser.getUserId());
			sysUserRoleService.save(userRole);

			return Boolean.TRUE;
		}else{
			return this.updateSysUser(password, username);
		}
	}

	/**
	 * 检测app登录用户员工信息是否存在
	 * @return SmtStaffDTO 员工信息
	 */
	private SmtStaffDTO checkStaff(String username) {
		Result<SmtStaffDTO> queryStaffRs = remoteStaffService.getSimpleSttaffByBadge(username);
		SmtStaffDTO staffInfo = queryStaffRs.getData();
		log.info("remoteStaffService.getSimpleSttaffByBadge.rs={}",queryStaffRs);
		if(!queryStaffRs.isSuccess() || Objects.isNull(staffInfo)){
			log.error("员工信息不存在");
			throw new TCEException("员工信息不存在");
		}
		else if(-1 == staffInfo.getStatus()
				||0 == staffInfo.getStatus()){
			log.error("员工状态异常");
			throw new TCEException("员工状态异常");
		}
		return staffInfo;
	}

	/**
	 * 修改系统用户表密码
	 * @param password
	 * @param username
	 * @return
	 */
	private Boolean updateSysUser(String password, String username) {
		SysUser sysUser = new SysUser();
		sysUser.setPassword(ENCODER.encode(password));
		sysUser.setUpdateTime(DateUtils.localDateTime());
		UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<SysUser>();
		updateWrapper.lambda().eq(SysUser::getUsername, username);
		return this.update(sysUser,updateWrapper);
	}

	/**
	 * 查询用户关联园区
	 *
	 * @param userId 用户Id
	 * @return List<Integer> 用户管理园区ID集合
	 */
	@Override
	public List<Integer> listUserPark(Integer userId) {
		List<Integer> parkIdList = null;
		List<SysUserPark> userParkList = sysUserParkService.getListByUserId(userId);
		if (CollectionUtils.isNotEmpty(userParkList)) {
			parkIdList = new ArrayList<>();
			for (SysUserPark sysUserPark : userParkList) {
				parkIdList.add(sysUserPark.getParkId());
			}
		}

		return parkIdList;
	}

	@Override
	public Integer loggedCount() {
		return stringRedisTemplate.keys("smart_oauth:access:*").size();
	}
}
