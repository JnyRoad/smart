package com.tce.smart.tool.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.tool.constant.SymbolConstants;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 导入导出工具类
 *
 * @author fushiping
 * @date 2020/7/10 16:49
 **/
@Slf4j
public class ToolUtils {

	private static String smbDomain() {
		return config("smart.smb.photo.domain", "SMART_SMB_PHOTO_DOMAIN", "");
	}

	private static String smbUsername() {
		return requiredConfig("smart.smb.photo.username", "SMART_SMB_PHOTO_USERNAME");
	}

	private static String smbPassword() {
		return requiredConfig("smart.smb.photo.password", "SMART_SMB_PHOTO_PASSWORD");
	}

	private static String smbRemotePhotoUrl() {
		return config("smart.smb.photo.url", "SMART_SMB_PHOTO_URL", "smb://10.0.20.99/photo$/App");
	}

	public static Boolean compareObject(Object obj1, Object obj2) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		if (null == obj1 || null == obj2) {
			return null;
		}
		Field[] fs = obj1.getClass().getDeclaredFields();
		for (Field field : fs) {
			field.setAccessible(true);
			Object v1 = field.get(obj1);
			Object v2 = field.get(obj2);
			result.put(field.getName(), String.valueOf(equals(v1, v2)));
		}

		Collection<String> collection = result.values();
		for (String str : collection) {
			if ("false".equals(str)) {
				return false;
			}
		}
		return true;
	}

	private static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		return obj1.equals(obj2);
	}

	/**
	 * 指定日期当月开始时间
	 *
	 * @return
	 */
	public static Date getDateMonthStartime(Date date) {
		Calendar dateEnd = Calendar.getInstance();
		dateEnd.setTime(date);
		dateEnd.set(Calendar.DAY_OF_MONTH, 1);
		dateEnd.set(Calendar.HOUR_OF_DAY, 0);
		dateEnd.set(Calendar.MINUTE, 0);
		dateEnd.set(Calendar.SECOND, 0);
		return dateEnd.getTime();
	}

	/**
	 * 指定日期当月结束时间
	 *
	 * @return
	 */
	public static Date getDateMonthEndTime(Date date) {

		final Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		final int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

		cal.set(Calendar.DAY_OF_MONTH, last);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);

		return cal.getTime();
	}

	/**
	 * 今天开始时间
	 *
	 * @return
	 */
	public static Date getTodayStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		return todayStart.getTime();
	}

	/**
	 * 今天结束时间
	 *
	 * @return
	 */
	public static Date getTodayEndTime() {
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		return todayEnd.getTime();
	}

	/**
	 * 指定日期开始时间
	 *
	 * @return
	 */
	public static Date getDateStartime(Date date) {
		Calendar dateEnd = Calendar.getInstance();
		dateEnd.setTime(date);
		dateEnd.set(Calendar.HOUR_OF_DAY, 0);
		dateEnd.set(Calendar.MINUTE, 0);
		dateEnd.set(Calendar.SECOND, 0);
		return dateEnd.getTime();
	}

	/**
	 * 指定日期结束时间
	 *
	 * @return
	 */
	/**
	 * 获取指定日期的最后时间
	 * @param date 指定日期
	 * @return 指定日期的最后时间
	 */
	public static Date getDateEndTime(Date date) {
		// 创建一个Calendar实例，并将指定日期设置给它
		Calendar dateEnd = Calendar.getInstance();
		dateEnd.setTime(date);
		// 设置小时为23
		dateEnd.set(Calendar.HOUR_OF_DAY, 23);
		// 设置分钟为59
		dateEnd.set(Calendar.MINUTE, 59);
		// 设置秒为59
		dateEnd.set(Calendar.SECOND, 59);
		// 返回指定日期的最后时间
		return dateEnd.getTime();
	}

	/**
	 * 获取计算时间
	 *
	 * @param date
	 * @param type
	 * @param num
	 * @return
	 */
	public static Date getCalDate(Date date, int type, int num) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(date);
		int typeNum = newTime.get(type);
		typeNum += num;
		newTime.set(type, typeNum);
		return newTime.getTime();
	}

	/**
	 * 设置指定时间
	 *
	 * @param date
	 * @param type
	 * @param num
	 * @return
	 */
	public static Date setCalDate(Date date, int type, int num) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(date);
		newTime.set(type, num);
		return newTime.getTime();
	}

	/**
	 * 设置指定月份的日期为今天
	 *
	 * @param date
	 * @return
	 */
	public static Date setMonthToday(Date date) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(date);

		Calendar toady = Calendar.getInstance();
		toady.setTime(new Date());

		//设置指定月份的日期为今天的日期
		newTime.set(Calendar.DATE, toady.get(Calendar.DATE));
		return newTime.getTime();
	}

	public static String readRemoteImgToBase64(String idCard) {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		String smbDomain = smbDomain();
		String smbUsername = smbUsername();
		String smbPassword = smbPassword();
		String remotePhotoUrl = smbRemotePhotoUrl();
		try {
			//获取图片
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(smbDomain, smbUsername, smbPassword);
			SmbFile remoteFile = new SmbFile(remotePhotoUrl + "/" + idCard + ".jpg", auth);
			remoteFile.connect(); //尝试连接
			log.info("dhr照片同步获取开始");
			//创建文件流
			in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
			out = new ByteArrayOutputStream((int) remoteFile.length());
			//读取文件内容
			byte[] buffer = new byte[4096];
			int len = 0; //读取长度
			while ((len = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, len);
			}
			out.flush(); //刷新缓冲的输出流
			String base64 = ImageUtils.encodeImage(out.toByteArray());
			log.info("dhr照片同步结束");
			return base64;
		} catch (Exception e) {
			String msg = "发生错误：" + e.getLocalizedMessage();
			log.info("dhr-urlFile-error");
			log.error("dhr照片同步获取异常:",e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
		return "";
	}

	public static List<String> readLastRemoteImgNameList(LocalDateTime lastTime) {
		List<String> fileNameList = new ArrayList<>();
		String smbDomain = smbDomain();
		String smbUsername = smbUsername();
		String smbPassword = smbPassword();
		String remotePhotoUrl = smbRemotePhotoUrl();
		try {
			//获取图片
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(smbDomain, smbUsername, smbPassword);
			SmbFile remoteFile = new SmbFile(remotePhotoUrl + "/", auth);
			remoteFile.connect(); //尝试连接
			log.info("开始获取最后更新文件1");
			if (remoteFile != null) {
				log.info("开始获取最后更新文件2");
				if (remoteFile.isDirectory()) {
					SmbFile[] smbFiles = remoteFile.listFiles();
					log.info("开始获取最后更新文件3={}", smbFiles.length);
					for (int i = 0; i < smbFiles.length; i++) {
						SmbFile file = smbFiles[i];
						long lastModified = file.getLastModified();
						LocalDateTime longToLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
						if (lastTime.isBefore(longToLocalDateTime)) {
							fileNameList.add(file.getName().replace(".jpg", ""));
						}
					}
				}
			}
			return fileNameList;
		} catch (Exception e) {
			log.error("连接DHR图片库异常:{}", e);
		}
        return null;
	}

	public static List<String> readLastRemoteImgFork(LocalDateTime lastTime) {
		List<String> fileNameList = new ArrayList<>();
		String smbDomain = smbDomain();
		String smbUsername = smbUsername();
		String smbPassword = smbPassword();
		String remotePhotoUrl = smbRemotePhotoUrl();
		try {
			//获取图片
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(smbDomain, smbUsername, smbPassword);
			SmbFile remoteFile = new SmbFile(remotePhotoUrl + "/", auth);
			remoteFile.connect(); //尝试连接
			log.info("开始获取最后更新文件");
			if (remoteFile != null) {
				if (remoteFile.isDirectory()) {
					SmbFile[] smbFiles = remoteFile.listFiles();
					List<SmbFile> smbFilesList = Arrays.asList(smbFiles);
					ForkJoinPool forkJoinPool = new ForkJoinPool();
					RemoteForkJoin task = new RemoteForkJoin(smbFilesList, lastTime);
					// 提交任务
					ForkJoinTask<List<String>> submit = forkJoinPool.submit(task);
					fileNameList = submit.get();
				}
			}
			return fileNameList;
		} catch (Exception e) {
			log.error("连接DHR图片库异常:{}", e);
		}
        return null;
	}

	/**
	 * 根据身份证获得性别
	 *
	 * @param cardNo
	 * @return
	 */
	public static SexType getGenderByIdCard(String cardNo) {
		if (Integer.parseInt(cardNo.substring(16).substring(0, 1)) % 2 == 0) {
			return SexType.WOMAN;
		} else {
			return SexType.MAN;
		}
	}

	/**
	 * 将使用逗号分隔的字符串转换为整形集合
	 *
	 * @param idStr
	 * @return
	 */
	public static List<Integer> splitInt(String idStr) {
		if (StringUtils.isEmpty(idStr)) {
			return new ArrayList<>();
		}
		int[] ids = StringUtils.splitToInt(idStr, SymbolConstants.COMMA);
		List<Integer> returnList = new ArrayList<>();
		returnList.addAll(IntStream.of(ids).boxed().collect(Collectors.toList()));
		return returnList;
	}

	/**
	 * 将使用逗号分隔的字符串转换为字符串集合
	 *
	 * @param idStr
	 * @return
	 */
	public static List<String> splitStr(String idStr) {
		if (StringUtils.isEmpty(idStr)) {
			return new ArrayList<>();
		}
		String[] idsArray = StringUtils.split(idStr, SymbolConstants.COMMA);
		List<String> returnList = new ArrayList<>();
		returnList.addAll(Stream.of(idsArray).collect(Collectors.toList()));
		return returnList;
	}


	/**
	 * 将使用空格分隔的字符串转换为字符串集合
	 *
	 * @param idsStr id组合字符串
	 * @return id集合
	 */
	public static List<String> splitBlankString(String idsStr) {
		List<String> returnList = new ArrayList<>();
		if (StringUtils.isNotEmpty(idsStr)) {
			String[] idsArray = idsStr.split("[\\s\\p{Zs}]+");
			returnList.addAll(Stream.of(idsArray).collect(Collectors.toList()));
		}
		return returnList;
	}

	public static String getBadge(String code) {
		JSONObject parameter = JSONUtil.createObj();
		parameter.put("code", code);
		String encodeParam = "";
		try {
			encodeParam = URLEncoder.encode(JSONUtil.toJsonStr(parameter), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String result = HttpRequest
				.post("https://xchr.szyuto.com:8888/commonData/getData.html?key=getUserInfoByWeixinCode&parameter=" + encodeParam)
				.execute()
				.body();
		log.info("微信绑定接口返回值：{}", result);
		JSONObject res = JSONUtil.parseObj(result);
		String one = "1";
		String accountNoExist = "账号不存在";
		String inUse = "code been used";
		String err = "invalid code";
		String outCode = res.getStr("code");
		if (one.equals(outCode)) {
			JSONObject data = res.getJSONObject("data");
			String inCode = data.getStr("code");
			if (one.equals(inCode)) {
				return data.getStr("data");
			} else {
				String msg = data.getStr("msg");
				if (accountNoExist.equals(msg)) {
					throw new SmartException(accountNoExist);
				} else if (msg.contains(inUse)) {
					throw new SmartException("code已被使用");
				} else if (msg.contains(err)) {
					throw new SmartException("code不正确");
				} else {
					throw new SmartException(msg);
				}
			}
		} else {
			throw new SmartException("获取失败");
		}
	}

	public static boolean unbindWechatAndBadge(String badge) {
		JSONObject parameter = JSONUtil.createObj();
		parameter.put("no", badge);
		String encodeParam = "";
		try {
			encodeParam = URLEncoder.encode(JSONUtil.toJsonStr(parameter), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String result = HttpRequest
				.post("https://xchr.szyuto.com:8888/commonData/getData.html?key=unbindUser&parameter=" + encodeParam)
				.execute()
				.body();
		log.info("微信解绑接口返回值：{}", result);
		JSONObject res = JSONUtil.parseObj(result);
		String one = "1";
		String outCode = res.getStr("code");
		if (one.equals(outCode)) {
			JSONArray data = res.getJSONArray("data");
			if (data == null || data.size() == 0) {
				throw new SmartException("解绑失败, 接口返回数据为空");
			}
			// 返回的data是一个数组形式，此处的1表示匹配上了一条数据，如果为0则表示没有匹配上，则可能是no错误或者已经解绑过了
			Integer code = (Integer) data.get(0);
			return code == 1;
		} else {
			log.info("解绑{}失败：{}", badge, res.getStr("msg"));
			throw new SmartException("解绑失败, 接口响应异常");
		}
	}

	/**
	 * 获取某月最后一天
	 * @return
	 */
	public static Integer getMonthLastDay(Date meterMonth) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(meterMonth);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * localDateTime转Date
	 * @param localDateTime
	 * @return
	 */
	public static Date localDateTimeToDate(LocalDateTime localDateTime) {
		ZoneId zoneId = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zoneId).toInstant();
		return Date.from(instant);
	}

	private static String config(String propertyName, String envName) {
		return config(propertyName, envName, "");
	}

	private static String config(String propertyName, String envName, String defaultValue) {
		return SmartToolConfigUtils.get(propertyName, envName, defaultValue);
	}

	private static String requiredConfig(String propertyName, String envName) {
		return SmartToolConfigUtils.getRequired(propertyName, envName);
	}
}
