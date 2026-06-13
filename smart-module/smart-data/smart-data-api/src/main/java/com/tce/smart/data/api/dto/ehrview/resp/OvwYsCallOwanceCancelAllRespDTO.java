package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/***
 * 外宿审批撤销实体类
 * @author QIPEI
 * 2019-09-09
 */
@Data
public class OvwYsCallOwanceCancelAllRespDTO extends BaseVO {

	private static final long serialVersionUID = 7919240156446989441L;

	/**
	 * 员工号
	 */
	private String Badge;

	/**
	 * 补贴类型
	 */
	private Integer xtype;

	/**
	 * 补贴开始时间
	 */
	private String begindate;


}
