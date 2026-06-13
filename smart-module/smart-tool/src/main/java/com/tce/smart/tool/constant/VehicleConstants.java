package com.tce.smart.tool.constant;

/**
 * 车辆常量
 * @author Lenovo
 *
 */
public interface VehicleConstants {

	/******车辆归属类型******/
	/**
	 * 园区
	 */
	Integer PARK = 0;
	/**
	 * 员工
	 */
	Integer STAFF = 1;
	/**
	 * 非员工
	 */
	Integer NOT_STAFF = 4;
	/**
	 * 访客
	 */
	Integer VISITOR = 2;
	/**
	 * 物流车
	 */
	Integer LOGISTICSAPPOINTMENT = 3;

	/******删除标识******/
	/**
	 * 未删除
	 */
	Integer UNDELETED = 0;
	/**
	 * 已删除
	 */
	Integer DELETED = 1;

	String BASE64_REGEX = "data:image/.*;base64,";

	String BASE64_REGEX_PNG = "data:image/png;base64,";

	String BASE64_PREFIX = "data:image/jpeg;base64,";

	String BASE64_PDF = "data:application/pdf;base64,";

}
