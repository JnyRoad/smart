package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.tce.smart.admin.api.dto.InternalUserPhoneSyncReqDTO;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.admin.api.feign.RemoteUserInternalService;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.fore.SettingService;
import com.tce.smart.app.vo.fore.CheckVersionVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.LoginResult;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.InternalStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import com.tce.smart.tool.constant.DictConstants;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * App设置服务实现类
 *
 * @author mckaywu
 * @date 2019-06-17 10:03:12
 */
@Service
@Slf4j
public class SettingServiceImpl implements SettingService {

	private final static Pattern VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)+$");

	private final static String MAX_VERSION = "maxVersion";

	private final static String MAX_FILE_NAME = "maxFileName";

	/**
	 * App整包更新开关-开启
	 */
	private final static String APP_REINSTALL_SWITCH_ON = "1";

	/** 旧手机号验证只在当前认证员工的短时换绑流程内有效，不能由客户端伪造或跨账号复用。 */
	private static final String PHONE_CHANGE_OLD_VERIFIED_KEY = "smart_app:phone-change:old-verified:";
	private static final long PHONE_CHANGE_OLD_VERIFIED_TTL_SECONDS = 600L;
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

	@Value("${spring.yuto-secsytem.phone.update-url}")
	private String phoneUpdateUrl;

	@Value("${spring.yuto-secsytem.phone.update-token}")
	private String updateToken;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private AppSmsService appSmsService;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Autowired
	private RemoteUserInternalService remoteUserInternalService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public CheckVersionVo checkVersion(String appId, String appVersion) {
		//检查AppId是否合法
		if (!StringUtil.isNullOrEmpty(appId) && !getConfigFromDict(DictConstants.APP_APPID).equals(appId)) {
			throw new TCEException("AppId无效");
		}

		CheckVersionVo checkVersionVo = new CheckVersionVo();
		checkVersionVo.setIsNeedUpdate(false);

		// App上送版本格式检查
		if (!VERSION_PATTERN.matcher(appVersion).matches()) {
			log.error("客户端上送版本格式无效");
			return checkVersionVo;
		}

		// 检查升级包存放地址地址是否配置
		String packPath = getConfigFromDict(DictConstants.APP_UPGRADE_PACK_PATH);

		// 检查升级包下载地址是否配置
		String packUrl = getConfigFromDict(DictConstants.APP_UPGRADE_PACK_URL);

		// 获取最新发布的升级包名称
		LatestVersion latestVersion = getMaxFileVer(packPath);
		log.info("App最新发布升级包===={}", latestVersion);
		if (ObjectUtil.isNotNull(latestVersion)) {
			if (compareVersion(latestVersion.getVersoinNo(), appVersion) > 0) {
				checkVersionVo.setIsNeedUpdate(true);
				checkVersionVo.setLatestVersion(latestVersion.getVersoinNo());
				checkVersionVo.setPatchUrl(packUrl.replace("{version}", latestVersion.getFileName()));

				//整包更新配置
				boolean isReInstall = getConfigFromDict(DictConstants.APP_REINSTALL_SWITCH).equals(APP_REINSTALL_SWITCH_ON);
				checkVersionVo.setIsNeedReInstall(isReInstall);
				//优先取数据库配置的安装包地址
				String appInstallUrl = getConfigFromDict(DictConstants.APP_INSTALL_URL).trim();
				if(StringUtil.isNullOrEmpty(appInstallUrl) && !StringUtil.isNullOrEmpty(latestVersion.getInstallFileName())){
					appInstallUrl = packUrl.replace("{version}", latestVersion.getInstallFileName());
				}
				checkVersionVo.setInstallAppUrl(appInstallUrl);
			}
		}

		return checkVersionVo;
	}

	@Override
	public boolean sendOldPhoneCode() {
		appSmsService.sendSmsCode(currentStaffPhone());
		return Boolean.TRUE;
	}

	@Override
	public boolean verifyOldPhoneCode(String smsCode) {
		String oldPhone = currentStaffPhone();
		if (appSmsService.verifySmsCode(oldPhone, smsCode)) {
			stringRedisTemplate.opsForValue().set(phoneChangeAuthKey(), phoneFingerprint(oldPhone),
					PHONE_CHANGE_OLD_VERIFIED_TTL_SECONDS, TimeUnit.SECONDS);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean sendNewPhoneCode(String mobile) {
		requireOldPhoneVerified();
		appSmsService.sendSmsCode(requireMobile(mobile));
		return Boolean.TRUE;
	}

	@Override
	public boolean confirmNewPhone(String mobile, String smsCode) {
		requireOldPhoneVerified();
		String newMobile = requireMobile(mobile);
		if(appSmsService.verifySmsCode(newMobile, smsCode)){
			SmartUser user = SecurityUtils.getUser();
			// 更新远端数据
			Map<String, String> param = new HashMap<>();
			param.put("UserName", user.getUsername());
			param.put("NewPhone", newMobile);
			param.put("TokenID",updateToken);
			String newUri = UriComponentsBuilder.fromHttpUrl(phoneUpdateUrl)
					.replaceQuery(HttpUtil.toParams(param))
					.build(true)
					.toString();
			HttpResponse response = HttpUtils.createGet(newUri).execute();
			log.info("HttpUtils.createGet=======updatePhone==========",response.body());
			LoginResult result = HttpUtils.parse(response, LoginResult.class);
			assert result != null;
			if (result.getType().equals(1) && result.getErrorcode().equals(0)) {
				// 修改本地 smt_staff 数据，使用最小内部更新请求避免透传员工实体。
				InternalStaffPhoneUpdateReqDTO staff = new InternalStaffPhoneUpdateReqDTO();
				staff.setBadge(user.getUsername());
				staff.setPhone(newMobile);
				Result<Boolean> staffUpdate = remoteStaffInternalService.updatePhone(staff,
						SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "phone-update");
				if (!staffUpdate.isSuccess() || !Boolean.TRUE.equals(staffUpdate.getData())) {
					throw new TCEException("员工手机号更新失败");
				}
				// 修改sys_user 表数据
				InternalUserPhoneSyncReqDTO needUpdate = new InternalUserPhoneSyncReqDTO();
				needUpdate.setUsername(user.getUsername());
				needUpdate.setPhone(newMobile);
				Result<Boolean> userUpdate = remoteUserInternalService.syncAppPhone(needUpdate);
				if (!userUpdate.isSuccess() || !Boolean.TRUE.equals(userUpdate.getData())) {
					throw new TCEException("用户手机号更新失败");
				}
				clearOldPhoneVerified();
				return true;
			}
		}
		return false;
	}

	/** 当前会话唯一身份是工号，旧手机号必须从受服务令牌保护的内部资料投影读取。 */
	private String currentStaffPhone() {
		SmartUser user = SecurityUtils.getUser();
		if (user == null || StringUtils.isBlank(user.getUsername())) {
			throw new TCEException("当前登录员工信息缺失");
		}
		Result<InternalStaffPhoneRespDTO> result = remoteStaffInternalService.getPasswordPhone(
				user.getUsername(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				"self-phone-verify");
		if (!result.isSuccess() || result.getData() == null || StringUtils.isBlank(result.getData().getPhone())) {
			throw new TCEException("获取员工手机号失败，请联系人资管理员");
		}
		return requireMobile(result.getData().getPhone());
	}

	private void requireOldPhoneVerified() {
		String verifiedPhoneFingerprint = stringRedisTemplate.opsForValue().get(phoneChangeAuthKey());
		String currentPhoneFingerprint = phoneFingerprint(currentStaffPhone());
		if (verifiedPhoneFingerprint == null || !MessageDigest.isEqual(
				verifiedPhoneFingerprint.getBytes(StandardCharsets.UTF_8), currentPhoneFingerprint.getBytes(StandardCharsets.UTF_8))) {
			throw new TCEException("请先验证原手机号码");
		}
	}

	private void clearOldPhoneVerified() {
		stringRedisTemplate.delete(phoneChangeAuthKey());
	}

	private String phoneChangeAuthKey() {
		SmartUser user = SecurityUtils.getUser();
		if (user == null || user.getId() == null || StringUtils.isBlank(user.getUsername())) {
			throw new TCEException("当前登录员工信息缺失");
		}
		return PHONE_CHANGE_OLD_VERIFIED_KEY + user.getId();
	}

	/** Redis 只保存旧手机号的 SHA-256 指纹，状态仍会在读取时与当前平台资料重新绑定。 */
	private String phoneFingerprint(String mobile) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(mobile.getBytes(StandardCharsets.UTF_8));
			StringBuilder fingerprint = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				fingerprint.append(String.format("%02x", value));
			}
			return fingerprint.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new TCEException("手机号验证状态初始化失败");
		}
	}

	private String requireMobile(String mobile) {
		String normalizedMobile = mobile == null ? null : mobile.trim();
		if (normalizedMobile == null || !MOBILE_PATTERN.matcher(normalizedMobile).matches()) {
			throw new TCEException("手机号格式错误");
		}
		return normalizedMobile;
	}

	/**
	 * 检查更新AppId
	 * @param dictKey 字段key值
	 * @return true-通过，false-失败
	 */
	private String getConfigFromDict(String dictKey) {
		if (StringUtil.isNullOrEmpty(dictKey)) {
			throw new TCEException("获取配异常");
		}

		String packPath = null;
		// App安装包AppId
		Result<List<SysDict>> dictListRs = remoteDictService.findByType(dictKey, SecurityConstants.FROM_IN);
		if (!dictListRs.isSuccess() || CollectionUtils.isEmpty(dictListRs.getData())) {
			log.error("[{}]参数未配置", dictKey);
			throw new TCEException("后台参数未配置异常");
		}

		if (dictListRs.getData().size() > 1) {
			log.error("发现多个[{}]，只能配一个", dictKey);
			throw new TCEException("后台参数未配置异常");
		}

		SysDict packPathSysDict = dictListRs.getData().get(0);
		// App安装包AppId
		return packPathSysDict.getValue();
	}

	/**
	 * 获取最新发布的升级包名称
	 *
	 * @param packPath 升级包路径
	 * @return
	 */
	private LatestVersion getMaxFileVer(String packPath) {
		if (StringUtil.isNullOrEmpty(packPath)) {
			return null;
		}

		LatestVersion latestVersion = null;
		log.debug("packPath======={}", packPath);
		File dirFile = new File(packPath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			log.error("App升级包存放地址目录不存在");
			throw new TCEException("未能获取到最新版本信息");
		}

		//获取更新补丁包
		Map<String,String> versionFileMap = getMaxFile(dirFile, "wgt");
		if(CollectionUtils.isEmpty(versionFileMap)){
			return null;
		}

		latestVersion = new LatestVersion();
		latestVersion.setFileName(versionFileMap.get(MAX_FILE_NAME));
		latestVersion.setVersoinNo(versionFileMap.get(MAX_VERSION));

		//获取安装包******注意：apk只支持安卓手机，IOS需要修改
		Map<String,String> installFileMap = getMaxFile(dirFile, "apk");
		if(CollectionUtils.isNotEmpty(installFileMap)){
			String maxVersion = installFileMap.get(MAX_VERSION);
			String maxFileName = installFileMap.get(MAX_FILE_NAME);
			if(!StringUtil.isNullOrEmpty(maxVersion)
					&& !StringUtil.isNullOrEmpty(maxFileName)
					&& maxVersion.equals(latestVersion.getVersoinNo())){
				latestVersion.setInstallFileName(maxFileName);
			}
		}

		return latestVersion;
	}

	private Map<String,String> getMaxFile(File dirFile, String fileType) {
		// 过滤文件
		File[] updatePckFiles = filterFile(dirFile, fileType);
		if (ArrayUtil.isEmpty(updatePckFiles)) {
			log.error("App升级包不存在");
			return null;
		}

		String fileName = null;
		String fileVersion = null;
		String maxFileName = null;
		String maxVersoin = null;
		for (File tempFile : updatePckFiles) {
			fileName = tempFile.getName();
			// 截取文件名版本号部分
			fileVersion = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf("."));
			if (VERSION_PATTERN.matcher(fileVersion).matches()) {
				if (ObjectUtil.isNotNull(maxVersoin)) {
					// 版本号比较
					if (compareVersion(maxVersoin, fileVersion) < 0) {
						maxFileName = fileName;
						maxVersoin = fileVersion;
					}
				} else {
					maxVersoin = fileVersion;
					maxFileName = fileName;
				}
			}
		}

		Map<String, String> versionFileMap = new HashMap<>();
		versionFileMap.put(MAX_VERSION, maxVersoin);
		versionFileMap.put(MAX_FILE_NAME, maxFileName);
		return versionFileMap;
	}

	/**
	 * 过滤文件
	 *
	 * @param dirFile  文件存放目录
	 * @param fileType 文件后缀名
	 * @return 文件列表
	 */
	private File[] filterFile(File dirFile, String fileType) {
		File[] updatePckFiles = dirFile.listFiles(pathname -> pathname.getName().endsWith("." + fileType));
		return updatePckFiles;
	}

	/**
	 * 版本号比较，适用于xx.xx... 版本标识格式
	 *
	 * @param currVersion  版本1
	 * @param otherVersion 版本2
	 * @return 小于0：版本1 < 版本2|等于0：版本1 = 版本2|大于0：版本1 > 版本2|
	 */
	public int compareVersion(String currVersion, String otherVersion) {
		String[] currVersionArr = currVersion.split("\\.");
		String[] otherVersionArr = otherVersion.split("\\.");

		int minLength = Math.min(currVersionArr.length, currVersionArr.length);

		int idx = 0;
		int diff = 0;
		// 先比较长度，再比较字符
		while (idx < minLength && (diff = currVersionArr[idx].length() - otherVersionArr[idx].length()) == 0
				&& (diff = currVersionArr[idx].compareTo(otherVersionArr[idx])) == 0) {
			++idx;
		}
		// 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大
		diff = (diff != 0) ? diff : currVersionArr.length - otherVersionArr.length;
		return diff;

	}

	@Data
	private class LatestVersion {
		/**
		 * 版本号
		 */
		private String versoinNo;

		/**
		 * 补丁包文件名称
		 */
		private String fileName;

		/**
		 * 补丁包文件名称
		 */
		private String installFileName;
	}
}
