package com.tce.smart.bridge.isc.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.isc.enums.ImageEnum;
import com.tce.smart.bridge.isc.service.HBaseFileService;
import com.tce.smart.bridge.isc.service.HandleService;
import com.tce.smart.bridge.isc.util.ISCEventDiagnosticUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.UUIDUtils;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.enums.ISCEventTypeEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author sunfujian
 * @date 2021/9/1 11:20
 */
@Slf4j
@Service
public class HandleServiceImpl implements HandleService {

	@Autowired
	private RemoteDispatcherService remoteDispatcherService;

	@Autowired
	private BridgeISCServiceImpl bridgeISCService;

	@Autowired
	private HBaseFileService hBaseFileService;

	@Value("${smart.park-id}")
	private Integer parkId;

	/**
	 * @param eventData
	 * @return
	 */
	@Override
	public boolean eventHandle(String eventData) {
		com.tce.smart.dispatcher.api.dto.resp.BridgeDTO<String> bridgeDTO = null;
		try {
			long start = DateUtils.toEpochMilli();
			log.info("收到-ISC平台消息：{}", eventData);
			JSONObject eventDataJson = JSONUtil.parseObj(eventData);
			JSONObject paramJson = eventDataJson.getJSONObject("params");
			JSONArray eventJson = paramJson.getJSONArray("events");

			// 记录事件处理开始
			ISCEventDiagnosticUtil.recordEventStart();

			for (int i = 0; i < eventJson.size(); i++) {
				JSONObject eventObj = eventJson.getJSONObject(i);
				Integer eventType = eventObj.getInt("eventType");
				JSONObject dataObj = eventObj.getJSONObject("data");
				ISCEventTypeEnum eventTypeEnum = ISCEventTypeEnum.getByCode(eventType);
				Map<String, Object> paramMap = new HashMap<>(8);
				switch (eventTypeEnum) {
					case TYPE_1:
					case TYPE_2:
					case TYPE_3:
					case TYPE_4:
					case TYPE_5:
					case TYPE_6:
						// ISC平台的人员主键
						String personNo = dataObj.getStr("ExtEventPersonNo");

						// 记录完整的事件数据用于问题诊断
						log.info("处理ISC事件，事件类型：{}，设备编码：{}，发生时间：{}",
								eventType, eventObj.getStr("srcParentIndex"), eventObj.getStr("happenTime"));

						if (StrUtil.isBlank(personNo)) {
							// 使用诊断工具记录人员编号为空的事件
							ISCEventDiagnosticUtil.recordEmptyPersonNo(eventType, eventObj.getStr("srcParentIndex"), dataObj);

							// 继续处理下一个事件，不中断整个流程
							continue;
						}

						Map<String, Object> params = new HashMap<>(2);
						params.put("paramName", "personId");
						// 通过人员主键查询详情，获取工号
						params.put("paramValue", new String[]{personNo});

						log.debug("查询ISC人员详情，personNo：{}", personNo);
						ISCResponse personResp = bridgeISCService.post(EventEnum.ISC_PERSON_GET, JSONUtil.toJsonStr(params));

						if (personResp == null || !"0".equals(personResp.getCode())) {
							// 记录人员未找到的事件
							ISCEventDiagnosticUtil.recordPersonNotFound(personNo, eventObj.getStr("srcParentIndex"));
							// 继续处理下一个事件
							continue;
						}

						JSONArray personList = JSONUtil.parseObj(personResp.getData()).getJSONArray("list");
						if (personList != null && personList.size() > 0) {
							String jobNo = personList.getJSONObject(0).getStr("jobNo");
							if (StrUtil.isNotBlank(jobNo)) {
								paramMap.put("cardNo", jobNo);
							}
							log.debug("成功获取人员工号，personNo：{}，jobNo：{}", personNo, jobNo);
						} else {
							// 记录人员详情数据为空的事件
							ISCEventDiagnosticUtil.recordPersonNotFound(personNo, eventObj.getStr("srcParentIndex"));
							// 继续处理下一个事件
							continue;
						}
						paramMap.put("personId", personNo);
						paramMap.put("deviceCode", eventObj.getStr("srcParentIndex"));
						/**
						 * ExtEventInOut-进出类型 1：进0：出-1:未知
						 * 				要求：进门读卡器拨码设置为1，出门读卡器拨码设置为2
						 * openMethod-开门方式  1:进,2:出
						 */
						Integer inOut = dataObj.getInt("ExtEventInOut");
						paramMap.put("openMethod", inOut.equals(0) ? 2 : inOut);
						// 是否放行 0-未放行;1-放行;2-未知
						paramMap.put("letPass", eventTypeEnum.equals(ISCEventTypeEnum.TYPE_1) || eventTypeEnum.equals(ISCEventTypeEnum.TYPE_3)
								|| eventTypeEnum.equals(ISCEventTypeEnum.TYPE_5) ? 1 : 0);
						LocalDateTime happenTime = LocalDateTime.parse(eventObj.getStr("happenTime"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
						paramMap.put("eventTime", happenTime.toEpochSecond(ZoneOffset.of("+8")));
						// 图片服务器ID
						String svrIndexCode = dataObj.getStr("svrIndexCode");
						String picUrl = dataObj.getStr("ExtEventPictureURL");
						byte[] imageByte = bridgeISCService.downISCImage(EventEnum.ISC_FACE_IMAGE_GET, picUrl, svrIndexCode);
						String imgId = StringUtils.EMPTY;
						if (imageByte != null) {
							imgId = IdUtil.fastUUID();
							boolean saveImg = hBaseFileService.save(imgId, imageByte, ImageEnum.ACCESS.getType());
							if (!saveImg) {
								throw new SmartException("保存人员通行图片失败");
							}
						} else {
							log.warn("下载ISC图片失败: {}", personNo);
						}
						paramMap.put("snapPhoto", imgId);
						paramMap.put("isISC", true);

						bridgeDTO = new com.tce.smart.dispatcher.api.dto.resp.BridgeDTO<>();
						bridgeDTO.setEventId(UUIDUtils.create());
						bridgeDTO.setEventType(eventTypeEnum.getEventEnum().getCode());
						bridgeDTO.setParkId(parkId);
						bridgeDTO.setData(JSONUtil.toJsonStr(paramMap));
						Result<Boolean> result = remoteDispatcherService.handle(bridgeDTO, SecurityConstants.FROM_IN,
								SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
						log.info("处理-ISC平台消息，key：{} - {}，EventId：{}，结果：{}，耗时：{}ms", eventTypeEnum.getEventEnum().getKey(), eventTypeEnum.getEventEnum().getDesc(), bridgeDTO.getEventId(), result.isSuccess(), DateUtils.toEpochMilli() - start);
						if (!result.isSuccess()) {
							throw new TCEException("Dispatcher处理失败");
						} else {
							// 记录成功处理的事件
							ISCEventDiagnosticUtil.recordSuccess(eventObj.getStr("srcParentIndex"));
						}
						break;
					case UNKNOWN:
						log.info("未定义的ISC事件通知类型：{}", eventType);
				}
			}
		} catch (Exception e) {
			log.error("处理-ISC平台消息出现异常，{}", e.getMessage(), e);
			if (Objects.nonNull(bridgeDTO)) {
				log.warn("处理-ISC平台消息，EventId：{}，写入数据库异常消息表！", bridgeDTO.getEventId());
//				exceptionLogService.insert(record.key(), JSONUtil.toJsonStr(bridgeDTO));
			}
		}
		return true;
	}

	@Override
	public boolean callbackHandle(String callbackData) {
		return false;
	}

	/**
	 * 定时输出ISC事件处理统计报告
	 * 每小时执行一次
	 */
	@Scheduled(cron = "0 0 * * * ?")
	public void printStatisticsReport() {
		try {
			String report = ISCEventDiagnosticUtil.getStatisticsReport();
			log.info("ISC事件处理统计报告：{}", report);

			// 检查是否需要告警
			if (ISCEventDiagnosticUtil.shouldAlert()) {
				log.error("ISC事件处理异常比例过高，请检查设备和人员配置！");
			}
		} catch (Exception e) {
			log.error("生成ISC统计报告失败", e);
		}
	}

	/**
	 * 每天凌晨重置统计数据
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void resetDailyStatistics() {
		try {
			// 先输出最终报告
			String finalReport = ISCEventDiagnosticUtil.getStatisticsReport();
			log.info("每日ISC事件处理最终统计报告：{}", finalReport);

			// 重置统计数据
			ISCEventDiagnosticUtil.resetStatistics();
		} catch (Exception e) {
			log.error("重置ISC统计数据失败", e);
		}
	}
}
