package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 考勤确认与工资签单
 *
 * @author fushiping
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 手机号码
	 */
	private String numbers;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 人员姓名
	 */
	private String personName;


}
