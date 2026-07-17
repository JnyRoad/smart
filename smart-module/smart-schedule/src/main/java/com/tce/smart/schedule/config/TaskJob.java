package com.tce.smart.schedule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Li.JiaJun
 * @since 2022/8/11 11:05
 */
@Data
@Component
@ConfigurationProperties(prefix = "task.job")
public class TaskJob {

	private Boolean deviceStatus;
	private Boolean syncDhrFaceImg;
	private Boolean syncStaff;
	private Boolean syncDhrStaff;
	private Boolean syncXcDhrFaceImg;
	private Boolean staffNoPhotoSyncXc;
	private Boolean staffNoPhotoSync;
	private Boolean leaveReason;
	private Boolean leaveType;
	private Boolean leaveLeaintent;
	private Boolean visitorType;
	private Boolean admittanceRemind;
	private Boolean admittanceUpdateOa;
	/** 保密门禁申请OA审批状态对账开关（spec §3.1.6，PR2 补偿定时任务，Nacos 默认关） */
	private Boolean securityAuthUpdateOa;
	/** 保密区权限异步下发 worker 开关，生产发布时默认关闭。 */
	private Boolean securityAuthDispatchProcess;
	/** OA回调日志90天过期清理开关（spec 2026-07-05 §3.2，Nacos 默认关） */
	private Boolean oaCallbackLogClean;
	private Boolean admittanceComeOntime;
	private Boolean admittanceOvertime;
	private Boolean visitorNoLeave;
    private Boolean newStaffRecharge;
	private Boolean newsTerminalCheck;
	private Boolean seniorStaffRecharge;
	private Boolean outdormitoryType;
	private Boolean dormitoryDealyQuit;
	private Boolean pushVisitorEmail;
	private Boolean replaceType;
	private Boolean refreshRecruit;
	private Boolean refreshComp;
	private Boolean wageSignInfo;
	private Boolean attendanceSignInfo;
	private Boolean ehrSetSendMsg;
	private Boolean logisticsAppointmentType;
	private Boolean iscDeviceTypeDownCard;
	private Boolean iscAuthProcessHandle;
	private Boolean iscAuthResultHandle;
	private Boolean iscAutoAuthResultHandle;
	private Boolean iscDeviceTypeDelCard;
	private Boolean iscDeviceSync;
	private Boolean iscPersonFaceRetry;
	private Boolean iscCardTaskSync;
	private Boolean iscTemperatureGet;
	private Boolean iscDeviceOfflineHandler;
	private Boolean deviceTypeDownCard;
	private Boolean deviceTypeDownCar;
	private Boolean deviceTypeDelCard;
	private Boolean deviceTypeDelCar;
	private Boolean deviceTaskRepeat;
	private Boolean supplierNotify;
	private Boolean admittanceOaAreaType;
	private Boolean supplierAutoAuthDelete;
	private Boolean supplierAuthMsg;
	private Boolean leaveApplicationProcessType;
	private Boolean smartMeterStatus;
	private Boolean waterMeterReading;
	private Boolean eleMeterReading;
	private Boolean genSettlementDaily;
}
