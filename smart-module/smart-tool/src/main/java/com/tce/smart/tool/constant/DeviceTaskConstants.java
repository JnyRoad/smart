package com.tce.smart.tool.constant;

/**
 * 设备任务
 * @author Lenovo
 *
 */
public interface DeviceTaskConstants {

	/**
	 * 下发
	 */
	Integer DOWN = 1;
	/**
	 * 删除
	 */
	Integer DEL = 2;

	/**
	 * 初始值
	 */
	Integer FAIL = 0;
	/**
	 * 下发成功
	 */
	Integer DOWN_SUCCESS = 1;

	/**
	 * 下发停止
	 */
	Integer DOWN_STOP = 2;

	/**
	 * 处理中
	 */
	Integer DOING = 3;

	/**
	 * 卡片
	 */
	Integer CARD = 1;

	/**
	 * 车辆
	 */
	Integer CAR = 2;

	/**
	 * 人员
	 */
	Integer PERSON = 3;

	Long maxTime = 33117041415L;

	/**
	 * 离线设备待处理任务自动取消周期（月）
	 */
	Integer OFFLINE_TASK_CANCEL_MONTHS = 6;

	/**
	 * ISC权限任务最大自动重试次数
	 */
	Integer AUTH_CONFIG_MAX_RETRY_TIMES = 10;

	/**
	 * 删除中
	 */
	Integer DELING = 1;
	/**
	 * 正常
	 */
	Integer NORMAL = 0;

//	人脸下发记录的业务类型

	// 修改员工通关权限
	Integer CARD_STAFF_IMPORT = 1;
	// APP信息完善
	Integer CARD_APP_PERFECT = 2;
	// 访客预约
	Integer CARD_VISITOR = 3;
	// 员工扫码登记
	Integer CARD_STAFF_SCAN = 4;
	// 招聘入职
	Integer CARD_RECRUIT = 5;
	//入厂申请
	Integer CARD_ADMITTANCE = 6;
	//修改人脸图片
	Integer UPDATE_FACE = 7;

//	车辆下发记录的业务类型
	// 员工车辆
	Integer CAR_STAFF = 1;
	// 公司车辆
	Integer CAR_COMPANY = 2;
	// 非员工车辆
	Integer CAR_NOT_STAFF = 3;
	// 访客预约
	Integer CAR_VISITOR = 4;
	// 物流车预约
	Integer CAR_GUARD = 5;

	Integer CAT_ADMITTANCE = 6;


}
