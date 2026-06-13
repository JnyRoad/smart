package com.tce.smart.bridge.isc.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import org.apache.http.*;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author sunfujian
 * @date 2021/8/20 15:18
 */
public class ISCApiTest {

	private static final String DEFAULT_HOST = "10.13.21.27:443";

	private final String host = config("smart.hik.host", "SMART_HIK_HOST", DEFAULT_HOST);

	private final String appKey = config("smart.hik.app-key", "SMART_HIK_APP_KEY");

	private final String appSecret = config("smart.hik.app-secret", "SMART_HIK_APP_SECRET");

	/**
	 * OpenAPI接口的上下文
	 */
	private static final String ARTEMIS_PATH = "/artemis";

	/**
	 * 参数提交方式
	 */
	private static final String CONTENT_TYPE = "application/json";

	public static void main(String[] args) {
		// artemis网关服务器ip端口
		ArtemisConfig.host = config("smart.hik.host", "SMART_HIK_HOST", DEFAULT_HOST);
		// 秘钥appKey
		ArtemisConfig.appKey = config("smart.hik.app-key", "SMART_HIK_APP_KEY");
		// 秘钥appSecret
		ArtemisConfig.appSecret = config("smart.hik.app-secret", "SMART_HIK_APP_SECRET");
//		Map<String, Object> params = new HashMap<>();
//		params.put("pageNo", 1);
//		params.put("pageSize", 10);
//		params.put("resourceType", "acsDevice");
//		System.out.println(ArtemisHttpUtil.doPostStringArtemis(getPath("/api/irds/v2/resource/resourcesByParams"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE , null));
//
//		Map<String, Object> params1 = new HashMap<>();
//		params1.put("resourceType", "acsDevice");
//		params1.put("resourceIndexCodes", new String[]{"0c6a84a8392f437c81019fd93c70a087"});
//		System.out.println(ArtemisHttpUtil.doPostStringArtemis(getPath("/api/resource/v1/resource/indexCodes/search"), JSONUtil.toJsonStr(params1), null, null, CONTENT_TYPE , null));

//		downAccess();
//		queryTaskProcess("65c4bc5875ca40f5b24acdcb0ec23182");
//		queryTaskRecord("65c4bc5875ca40f5b24acdcb0ec23182");
//		queryTaskDetailByResultId("15f436bc4370ce99d2d107f90b771e90");

		String deviceCode = "57a291faa7f34c438341063167d1fc89";
		String personId = "1402512566937882625";

//		String taskId = createDownTask();
//		addAccessToTask(taskId, deviceCode, personId, 0);
//		addAccessToTask(taskId, deviceCode, personId, 1);
//		addAccessToTask(taskId, deviceCode, personId, 2);
//		startTask(taskId);
//		queryTaskProcess(taskId);
//		deleteTask("dcd0ffcaa62d4c4fa3f28e8ef95a6ea8");

//		authConfigAdd(deviceCode, personId);
//		authConfigDown(deviceCode);

//		String param = "{\"resourceInfos\":[{\"resourceIndexCode\":\"57a291faa7f34c438341063167d1fc89\",\"channelNos\":[1],\"resourceType\":\"acsDevice\"}],\"personDatas\":[{\"indexCodes\":[\"10061\"],\"personDataType\":\"person\"}]}";
//
//		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/auth_config/delete"), param, null, null, CONTENT_TYPE, null);
//		System.out.println("查询下载任务详情响应："+resp);

//		configProcessGet("c5c2456b574548d391642685650ae197");
//		downAccess("57a291faa7f34c438341063167d1fc89", 4);
//		downProcessGet("f996159f615141e280c7c8435681f724");
//		downListGet("6da42f2c073d4061b2d60e27a59e6b56", "d08b4c7ed3ab49b78c0e0a3a636c6bd0");
//		getISCImg("/pic?1d67=f605iac-=o571bp32b337b2-e8f3a5ab7*8c4==sp**212==t3*6816012156l4*7162=5=3o3*9-18*ae30od1e3l98-0934e9",
//				"e3eeb419-b5e8-42e0-a208-132df568bebf");
		getPersonDetail();
	}

