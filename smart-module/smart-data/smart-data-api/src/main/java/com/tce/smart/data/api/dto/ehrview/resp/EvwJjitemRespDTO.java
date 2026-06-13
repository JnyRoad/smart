package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class EvwJjitemRespDTO extends BaseVO {

    private static final long serialVersionUID = -8997920024768403177L;

	/**
	 * 交接项目Id
	 */
	private Integer jjItemId;

	/**
	 * 人事区域
	 */
    private Integer ezid;
	/**
	 * 人事区域名称
	 */
    private String empzone;
	/**
	 * 责任部门
	 */
    private Integer zrdep;
	/**
	 *
	 */
    private String zrdepName;
	/**
	 * 交接项目
	 */
    private String jjItem;
	/**
	 * 交接人工号
	 */
    private String jjr;
	/**
	 * 交接人姓名
	 */
    private String jjrName;
	/**
	 * 金额
	 */
    private Double je;
	/**
	 * 金额
	 */
    private String jjremark;

}
