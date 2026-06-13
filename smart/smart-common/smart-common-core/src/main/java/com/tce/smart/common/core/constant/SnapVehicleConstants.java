package com.tce.smart.common.core.constant;

/**
 * 车辆抓拍事件类型
 * @author Lenovo
 *
 */
public interface SnapVehicleConstants {


	/**
	 * 驶入标记
	 */
	Integer DIRECTION_IN = 1;

	/**
	 * 驶出标记
	 */
	Integer DIRECTION_OUT = 2;


    /**
   * 园区车辆
   */
   Integer PARK_VEHICLE = 1;
   /**
    * 员工车辆
    */
   Integer STAFF_VEHICLE = 2;
   /**
    * 访客车辆
    */
   Integer VISITOR_VEHICLE = 3;
   /**
    * 物流车辆
    */
   Integer LOGISTICS_APPOINTMENT_VEHICLE = 4;

   /**
    * 员工车辆
    */
   Integer STAFF_VEHICLE_VEHICLE = 5;


   /**
    * 员工
    */
   Integer STAFF_MASTER = 1;
   /**
    * 访客
    */
   Integer VISITOR_MASTER = 2;
   /**
    * 物流车车主
    */
   Integer LOGISTICS_APPOINTMENT_MASTER = 3;

   /**
    * 非员工
    */
   Integer NOT_STAFF_MASTER = 4;

   /**
    * 其他车辆
    */
   Integer OTHER_MASTER = 6;

   /**
    * 全部通行记录
    */
   Integer ALL = 1;
   /**
    * 当前停车
    */
   Integer CURRENT = 2;

}
