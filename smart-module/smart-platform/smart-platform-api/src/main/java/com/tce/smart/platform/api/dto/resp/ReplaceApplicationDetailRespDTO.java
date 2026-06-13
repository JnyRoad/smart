package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 补卡详情返回值
 * @author QIPEI
 *
 */
@Data
public class ReplaceApplicationDetailRespDTO {


	 /**
	   * 考勤月份
	   */
	    private String workMonth;
	    /**
	     *补卡开始时间
	     */
	    private String startTime;

	    private String processId;

	    /**
	   * 补卡原因
	   */
	    private String causeDesc;


	    /**
	     * 2段进
	     */
	    private String secondEnter;
	    /**
	     * 2段出
	     */
	    private String secondOut;

	    /**
	     * 4段进
	     */
	    private String fourthEnter;
	    /**
	     * 4段出
	     */
	    private String fourthOut;


	    /**
	     * 5段进
	     */
	    private String fifthEnter;
	    /**
	     * 5段出
	     */
	    private String fifthOut;


	    /**
		 * 2出是否跨天
		 */
		private String secondOutCover;


		/**
		 * 4入是否跨天
		 */
		private String fourthEnterCover;
		/**
		 * 4出是否跨天
		 */
		private String fourthOutCover;


		/**
		 * 5入是否跨天
		 */
		private String fifthEnterCover;
		/**
		 * 5出是否跨天
		 */
		private String fifthOutCover;

	    /**
	     * 备注
	     */
	    private String remark;
	    /**
	     * 附件图片的url
	     */
	    private String photoUrl;


	    private List<FlowRespDTO> flow;
}
