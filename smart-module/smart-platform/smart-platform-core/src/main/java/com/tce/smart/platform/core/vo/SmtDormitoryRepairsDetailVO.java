package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryRepairsDetailVO
 * @date: 2020-07-24 18:11
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRepairsDetailVO extends SmtDormitoryRepairsVO{

	/**
	 * 故障描述
	 */
	private String faultDesc;

	/**
	 * 描述图片code
	 */
	private String faultImgs;

	/**
	 * 故障图片地址列表
	 */
	private List<String> imgs;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 回复列表
	 */
	private List<RepairReply> repairReplyList;

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RepairReply{

		/**
		 * 回复人姓名
		 */
		private String replyName;

		private String replyStatusDesc;

		/**
		 * 回复描述
		 */
		private String replyDesc;

		/**
		 * 回复时间
		 */
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private Date replyTime;
	}
}