	public static void getPersonDetail() {
		Map<String, Object> params = new HashMap<>(2);
		params.put("paramName", "personId");
		// 通过人员主键查询详情，获取工号
		params.put("paramValue", new String[]{"ZLC00001"});
		System.out.println(ArtemisHttpUtil.doPostStringArtemis(getPath("/api/resource/v1/person/condition/personInfo"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE , null));
	}

	public static <T> void getISCImg(String picUrl, String serverCode) {
		Map<String, String> params = new HashMap<>(2);
//		params.put("url", picUrl);
		params.put("picUri", picUrl);
		params.put("svrIndexCode", serverCode);
		// 调用接口
		// 人脸服务图片下载：/api/frs/v1/application/picture
		// 提取人员图片: /api/resource/v1/person/picture
//		HttpResponse response = ArtemisHttpUtil.doPostFormImgArtemis(getPath("/api/acs/v1/event/pictures"), params, null, null, CONTENT_TYPE , null);
		HttpResponse response = ArtemisHttpUtil.doPostStringImgArtemis(getPath("/api/acs/v1/event/pictures"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE , null);
		int statusCode = response.getStatusLine().getStatusCode();
		System.out.println("状态码："+statusCode);
		saveImage(response);
	}

	public static void saveImage(HttpResponse response) {
		try {
			byte[] imgByte = IoUtil.readBytes(response.getEntity().getContent());
			FileUtil.writeBytes(imgByte, new File("G:\\iscImage.jpg"));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void configProcessGet(String taskId) {
		Map<String, String> param = new HashMap<>(1);
		param.put("taskId", taskId);
		System.out.println("配置进度："+ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/auth_config/rate/search"), JSONUtil.toJsonStr(param), null, null, CONTENT_TYPE, null));
	}

	public static void downAccess(String deviceCode, Integer taskType) {
		Map<String, Object> deviceInfo = new HashMap<>(3);
		deviceInfo.put("resourceIndexCode", deviceCode);
		deviceInfo.put("resourceType", "acsDevice");
		deviceInfo.put("channelNos", new Integer[]{1});
		Map<String, Object> downAuth = new HashMap<>(2);
		downAuth.put("taskType", taskType);
		downAuth.put("resourceInfos", new Object[]{deviceInfo});
		System.out.println("下载权限："+ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/configuration/shortcut"), JSONUtil.toJsonStr(downAuth), null, null, CONTENT_TYPE, null));
	}

	public static void downProcessGet(String taskId) {
		Map<String, String> param = new HashMap<>(1);
		param.put("taskId", taskId);
		System.out.println("下载进度："+ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/progress"), JSONUtil.toJsonStr(param), null, null, CONTENT_TYPE, null));
	}

	public static void downListGet(String taskId, String deviceCode) {
		Map<String, Object> deviceMap = new HashMap<>(3);
		deviceMap.put("resourceIndexCode", deviceCode);
		deviceMap.put("resourceType", "acsDevice");
		deviceMap.put("channelNos", new Integer[]{1});
		Map<String, Object> detail = new HashMap<>(5);
		detail.put("taskId", taskId);
		detail.put("resourceInfos", new Object[]{deviceMap});
		detail.put("taskTypes", new Integer[]{4});
		detail.put("pageNo", 1);
		detail.put("pageSize", 1000);
		System.out.println("下载权限列表："+ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/download_record/channel/list/search"), JSONUtil.toJsonStr(detail), null, null, CONTENT_TYPE, null));
	}

	public static void downAccess() {
		Map<String, Object> deviceInfo = new HashMap<>(3);
		deviceInfo.put("resourceIndexCode", "57a291faa7f34c438341063167d1fc89");
		deviceInfo.put("resourceType", "acsDevice");
		deviceInfo.put("channelNos", new Integer[]{1});
		// 人员信息
		Map<String, Object> personInfo = new HashMap<>(5);
		personInfo.put("personId", "1402512566937882625");
		// 操作类型，0新增；1修改；2删除
		personInfo.put("operatorType", 0);
		personInfo.put("personType", 1);
		personInfo.put("startTime", parseISODate(1630395009L));
		personInfo.put("endTime", parseISODate(LocalDateTime.now().plusYears(10).toEpochSecond(ZoneOffset.of("+8"))));
		// 最终参数信息
		Map<String, Object> params = new HashMap<>(3);
		params.put("resourceInfo", deviceInfo);
		params.put("personInfo", personInfo);
		/**
		 * TYPE_1(1, "卡片"),
		 * 	TYPE_2(2, "指纹"),
		 * 	TYPE_3(3, "卡片+指纹（组合）"),
		 * 	TYPE_4(4, "人脸"),
		 * 	TYPE_5(5, "卡片+人脸（组合）"),
		 * 	TYPE_6(6, "人脸+指纹（组合）"),
		 * 	TYPE_7(7, "卡片+指纹+人脸（组合）");
		 */
		params.put("taskType", 5);
		System.out.println("参数："+JSONUtil.toJsonStr(params));
		System.out.println("响应："+ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/simpleDownload"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE , null));
	}

	public static void authConfigAdd(String deviceCode, String personId) {
		// 设备信息
		Map<String, Object> deviceInfo = new HashMap<>(3);
		deviceInfo.put("resourceIndexCode", deviceCode);
		deviceInfo.put("resourceType", "acsDevice");
		deviceInfo.put("channelNos", new Integer[]{1});
		// 人员信息
		Map<String, Object> personInfo = new HashMap<>(2);
		personInfo.put("indexCodes", new String[]{personId});
		personInfo.put("personDataType", "person");
		// 最终参数信息
		Map<String, Object> params = new HashMap<>(4);
		params.put("resourceInfos", new Object[]{deviceInfo});
		params.put("personDatas", new Object[]{personInfo});
		params.put("startTime", parseISODate(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))));
		params.put("endTime", parseISODate(LocalDateTime.now().plusYears(10).toEpochSecond(ZoneOffset.of("+8"))));

		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/auth_config/add"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE, null);
		System.out.println("添加权限配置："+resp);
	}

	public static void authConfigDown(String deviceCode) {
		// 设备信息
		Map<String, Object> deviceInfo = new HashMap<>(3);
		deviceInfo.put("resourceIndexCode", deviceCode);
		deviceInfo.put("resourceType", "acsDevice");
		deviceInfo.put("channelNos", new Integer[]{1});
		// 最终参数信息
		Map<String, Object> params = new HashMap<>(4);
		params.put("resourceInfos", new Object[]{deviceInfo});
		params.put("taskType", 4);

		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/configuration/shortcut"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE, null);
		System.out.println("下载权限配置："+resp);
	}

	/**
	 * 创建下载任务
	 * @return
	 */
	public static String createDownTask() {
		Map<String, Object> taskInfo = new HashMap<>(1);
		taskInfo.put("taskType", 5);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/addition"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("创建任务响应："+resp);
		return JSONUtil.parseObj(resp).getJSONObject("data").getStr("taskId");
	}

	public static boolean addAccessToTask(String taskId, String deviceCode, String personId, Integer action) {
		// 设备信息
		Map<String, Object> deviceInfo = new HashMap<>(3);
		deviceInfo.put("resourceIndexCode", deviceCode);
		deviceInfo.put("resourceType", "acsDevice");
		deviceInfo.put("channelNos", new Integer[]{1});
		// 人员信息
		Map<String, Object> personInfo = new HashMap<>(5);
		personInfo.put("personId", personId);
		// 操作类型，0新增；1修改；2删除
		personInfo.put("operatorType", action);
		personInfo.put("personType", 1);
		personInfo.put("startTime", parseISODate(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))));
		personInfo.put("endTime", parseISODate(LocalDateTime.now().plusYears(10).toEpochSecond(ZoneOffset.of("+8"))));
		// 最终参数信息
		Map<String, Object> params = new HashMap<>(3);
		params.put("taskId", taskId);
		params.put("resourceInfos", deviceInfo);
		params.put("personInfos", personInfo);

		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/data/addition"), JSONUtil.toJsonStr(params), null, null, CONTENT_TYPE, null);
		System.out.println("向任务中添加数据响应："+resp);
		return JSONUtil.parseObj(resp).getInt("code") == 0;
	}

	public static void startTask(String taskId) {
		Map<String, Object> taskInfo = new HashMap<>(1);
		taskInfo.put("taskId", taskId);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/start"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("开始任务响应："+resp);
	}

	public static void queryTaskProcess(String taskId) {
		Map<String, Object> taskInfo = new HashMap<>(1);
		taskInfo.put("taskId", taskId);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/progress"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("查询下载任务进度响应："+resp);
	}

	public static void queryTaskRecord(String taskId) {
		Map<String, Object> taskInfo = new HashMap<>(3);
		taskInfo.put("taskId", taskId);
		taskInfo.put("pageNo", 1);
		taskInfo.put("pageSize", 100);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/download_record/channel/list/search"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("查询下载任务记录响应："+resp);
	}

	public static void queryTaskDetailByResultId(String resultId) {
		Map<String, Object> taskInfo = new HashMap<>(3);
		taskInfo.put("downloadResultId", resultId);
		taskInfo.put("pageNo", 1);
		taskInfo.put("pageSize", 100);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v2/download_record/person/detail/search"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("查询下载任务详情响应："+resp);
	}

	public static void deleteTask(String taskId) {
		Map<String, Object> taskInfo = new HashMap<>(3);
		taskInfo.put("taskId", taskId);
		String resp = ArtemisHttpUtil.doPostStringArtemis(getPath("/api/acps/v1/authDownload/task/deletion"), JSONUtil.toJsonStr(taskInfo), null, null, CONTENT_TYPE, null);
		System.out.println("删除任务响应："+resp);
	}

	public static String parseISODate(Long timeSecond) {
		LocalDateTime end = LocalDateTime.ofEpochSecond(timeSecond, 0, ZoneOffset.of("+08:00"));
		return OffsetDateTime.of(end, ZoneOffset.of("+08:00")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
	}

	public static Map<String, String> getPath(String api) {
		// 设置接口的URI地址
		Map<String, String> path = new HashMap<>(1);
		path.put("https://", ARTEMIS_PATH + api);
		return path;
	}

	private static String config(String propertyName, String envName) {
		return config(propertyName, envName, "");
	}

	private static String config(String propertyName, String envName, String defaultValue) {
		String value = System.getProperty(propertyName);
		if (StrUtil.isNotBlank(value)) {
			return value;
		}
		value = System.getenv(envName);
		return StrUtil.isNotBlank(value) ? value : defaultValue;
	}
}
