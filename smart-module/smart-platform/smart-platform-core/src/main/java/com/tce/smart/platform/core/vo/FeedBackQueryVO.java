package com.tce.smart.platform.core.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FeedBackQueryVO {


	 private Integer id;

		/**
		 * 反馈员工工号
		 */
		private String staffBadge;

		/**
		 * 反馈员工姓名
		 */
		private String staffName;

		/**
		 * 反馈员工电话
		 */
		private String staffPhone;


		/**
		 * BU
		 */
		private String compName;
		/**
		 * 部门
		 */
		private String depName;

		/**
		 * 反馈问题标签
		 */
		private String question;

		/**
		 * 反馈时间
		 */
		private LocalDateTime createTime;

		/**
		 * 处理状态 0-未处理 1-已处理
		 */
		private Integer status;

		/**
		 * 处理人
		 */
		private String operator;

		/**
		 * 处理时间
		 */
		private LocalDateTime operateTime;

		/**
		 * 回复
		 */
		private String reply;
}
