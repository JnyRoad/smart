package com.tce.smart.platform.service.impl;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.data.api.dto.msg.req.AppointmentMsgReqDTO;
import com.tce.smart.data.api.dto.msg.req.EmailReqDTO;
import com.tce.smart.data.api.dto.msg.req.EmailFileReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteEmailManagerService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时任务的接口
 *
 * @author 梁圆
 * @date 2019-05-15 11:34:58
 */
@Slf4j
@Service
public class VisitorTaskServiceImpl extends ServiceImpl<SmtVisitorMapper, SmtVisitor> implements VisitorTaskService {

	@Autowired
	private SmtVisitorService visitorService;

	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Autowired
	private SmtDeviceAuthorityRelationService deviceAuthorityRelationService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Autowired
	private SmtFellowVisitorService smtFellowVisitorService;

	@Autowired
	private ApproveListService approveListService;

	@Autowired
	private SmtVisitorProcessRecordService smtVisitorProcessRecordService;

	@Autowired
	private SmtVisitorPushEamilService visitorPushEamilService;

	@Autowired
	private SmtStaffService staffService;


	@Autowired
	private SmtSnapPersonService smtSnapPersonService;

	@Autowired
	private SmtParkService smtParkService;

	@Autowired
	private RemoteEmailManagerService emailManagerService;

	@Autowired
	private SmtNoticeSwitchService smtNoticeSwitchService;

	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";

	@Value("${spring.visitor.arrived-offset-hour:0}")
	private Integer arrivedOffsetHour;
	@Value("${spring.visitor.put-offset-hour:2}")
	private Integer putOffsetHour;
	@Value("${spring.visitor.arrived-send:true}")
	private Boolean arrivedSend;
	@Value("${spring.visitor.arrived-send-message-offset-minute:30}")
	private Integer arrivedSendMessageOffsetMinute;
	@Value("${spring.visitor.pre-send:true}")
	private Boolean preSend;
	@Value("${spring.visitor.pre-send-message-offset-minute:30}")
	private Integer preSendMessageOffsetMinute;
	@Value("${spring.visitor.push-email}")
	private String pushEmailUrl;


