package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAskLeaveTypeVO extends Model<SearchAskLeaveTypeVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 类型编号
	 */
	private String vacateCode;
	/**
	 * 类型名称
	 */
	private String vacateName;

	/**
	 * 类型说明
	 */
	private String vacateRemark;


}
