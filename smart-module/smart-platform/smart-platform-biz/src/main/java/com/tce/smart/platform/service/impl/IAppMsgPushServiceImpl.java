package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.api.feign.RemoteAppDeviceService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.QueryAppMsgRecDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.MsgStateVO;
import com.tce.smart.platform.core.vo.QueryAppMsgRecVO;
import com.tce.smart.platform.service.IAppMsgPushService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;
import com.tce.smart.push.feign.RemotePushService;
import com.tce.smart.tool.enums.DeviceOSTypeEnum;
import com.tce.smart.tool.enums.MsgTemplateTypeEnum;
import com.tce.smart.tool.enums.SmsRecordSateEnum;
import com.tce.smart.tool.enums.SmsTemplateEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * App消息推送服务实现类
 *
 * @author mkwu
 * @date 2019-07-05
 */
@Slf4j
@Service
public class IAppMsgPushServiceImpl implements IAppMsgPushService {

	@Autowired
	private RemoteAppDeviceService remoteAppDeviceService;

	@Autowired
	private RemotePushService remotePushService;

	@Autowired
	private SmtMsgRecordService msgRecordService;

	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;

	@Autowired
	private SmtStaffService smtStaffService;

	private static final String BUSINESS_TYPE = "businessType";

	private static final String BUSINESS_ID = "businessId";

	// App消息推送业务类型-模板关联信息Map
	public static final Map<String, Integer> APP_MSG_BUS_MAP;