	/**
	 * 访客是否超时。
	 * <p>
	 * 超时判定必须用"预约结束时间已过"（end_time &lt; now），不允许叠加提前量：
	 * 历史上这里加了 overtime-offset-hour（生产配 24 小时）提前量，会把仍在 OA 审批中的
	 * 短期访客单提前终态化为"预约超时"，此后 OA 回调与拉取对账（都只认待审核状态）
	 * 永远无法落审批结果（与入厂申请侧 2026-07-07 生产事故同根因）。
	 */
	@Override
	public void visitorOverTime(Integer parkId) {
		//查询已通过(0)且预约结束时间已过的访客：置为超时未到(4)
		List<SmtVisitor> selectList = visitorService.list(Wrappers.<SmtVisitor>query().lambda()
				.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_0.getCode()).eq(SmtVisitor::getParkId, parkId)
				.lt(SmtVisitor::getEndTime, DateUtil.date())
		);
		removeVisitor(selectList);
		//查询待审核(2)且预约结束时间已过的访客：置为预约超时(6)
		List<SmtVisitor> selectListNoPass = visitorService.list(
				Wrappers.<SmtVisitor>query().lambda()
						.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode()).eq(SmtVisitor::getParkId, parkId)
						.lt(SmtVisitor::getEndTime, DateUtil.date())
		);
		updateNoPass(selectListNoPass);


	}

	/**
	 * 预约审批超时：把过期仍待审核(2)的访客置为预约超时(6)。
	 * CAS 抢占（仅当行仍为待审核时生效）——与 OA 回调/拉取对账的 claim 并发时，
	 * 无条件写会把刚落库的审批结果改回超时。CAS 未命中整单跳过（含待办与流程记录更新）。
	 */
	private void updateNoPass(List<SmtVisitor> selectListNoPass) {
		for (SmtVisitor v : selectListNoPass) {
			//CAS：仅当行仍为待审核(2)时置为预约超时(6)
			boolean claimed = this.update(Wrappers.<SmtVisitor>lambdaUpdate()
					.eq(SmtVisitor::getId, v.getId())
					.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_2.getCode())
					.set(SmtVisitor::getStatus, VisitorStatusEnum.CAUSE_6.getCode()));
			if (!claimed) {
				continue;
			}

			/**
			 * 修改待我审批里的状态
			 */
			List<ApproveList> selectListApprove = approveListService.list(Wrappers.<ApproveList>query().lambda().eq(ApproveList::getBusinessId, v.getId()).eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR));
			List<SmtVisitorProcessRecord> processList = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query()
					.lambda().eq(SmtVisitorProcessRecord::getVisitorId, v.getId())
					.eq(SmtVisitorProcessRecord::getStatus, VisitorProcessEnum.WATING_2.getCode()));
			if (CollectionUtils.isNotEmpty(processList)) {
				processList.forEach(process -> {
					process.setStatus(VisitorProcessEnum.WATING_3.getCode());
					process.setStatusName(VisitorProcessEnum.WATING_3.getDesc());
					smtVisitorProcessRecordService.updateById(process);
				});
			}
			if (selectListApprove.size() > 0) {
				ApproveList approveList = new ApproveList();
				approveList.setBusinessId(v.getId().toString());
				approveList.setApproveState(VisitorStatusEnum.CAUSE_6.getCode());
				approveList.setApproveType(ApproveListTypeConstants.VISITOR);
				approveList.setApproveBadge(v.getReceptionistBadge());
				approveListService.updateState(approveList);
			}
		}
	}

	/**
	 * 访客已到达，判断截止时间是否等于当天的时间
	 */
	public void visitorComeOnTime() {
		//查询状态为3的访客，判断是否已经超时
		List<SmtVisitor> selectList = visitorService.list(
				Wrappers.<SmtVisitor>query().lambda()
						.eq(SmtVisitor::getStatus, SmtVisitorEnum.COME_STATUS.getType())
						.ge(SmtVisitor::getEndTime, DateUtil.beginOfDay(DateUtils.date()))
						.le(SmtVisitor::getEndTime, DateUtil.endOfDay(DateUtils.date()))
		);
		deleteVisitor(selectList);
	}

	/**
	 * 添加删除已到达的访客的信息任务
	 *
	 * @param selectList
	 */
	private void deleteVisitor(List<SmtVisitor> selectList) {
		if (CollectionUtils.isNotEmpty(selectList)) {
			selectList.forEach(v -> {
				addDeleteTaskVisitor(v);
			});
		}
	}

	/**
	 * 修改超时未到的访客：把过期仍为已通过(0)的访客置为超时未到(4)。
	 * CAS 抢占（仅当行仍为已通过时生效），避免与到场登记等并发写入互相覆盖；
	 * CAS 未命中整单跳过。
	 *
	 * @param selectList 过期仍为已通过的访客列表
	 */
	private void removeVisitor(List<SmtVisitor> selectList) {
		if (CollectionUtils.isNotEmpty(selectList)) {
			selectList.forEach(v -> {
				//CAS：仅当行仍为已通过(0)时置为超时未到(4)
				boolean claimed = this.update(Wrappers.<SmtVisitor>lambdaUpdate()
						.eq(SmtVisitor::getId, v.getId())
						.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_0.getCode())
						.set(SmtVisitor::getStatus, VisitorStatusEnum.CAUSE_4.getCode()));
				if (!claimed) {
					return;
				}

				/**
				 * 修改待我审批里的状态
				 */
				List<ApproveList> selectListApprove = approveListService.list(Wrappers.<ApproveList>query().lambda().eq(ApproveList::getBusinessId, v.getId()).eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR));
				if (selectListApprove.size() > 0) {
					ApproveList approveList = new ApproveList();
					approveList.setBusinessId(v.getId().toString());
					approveList.setApproveState(VisitorStatusEnum.CAUSE_4.getCode());
					approveList.setApproveType(ApproveListTypeConstants.VISITOR);
					approveList.setApproveBadge(v.getReceptionistBadge());
					approveListService.updateState(approveList);
				}
				/*addDeleteTaskVisitor(v);*/
			});
		}
	}

	/**
	 * 添加删除定时任务
	 *
	 * @param smtVisitor
	 */
	private void addDeleteTaskVisitor(SmtVisitor smtVisitor) {
		if (!Objects.isNull(smtVisitor)) {

			//查询访客人员设备权限
			List<SmtDeviceAuthorityRelation> visitorDeviceList = deviceAuthorityRelationService.getRelationAuth(smtVisitor.getParkId(),
					BusinessAuthorityEnum.VISITOR_FACE.getCode(), DeviceAuthorityEnum.VISITOR);
			//查询访客车辆的的设备权限
			List<SmtDeviceAuthorityRelation> vehicleDeviceList = deviceAuthorityRelationService.getRelationAuth(smtVisitor.getParkId(),
					BusinessAuthorityEnum.VISITOR_VEHICLE.getCode(), DeviceAuthorityEnum.VISITOR_VEHICLE);
			//删除闸机,删除道闸
			addDeleteVisitor(smtVisitor, visitorDeviceList, vehicleDeviceList);
		}
	}

	/**
	 * 删除访客的闸机数据
	 *
	 * @param visitor
	 * @param visitorDeviceList
	 * @param vehicleDeviceList
	 */
	private void addDeleteVisitor(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> visitorDeviceList, List<SmtDeviceAuthorityRelation> vehicleDeviceList) {

		//添加访客设备权限
		deleteCard(visitor, visitorDeviceList);
		//添加随行人员设备权限
		deleteFellow(visitor, visitorDeviceList);
		//判断是否有车牌 有车牌下发道闸
		if (visitor.getIsVehicle().equals(SmtVisitorEnum.IS_VEHICLE.getType())) {
			deleteCarCard(visitor, vehicleDeviceList);
		}
	}

	/**
	 * 删除道闸
	 *
	 * @param visitor
	 * @param deviceAuthorityRelations
	 */
	private void deleteCarCard(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				//删除道闸
				deleteCarCard(
						visitor,
						d.getDeviceId()
				);
			});
		}
	}

	/**
	 * 删除道闸
	 *
	 * @param visitor
	 * @param deviceId
	 */
	private void deleteCarCard(SmtVisitor visitor, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DEL);
		deviceTaskVO.setCardNo(visitor.getId().toString());
		deviceTaskVO.setDeviceCode(deviceId);
		smtDeviceTaskService.saveTask(deviceTaskVO);

	}

	/**
	 * 随行人员删除闸机
	 *
	 * @param visitor
	 * @param visitorDeviceList
	 */
	private void deleteFellow(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> visitorDeviceList) {
		//判断是否有随行人员
		List<GetSmtFellowVisitorVO> fellowList = getFellowPerson(visitor.getId());
		if (CollectionUtils.isNotEmpty(fellowList)) {
			fellowList.forEach(f -> deleteCard(f, visitorDeviceList));
		}
	}

	/**
	 * 访客的随行人员删除闸机
	 *
	 * @param fellowVisitorVO
	 * @param deviceAuthorityRelations
	 */
	private void deleteCard(GetSmtFellowVisitorVO fellowVisitorVO, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				deleteCard(
						fellowVisitorVO,
						d.getDeviceId()
				);
			});
		}
	}

	/**
	 * 访客随行人员删除闸机
	 *
	 * @param deviceId
	 */
	private void deleteCard(GetSmtFellowVisitorVO fellowVisitorVO, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DEL);
		deviceTaskVO.setCardNo(fellowVisitorVO.getId().toString());
		deviceTaskVO.setDeviceCode(deviceId);
		smtDeviceTaskService.saveTask(deviceTaskVO);
	}

	/**
	 * 访客删除闸机
	 *
	 * @param visitor
	 * @param deviceAuthorityRelations
	 */
	private void deleteCard(SmtVisitor visitor, List<SmtDeviceAuthorityRelation> deviceAuthorityRelations) {
		if (CollectionUtils.isNotEmpty(deviceAuthorityRelations)) {
			deviceAuthorityRelations.forEach(d -> {
				deleteCard(visitor, d.getDeviceId());
			});
		}
	}

	/**
	 * 访客删除闸机
	 *
	 * @param visitor
	 * @param deviceId
	 */
	private void deleteCard(SmtVisitor visitor, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DEL);
		deviceTaskVO.setCardNo(visitor.getId().toString());
		deviceTaskVO.setDeviceCode(deviceId);
		smtDeviceTaskService.saveTask(deviceTaskVO);
	}

	/**
	 * 未到达的访客，和没有发过短信的访客发送短信
	 */
	@Override
	public void visitorRemind(Integer parkId) {
		if (preSend) {
			//查询配置文件
			SmtNoticeSwitch noticeSwitch = smtNoticeSwitchService.getOne(Wrappers.<SmtNoticeSwitch>query().lambda().eq(SmtNoticeSwitch::getSwitchCode, ParkNoticeTypeEnum.VISITOR_ARRIVE.getCode()).eq(SmtNoticeSwitch::getParkId, parkId).eq(SmtNoticeSwitch::getIsOn, 1));
			if (ObjectUtil.isNotNull(noticeSwitch)) {
				//查询状态为0的访客，且没有发送过短信的访客信息
				List<SmtVisitor> selectList = visitorService.list(
						Wrappers.<SmtVisitor>query().lambda()
								.eq(SmtVisitor::getParkId, parkId)
								.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_0.getCode())
								.eq(SmtVisitor::getIsSend, SmtVisitorEnum.NOT_IS_SEND.getType())
								.ge(SmtVisitor::getStartTime, DateUtil.date())
								.le(SmtVisitor::getStartTime, DateUtils.offsetMinute(DateUtil.date(), +noticeSwitch.getBeforeTime())));
				log.info("未到达的访客, 提前短信通知：{}", selectList);
				if (CollectionUtils.isNotEmpty(selectList)) {
					selectList.forEach(v -> {
						Long between = DateUtils.between(v.getStartTime(), DateUtil.date(), DateUnit.MINUTE);
						boolean send = sendMessage(v, SmsTemplateEnum.VISIT_1003, between.intValue());
						if (send) {
							v.setIsSend(SmtVisitorEnum.IS_SEND.getType());
							v.updateById();
						}
					});
				}
			}
		}
		//todo 预约时间快结束时发送短信处理逻辑
	}

	/**
	 * @Title:查询是否有随行人员
	 * @Param :
	 * @Exception :
	 * @Return :List<SmtFellowVisitor>
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午11:42:27
	 */
	private List<GetSmtFellowVisitorVO> getFellowPerson(Long visitorId) {
		SmtVisitor smtVisitor = new SmtVisitor();
		smtVisitor.setId(visitorId);
		List<GetSmtFellowVisitorVO> fellowVisitorList = smtFellowVisitorService.selectListByVisitorId(smtVisitor);
		return fellowVisitorList;
	}

	/**
	 * 发送短信通知
	 *
	 * @param visitor
	 * @param template
	 * @param noticeTime
	 */
	private boolean sendMessage(SmtVisitor visitor, SmsTemplateEnum template, Integer noticeTime) {
		//给访客发送短信,调用短信发送接口
		AppointmentMsgReqDTO appointmentMsgAo = new AppointmentMsgReqDTO();
		appointmentMsgAo.setNumber(visitor.getVisitorPhone());
		appointmentMsgAo.setVisitorName(visitor.getVisitorName());
		appointmentMsgAo.setTempCode(template.getCode());
		appointmentMsgAo.setHostName(visitor.getReceptionistName());
		appointmentMsgAo.setAppointmentDate(DateUtils.formatDateTime(visitor.getStartTime()));
		appointmentMsgAo.setNoticeTime(String.valueOf(noticeTime));
		Result result = remoteSmsManageService.sendAppointmentSms(appointmentMsgAo);
		return result.getCode() == CommonConstants.SUCCESS;
	}

	/**
	 * 推送访客信息给指定email
	 */
	@Override
	public void toEmail() {
		// TODO Auto-generated method stub
		//查询出所有的园区
		List<SmtPark> parkList = smtParkService.list();
		List<Integer> status = new ArrayList<Integer>() {{
			add(VisitorStatusEnum.Status_3.getCode());
			add(VisitorStatusEnum.Status_0.getCode());
			add(VisitorStatusEnum.CAUSE_5.getCode());
		}};
		for (SmtPark smtPark : parkList) {
			List<SmtVisitorPushEamil> searchAll = visitorPushEamilService.list(Wrappers.<SmtVisitorPushEamil>query().lambda().eq(SmtVisitorPushEamil::getParkId, smtPark.getId()));
			List<SmtVisitor> list = new ArrayList<>();
			String fileNameAdd = "";
			//是否需要发送邮件
			Boolean flag = Boolean.FALSE;
			if (CollectionUtils.isEmpty(searchAll)) {
				continue;
			}
			Integer type = searchAll.get(0).getType();
			if (PushVistorEnum.PSHT_DAY.getCode().equals(type)) {
				//2.如果周期是每天，查询出昨天的所有的访客
				list = visitorService.list(Wrappers.<SmtVisitor>query().lambda()
						.in(SmtVisitor::getStatus, status)
						.eq(SmtVisitor::getParkId, smtPark.getId())
						.ge(SmtVisitor::getCreateTime, DateUtil.beginOfDay(DateUtils.yesterday()))
						.le(SmtVisitor::getCreateTime, DateUtil.endOfDay(DateUtils.yesterday())));
				fileNameAdd = DateUtil.format(DateUtil.beginOfDay(DateUtils.yesterday()), "yyyy-MM-dd");
				flag = Boolean.TRUE;
				log.info("访客定时推送邮件信息, 周期是每天:{}", list);
			}

			if (PushVistorEnum.PSHT_WEEK.getCode().equals(type)) {
				//3.如果周期是每周，判断今天是不是周一，如果是周一查询出上周的访客
				if (DateUtil.dayOfWeek(DateUtils.date()) == 2) {
					list = visitorService.list(Wrappers.<SmtVisitor>query().lambda()
							.in(SmtVisitor::getStatus, status)
							.eq(SmtVisitor::getParkId, smtPark.getId())
							.ge(SmtVisitor::getCreateTime, DateUtil.beginOfWeek(DateUtils.lastWeek()))
							.le(SmtVisitor::getCreateTime, DateUtil.endOfWeek(DateUtils.lastWeek())));
					fileNameAdd = DateUtil.format(DateUtil.beginOfWeek(DateUtils.lastWeek()), "yyyy-MM-dd") + "-" + DateUtil.format(DateUtil.endOfWeek(DateUtils.lastWeek()), "yyyy-MM-dd");
					flag = Boolean.TRUE;
				}
			}

			if (type.equals(PushVistorEnum.PSHT_MONTH.getCode())) {
				//4.如果周期是每月，判断今天是不是1号，如果是1号查询上月的访客
				if (DateUtil.dayOfMonth(DateUtils.date()) == 1) {
					list = visitorService.list(Wrappers.<SmtVisitor>query().lambda()
							.in(SmtVisitor::getStatus, status)
							.eq(SmtVisitor::getParkId, smtPark.getId())
							.ge(SmtVisitor::getCreateTime, DateUtil.beginOfMonth(DateUtils.lastMonth()))
							.le(SmtVisitor::getCreateTime, DateUtil.endOfMonth(DateUtils.lastMonth())));
					fileNameAdd = DateUtil.format(DateUtil.beginOfMonth(DateUtils.lastMonth()), "yyyy-MM-dd") + "-" + DateUtil.format(DateUtil.endOfMonth(DateUtils.lastMonth()), "yyyy-MM-dd");
					flag = Boolean.TRUE;
				}
			}
			if (!flag) {
				continue;
			}
			log.info("园区" + smtPark.getParkName() + ",预约的并到达的访客个数：" + list.size());
			List<Map> listMap = new ArrayList<Map>();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (SmtVisitor smtVisitor : list) {
				Map<String, String> dataMap = new HashMap<String, String>();
				SmtStaff selectOne = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtVisitor.getReceptionistBadge()));

				dataMap.put("访客姓名", smtVisitor.getVisitorName());
				dataMap.put("访客手机号", smtVisitor.getVisitorPhone());
				dataMap.put("车牌号", smtVisitor.getVehiclePlate() == null ? "" : smtVisitor.getVehiclePlate());
				dataMap.put("所属单位", smtVisitor.getCompany() == null ? "" : smtVisitor.getCompany());
				dataMap.put("来访状态", VisitorStatusEnum.desc(smtVisitor.getStatus()));
				dataMap.put("预计来访时间", format.format(smtVisitor.getStartTime()));
				dataMap.put("预计离开时间", format.format(smtVisitor.getEndTime()));
				dataMap.put("被访人工号", smtVisitor.getReceptionistBadge());
				dataMap.put("被访人姓名", smtVisitor.getReceptionistName());
				dataMap.put("被访人手机", smtVisitor.getReceptionistPhone());
				dataMap.put("被访人BU", selectOne.getCompName());
				dataMap.put("被访人部门", selectOne.getDepName());
				dataMap.put("被访人岗位", selectOne.getJobName());
				listMap.add(dataMap);
			}
			log.info("pushEmailUrl:" + pushEmailUrl);
			writeExcel(listMap, list.size(), pushEmailUrl);
			File file = new File(pushEmailUrl);
			EmailReqDTO sendEmailAo = new EmailReqDTO();
			List<String> emails = new ArrayList<>();
			for (SmtVisitorPushEamil emailsL : searchAll) {
				emails.add(emailsL.getEmail());
			}
			sendEmailAo.setInboxs(emails);
			sendEmailAo.setTitle(smtPark.getParkName() + "-访客记录报表-" + fileNameAdd);
			sendEmailAo.setContent("");
			EmailFileReqDTO emailFileAo = new EmailFileReqDTO();
			emailFileAo.setFileName(file.getName());
			try {
				emailFileAo.setFileBytes(FileUtils.readFileToByteArray(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<EmailFileReqDTO> listFile = new ArrayList<>();
			listFile.add(emailFileAo);
			sendEmailAo.setFileData(listFile);
			Result<?> sendEmail = emailManagerService.sendEmailsWithContent(sendEmailAo);
			log.info("邮件发送接口：" + sendEmail);

		}
	}


	public void writeExcel(List<Map> dataList, int cloumnCount, String finalXlsxPath) {
		OutputStream out = null;
		try {
			// 获取总列数
			int columnNumCount = cloumnCount;
			// 读取Excel文档
			File finalXlsxFile = new File(finalXlsxPath);
			Workbook workBook = getWorkbok(finalXlsxFile);
			// sheet 对应一个工作页
			Sheet sheet = workBook.getSheetAt(0);
			/**
			 * 删除原有数据，除了属性列
			 */
			int rowNumber = sheet.getLastRowNum();    // 第一行从0开始算
			log.info("原始数据总行数，除属性列：" + rowNumber);
			for (int i = 1; i <= rowNumber; i++) {
				Row row = sheet.getRow(i);
				sheet.removeRow(row);
			}
			// 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			out = new FileOutputStream(finalXlsxPath);
			workBook.write(out);
			/**
			 * 往Excel中写新数据
			 */
			for (int j = 0; j < dataList.size(); j++) {
				// 创建一行：从第二行开始，跳过属性列
				Row row = sheet.createRow(j + 1);
				// 得到要插入的每一条记录
				Map dataMap = dataList.get(j);
				String visitorName = dataMap.get("访客姓名").toString();
				String VisitorPhone = dataMap.get("访客手机号").toString();
				String vehiclePlate = dataMap.get("车牌号").toString();
				String company = dataMap.get("所属单位").toString();
				String status = dataMap.get("来访状态").toString();
				String startTime = dataMap.get("预计来访时间").toString();
				String endTime = dataMap.get("预计离开时间").toString();
				String badge = dataMap.get("被访人工号").toString();
				String staffName = dataMap.get("被访人姓名").toString();
				String staffPhone = dataMap.get("被访人手机").toString();
				String staffBu = dataMap.get("被访人BU").toString();
				String staffDep = dataMap.get("被访人部门").toString();
				String staffJob = dataMap.get("被访人岗位").toString();
				for (int k = 0; k <= columnNumCount; k++) {
					// 在一行内循环
					Cell first = row.createCell(0);
					first.setCellValue(visitorName);
					Cell second = row.createCell(1);
					second.setCellValue(VisitorPhone);
					Cell third = row.createCell(2);
					third.setCellValue(vehiclePlate);
					Cell four = row.createCell(3);
					four.setCellValue(company);
					Cell five = row.createCell(4);
					five.setCellValue(status);
					Cell six = row.createCell(5);
					six.setCellValue(startTime);
					Cell seven = row.createCell(6);
					seven.setCellValue(endTime);
					Cell cell8 = row.createCell(7);
					cell8.setCellValue(badge);
					Cell cellEgit = row.createCell(8);
					cellEgit.setCellValue(staffName);
					Cell cell9 = row.createCell(9);
					cell9.setCellValue(staffPhone);
					Cell cell10 = row.createCell(10);
					cell10.setCellValue(staffBu);
					Cell cell11 = row.createCell(11);
					cell11.setCellValue(staffDep);
					Cell cell12 = row.createCell(12);
					cell12.setCellValue(staffJob);
				}
			}
			// 创建文件输出流，准备输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			out = new FileOutputStream(finalXlsxPath);
			workBook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("数据导出成功");
	}


	/**
	 * 判断Excel的版本,获取Workbook
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Workbook getWorkbok(File file) throws IOException {
		Workbook wb = null;
		FileInputStream in = new FileInputStream(file);
		wb = new XSSFWorkbook(in);
		return wb;
	}

	/**
	 * 超时未离开的访客要发短息
	 */
	@Override
	public void visitorOverTimeNoLeave(Integer parkId) {
		// TODO Auto-generated method stub
		//查询状态为3(已到达)的访客，判断是否已经超时
		List<SmtVisitor> selectList = visitorService.list(
				Wrappers.<SmtVisitor>query().lambda()
						.eq(SmtVisitor::getStatus, VisitorStatusEnum.Status_3.getCode()).eq(SmtVisitor::getParkId, parkId)
						.ne(SmtVisitor::getCause, VisitorEnum.CAUSE_5.getCode())
						.ge(SmtVisitor::getEndTime, DateUtils.offsetMinute(DateUtil.date(), -150))
						.le(SmtVisitor::getEndTime, DateUtils.offsetMinute(DateUtil.date(), -30))
		);
		log.info("visitorService.list selectList.count{}", selectList.size());
		log.info("visitorService.list selectList{}", selectList);
		for (SmtVisitor smtVisitor : selectList) {
			//要查询是规定时间内是否有出门
			List<SmtSnapPerson> list = smtSnapPersonService.list(
					Wrappers.<SmtSnapPerson>query().lambda().eq(SmtSnapPerson::getParkId, parkId)
							.eq(SmtSnapPerson::getPersonId, smtVisitor.getId())
							.eq(SmtSnapPerson::getEventType, VehicleEventTypEnum.OUT.getCode())
			);

			//给被访人发送短息,调用短信发送接口
			if ((Objects.isNull(list) || list.size() == 0)) {
				int noticeSwitch = smtNoticeSwitchService.count(Wrappers.<SmtNoticeSwitch>query().lambda()
						.eq(SmtNoticeSwitch::getSwitchCode, ParkNoticeTypeEnum.VISIT_OVERTIME_STAY.getCode())
						.eq(SmtNoticeSwitch::getIsOn, 1)
						.eq(SmtNoticeSwitch::getParkId, parkId));
				if (noticeSwitch > 0) {
					log.info("超时未离开的访客smtVisitor:" + smtVisitor);
					sendMessage(smtVisitor.getReceptionistPhone(), smtVisitor.getVisitorName(), SmsTemplateEnum.VISIT_1009.getCode(), smtVisitor.getReceptionistName(), DateUtils.formatDateTime(smtVisitor.getStartTime()), null, null, smtVisitor.getCompany());
				}
			}
		}
	}

	/**
	 * 发送短信通知
	 *
	 * @param number          number
	 * @param visitorName     visitorName
	 * @param tempCode        tempCode
	 * @param hostName        hostName
	 * @param appointmentDate appointmentDate
	 * @param realityDate     realityDate
	 * @param deviceName      deviceName
	 */
	public void sendMessage(String number, String visitorName, String tempCode, String hostName, String appointmentDate, String realityDate, String deviceName, String company) {
		//给访客发送短信,调用短信发送接口
		AppointmentMsgReqDTO appointmentMsgAo = new AppointmentMsgReqDTO();
		appointmentMsgAo.setNumber(number);
		appointmentMsgAo.setVisitorName(visitorName);
		appointmentMsgAo.setTempCode(tempCode);
		appointmentMsgAo.setHostName(hostName);
		appointmentMsgAo.setAppointmentDate(appointmentDate);
		appointmentMsgAo.setRealityDate(realityDate);
		appointmentMsgAo.setDeviceName(deviceName);
		appointmentMsgAo.setCompany(company);
		remoteSmsManageService.sendAppointmentSms(appointmentMsgAo);
	}


}
