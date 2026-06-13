package com.tce.smart.platform.core.dto;



import lombok.Data;

import java.util.List;

/**
 * 查询内宿员工
 * @author 齐佩
 *
 */
@Data
public class StaffInDormitoryHistoryDTO  {


	    /**
	   * 员工名称
	   */
	    private String staffName;
	    /**
	   * 员工工号
	   */
	    private String staffBadge;
	    /**
	   * 园区id
	   */
	    private Integer parkId;

	    /**
	     * 性别 0-男 1-女
	     */
	    private Integer sex;

	    /**
	   * 宿舍楼id
	   */
	    private Integer dormitoryId;
	    /**
	   * 宿舍楼名称
	   */
	    private String dormitoryName;
	    /**
	   * 楼层id
	   */
	    private Integer floorId;

	    /**
	     * 房间id
	     */
	    private Integer roomId;

	    /**
	     * 部门id
	     */
	    private String depId;

	    /**
	     * 职层id
	     */
	    private String jcheId;



	    /**
	   * 宿舍类型id
	   */
	    private Integer dormitoryTypeId;


	  /**
	   * 入住开始时间
	   */
	    private  String inStartTime;

	    /**
		 * 入住结束时间
		 */
	    private  String inEndTime;


	    /**
		 * 退宿开始时间
		 */
	    private  String outStartTime;

		/**
		 * 退宿结束时间
		 */
		private  String outEndTime;

		/**
		 * 住宿类型  0-入住 1-换宿  2-外宿 3-离职
		 */
		private Integer type;


		private String compName;

		private List<Integer> dormitoryIds;
}