	static {
		APP_MSG_BUS_MAP = new HashMap<String, Integer>();
		// 访客
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_1301.getCode(), MsgTemplateTypeEnum.VISITOR.getCode());// 外向内预约审批通知
		// 招聘类
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2301.getCode(), MsgTemplateTypeEnum.RECRUIT.getCode());// 简历投递通知HR
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2302.getCode(), MsgTemplateTypeEnum.RECRUIT.getCode());// 员工入职
		/// 离职类
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2303.getCode(), MsgTemplateTypeEnum.DIMISSION.getCode());// 离职审批通过通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2304.getCode(), MsgTemplateTypeEnum.DIMISSION.getCode());// 离职工作交接通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2305.getCode(), MsgTemplateTypeEnum.DIMISSION.getCode());// 离职工作交接完成通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_2306.getCode(), MsgTemplateTypeEnum.DIMISSION.getCode());// 离职审批拒接通知
		// 住宿类通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6301.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 外宿申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6302.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 退宿申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6303.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 退宿申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6304.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 退宿申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6305.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 退宿申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_6306.getCode(), MsgTemplateTypeEnum.ROOM_CODE.getCode());// 退宿申请通过

		// 其他通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_7301.getCode(), MsgTemplateTypeEnum.VACATE_CODE.getCode());// 请假申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_7302.getCode(), MsgTemplateTypeEnum.VACATE_CODE.getCode());// 请假申请退回
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_8301.getCode(), MsgTemplateTypeEnum.REST_CODE.getCode());// 调休申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_8302.getCode(), MsgTemplateTypeEnum.REST_CODE.getCode());// 调休申请退回
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_9301.getCode(), MsgTemplateTypeEnum.EXTRAWORK_CODE.getCode());// 加班申请通过"
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_9302.getCode(), MsgTemplateTypeEnum.EXTRAWORK_CODE.getCode());// 加班申请退回"
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_9303.getCode(), MsgTemplateTypeEnum.TRAVEL_CODE.getCode());//出差申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_9304.getCode(), MsgTemplateTypeEnum.TRAVEL_CODE.getCode());//出差申请退回
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_10301.getCode(), MsgTemplateTypeEnum.ATTENDANCE_CODE.getCode());// 补卡申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_10302.getCode(), MsgTemplateTypeEnum.ATTENDANCE_CODE.getCode());// 考勤异常通知
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_10303.getCode(), MsgTemplateTypeEnum.ATTENDANCE_CODE.getCode());// 补卡申请退回

		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_10503.getCode(), MsgTemplateTypeEnum.BADGE_CODE.getCode());//厂牌补领申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_PUSH_10504.getCode(), MsgTemplateTypeEnum.BADGE_CODE.getCode());//厂牌补领申请退回

		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_RELEASE_10602.getCode(), MsgTemplateTypeEnum.ARTICLES_CODE.getCode());//物品放行申请通过
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_RELEASE_10603.getCode(), MsgTemplateTypeEnum.ARTICLES_CODE.getCode());//物品放行申请退回
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_RELEASE_10604.getCode(), MsgTemplateTypeEnum.ARTICLES_CODE.getCode());//物品放行申请通知

		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_APPEAL_10701.getCode(), MsgTemplateTypeEnum.APPEAL_CODE.getCode());
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_APPEAL_10702.getCode(), MsgTemplateTypeEnum.APPEAL_CODE.getCode());

		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_REPAIR_10801.getCode(), MsgTemplateTypeEnum.PARK_WARRANTY.getCode());
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_REPAIR_10802.getCode(), MsgTemplateTypeEnum.PARK_WARRANTY.getCode());
		APP_MSG_BUS_MAP.put(SmsTemplateEnum.APP_REPAIR_10803.getCode(), MsgTemplateTypeEnum.PARK_WARRANTY.getCode());
	}

	@Override
	public IPage<SmtMsgRecord> queryAppMsgList(Page<?> page, QueryAppMsgRecDTO queryAppMsgRecDTO) {
		// 开始时间为空，则查询三个月前的记录
		if (Objects.isNull(queryAppMsgRecDTO.getStartTime())) {
			queryAppMsgRecDTO.setStartTime(DateUtil.offsetMonth(new Date(), -3));
		}
		return msgRecordService.listAppMsgByPage(page, queryAppMsgRecDTO);
	}

	/**
	 * App消息推送基础方法
	 *
	 * @param appMsgPushDTO App消息推送类
	 */
	@Override
	@SneakyThrows
	public Boolean pushAppMsg(AppMsgPushDTO appMsgPushDTO) {
		Boolean isSuccess = false;

		if (Objects.isNull(appMsgPushDTO) || StringUtils.isEmpty(appMsgPushDTO.getBadge())
				|| StringUtils.isEmpty(appMsgPushDTO.getTemplateCode())) {

			log.info("参数不全，停止推送，appMsgPushDTO={}", appMsgPushDTO);
			return isSuccess;
		}

		String badge = appMsgPushDTO.getBadge();
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(badge);
		if (Objects.isNull(smtStaff)) {
			log.error("未找到员工信息,badge={}", badge);
			return isSuccess;
		}

		String empName = smtStaff.getName();
		String url = StringUtils.isEmpty(appMsgPushDTO.getUrl()) ? "http://www.szyuto.com/cn"
				: appMsgPushDTO.getUrl();
		String tempCode = appMsgPushDTO.getTemplateCode();// App消息推送模板编号
		Integer businessType = APP_MSG_BUS_MAP.get(tempCode); // 业务类型

		// 扩展参数
		StringBuilder bussinessParam = new StringBuilder();
		bussinessParam.append(BUSINESS_TYPE).append('=').append(businessType).append("||").append(BUSINESS_ID)
				.append('=').append(appMsgPushDTO.getBussiessId());
		if (!StringUtils.isEmpty(appMsgPushDTO.getExtraParam())) {
			bussinessParam.append("||").append(appMsgPushDTO.getExtraParam());
		}
		String extraParam = bussinessParam.toString();

		Result<List<AppUserDevice>> appDeviceRs = remoteAppDeviceService.queryUserDevice(badge);
		if (appDeviceRs.isSuccess() && CollectionUtils.isNotEmpty(appDeviceRs.getData())) {

			SmtMsgTemplate smtMsgTemplate = smtMsgTemplateService.selectByTempCode(tempCode);
			if (Objects.isNull(smtMsgTemplate)) {
				throw new TCEException(ExceptionType.SERVER_ERROR.getCode(), "未配置消息模板");
			}

			String title = smtMsgTemplate.getTempName();// 标题
			String content = "";// 内容
			if (SmsTemplateEnum.APP_PUSH_10302.getCode().equals(tempCode)) {
				content = appMsgPushDTO.getContent();
			} else {
				content = smtMsgTemplate.getTempContent();// 内容
			}

			for (AppUserDevice tempDevcie : appDeviceRs.getData()) {
				if (StringUtils.isEmpty(tempDevcie.getDevicePushId())) {
					log.info("用户设备devicePushId为空，停止推送,badge={},deviceNo={}", tempDevcie.getBadge(),
							tempDevcie.getDeviceNo());
				}

				SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
				smtMsgRecord.setTempId(smtMsgTemplate.getId());
				smtMsgRecord.setTempName(smtMsgTemplate.getTempName());
				smtMsgRecord.setMsgObject(tempDevcie.getBadge() + "-" + tempDevcie.getDeviceNo());
				smtMsgRecord.setMsgContent(content);
				String remark1 = smtMsgTemplate.getRemark1();
				if (StringUtils.isNotEmpty(remark1)) {
					if (StringUtils.isNotEmpty(appMsgPushDTO.getApplicant())) {
						remark1 = remark1.replace("{name}", appMsgPushDTO.getApplicant());//通知
					} else {
						remark1 = remark1.replace("{name}", empName);//通过或退回
					}
				}
				smtMsgRecord.setRemark1(remark1);
				smtMsgRecord.setRemark2(extraParam);// 业务参数
				smtMsgRecord.setMsgState(SmsRecordSateEnum.INIT.getCode());
				smtMsgRecord.setCreateTime(LocalDateTime.now());

				PushMessageDTO pushMessageDTO = new PushMessageDTO();
				pushMessageDTO.setTitle(title);
				pushMessageDTO.setContent(content);
				pushMessageDTO.setPayload(extraParam);
				pushMessageDTO.setUrl(url);

				sendRemotePush(smtMsgRecord, pushMessageDTO, tempDevcie.getOsType(), tempDevcie.getDevicePushId());
			}
			isSuccess = true;
		} else {
			log.info("未找到用户设备信息，停止推送,badge={},appDeviceRs={}", badge, appDeviceRs);
		}

		return isSuccess;
	}

	@Override
	public Boolean changeRecordToRead(Integer recordId) {
		return msgRecordService.updateRecordToRead(recordId);
	}


	@Override
	public QueryAppMsgRecVO countAppMsg(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		QueryAppMsgRecVO queryAppMsgRecVO = new QueryAppMsgRecVO();

		// 开始时间为空，则查询三个月前的记录
		if (Objects.isNull(queryAppMsgRecDTO.getStartTime())) {
			queryAppMsgRecDTO.setStartTime(DateUtil.offsetMonth(new Date(), -3));
		}

		queryAppMsgRecVO.setTotal(msgRecordService.countAppMsgSuccess(queryAppMsgRecDTO));
		queryAppMsgRecVO.setReadTotal(msgRecordService.countAppMsgRead(queryAppMsgRecDTO));
		queryAppMsgRecVO.setUnReadTotal(msgRecordService.countAppMsgUnRead(queryAppMsgRecDTO));

		return queryAppMsgRecVO;
	}

	/**
	 * 调用远程推送服务
	 *
	 * @param smtMsgRecord   消息推送记录
	 * @param pushMessageDTO 推送内容
	 * @param osType         系统操作类型
	 * @param devicePushId   设备消息推送ID
	 * @return
	 */
	private void sendRemotePush(SmtMsgRecord smtMsgRecord, PushMessageDTO pushMessageDTO, Integer osType,
								String devicePushId) {

		String sendDesc = null;
		Integer sendState = SmsRecordSateEnum.FAILD.getCode();

		// 推送前添加记录
		Integer pk = msgRecordService.addRecord(smtMsgRecord);
		Result<?> remotePushRs = null;
		try {
			// 安卓设备推送
			if (DeviceOSTypeEnum.ANDROID.getCode().equals(osType)) {
				NoticeMessageDTO noticeMessageDTO = new NoticeMessageDTO();
				BeanUtils.copyProperties(pushMessageDTO, noticeMessageDTO);
				noticeMessageDTO.setClientId(devicePushId);// 设备token

				log.info("noticeMessageDTO====req====={}", JSONUtil.toJsonPrettyStr(noticeMessageDTO));
				remotePushRs = remotePushService.notice(noticeMessageDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

			}
			// IOS设备推送
			else if (DeviceOSTypeEnum.IOS.getCode().equals(osType)) {
				ApnsMessageDTO apnsMessageDTO = new ApnsMessageDTO();
				BeanUtils.copyProperties(pushMessageDTO, apnsMessageDTO);
				apnsMessageDTO.setDeviceToken(devicePushId);

				log.info("ApnsMessageDTO=====req===={}", JSONUtil.toJsonPrettyStr(apnsMessageDTO));
				remotePushRs = remotePushService.transmission(apnsMessageDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			}

		} catch (TCEException tce) {
			sendState = SmsRecordSateEnum.FAILD.getCode();
			sendDesc = tce.getMessage();
			log.error("向设备推送消息失败,receive={}", smtMsgRecord.getMsgObject(), tce);
		} catch (Exception e) {
			sendState = SmsRecordSateEnum.FAILD.getCode();
			sendDesc = "内部错误";
			log.error("向设备推送消息失败,receive={}", smtMsgRecord.getMsgObject(), e);
		} finally {
			if (Objects.isNull(remotePushRs)) {
				sendState = SmsRecordSateEnum.FAILD.getCode();
				sendDesc = "内部错误";
			} else {
				if (remotePushRs.isSuccess()) {
					sendState = SmsRecordSateEnum.SUCCESS.getCode();
				} else {
					if (!remotePushRs.isSuccess() && StringUtils.isEmpty(sendDesc)) {
						sendState = SmsRecordSateEnum.FAILD.getCode();
						sendDesc = "内部错误";
					}
				}
			}
			try {
				// 更新推送状态
				msgRecordService.updateRecordState(pk, sendState, sendDesc);
			} catch (Exception e) {
				log.error("更新推送状态失败", e);
			}
		}

		log.info("remotePushRs===receiver={}=====RS={}", smtMsgRecord.getMsgObject(), remotePushRs);

	}

	@Override
	public Boolean deleteMsg(Integer recordId) {
		return msgRecordService.deleteMsg(recordId);
	}


	@Override
	public List<MsgStateVO> getState() {
		List<MsgStateVO> list = new ArrayList<>();
		for (SmsRecordSateEnum temp : SmsRecordSateEnum.values()) {
			MsgStateVO resp = new MsgStateVO();
			resp.setStateId(temp.getCode());
			resp.setState(temp.getDesc());
			list.add(resp);
		}
		list.remove(0);

		return list;
	}

	/**
	 * 根据id获得信息
	 *
	 * @param id
	 * @return
	 */
	@Override
	public SmtMsgRecord getMsgById(Integer id) {
		if (Objects.nonNull(id)) {
			return msgRecordService.getById(id);
		}
		return null;
	}

	@Override
	public Boolean changeAllRecordToRead(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		// TODO Auto-generated method stub
		return msgRecordService.updateAllRecordToRead(queryAppMsgRecDTO);
	}

	@Override
	public Boolean deleteAllMsg(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		// TODO Auto-generated method stub
		return msgRecordService.deleteAllMsg(queryAppMsgRecDTO);
	}


}
