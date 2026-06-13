package com.tce.smart.tool.constant;

/**
 * 带审批类型
 * @author Lenovo
 *
 */
public interface ApproveListTypeConstants {

	/**
	 * 离职
	 */
    Integer LEAVE = 1;

	/**
	 * 访客
	 */
	Integer VISITOR = 2;

	/**
	 *  物品
	 */
	Integer ARTICLE = 3;

	/**
	 * 员工申诉
	 */
	Integer APPEAL = 4;

	/**
	 * 园区报修
	 */
	Integer REPAIR = 5;

	/**
	 * 退宿申请
	 */
	Integer QUIT_DORMITORY = 6;

	/**
	 * 离职标题
	 */
	String LEAVE_TITLE = "离职申请";

	/**
     * 离职日期
     */
    String LEAVE_DATE = "离职日期";

	/**
     * 访客预约开始日期
     */
	String START_TIME = "开始时间";

	/**
     * 访客预约结束日期
     */
	String END_TIME = "结束时间";

}
