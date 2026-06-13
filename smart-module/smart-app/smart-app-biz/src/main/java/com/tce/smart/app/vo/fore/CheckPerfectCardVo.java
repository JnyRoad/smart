package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信息完善-身份证OCR识别结果校验Vo
 * @author mkwu
 * @date 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckPerfectCardVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6536184066535073007L;

	/**
	 * 信息采集编号
	 */
	private Integer perfectId;

}
