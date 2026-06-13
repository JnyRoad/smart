package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 *
 * @ClassName FlowOverAo.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 审核完成推送消息
 */
@Data
public class FlowOverReqDTO {

	/**
	 * 流程ID
	 */
	private String requestid;

	/**
	 * 流程
	 */
	private List<FlowRecordReqDTO> flowRecord;

//	class FlowRecord {
//
//		/**
//		 * 操作者ID (OA系统ID)
//		 */
//		private String operator;
//
//		/**
//		 * 操作在工号
//		 */
//		private String workcode;
//
//		/**
//		 * 操作者姓名
//		 */
//		private String lastname;
//
//		/**
//		 * 操作日期
//		 */
//		private String operatedate;
//
//		/**
//		 * 操作时间
//		 */
//		private String operatetime;
//
//		/**
//		 * 操作类型
//		 */
//		private String logtype;
//
//		/**
//		 * 操作类型说明
//		 */
//		private String Description;
//
//		/**
//		 * 签批意见
//		 */
//		private String remark;
//
//		public String getOperator() {
//			return operator;
//		}
//
//		public void setOperator(String operator) {
//			this.operator = operator;
//		}
//
//		public String getWorkcode() {
//			return workcode;
//		}
//
//		public void setWorkcode(String workcode) {
//			this.workcode = workcode;
//		}
//
//		public String getLastname() {
//			return lastname;
//		}
//
//		public void setLastname(String lastname) {
//			this.lastname = lastname;
//		}
//
//		public String getOperatedate() {
//			return operatedate;
//		}
//
//		public void setOperatedate(String operatedate) {
//			this.operatedate = operatedate;
//		}
//
//		public String getOperatetime() {
//			return operatetime;
//		}
//
//		public void setOperatetime(String operatetime) {
//			this.operatetime = operatetime;
//		}
//
//		public String getLogtype() {
//			return logtype;
//		}
//
//		public void setLogtype(String logtype) {
//			this.logtype = logtype;
//		}
//
//		public String getDescription() {
//			return Description;
//		}
//
//		public void setDescription(String description) {
//			Description = description;
//		}
//
//		public String getRemark() {
//			return remark;
//		}
//
//		public void setRemark(String remark) {
//			this.remark = remark;
//		}
//
//	}
}
