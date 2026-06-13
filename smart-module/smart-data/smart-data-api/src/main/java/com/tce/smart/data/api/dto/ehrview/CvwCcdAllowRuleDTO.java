package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;

/**
 * 补贴计算规则
 *
 * @author qipei
 */
@Data
public class CvwCcdAllowRuleDTO implements Serializable {
	private static final long serialVersionUID = -2864354769450509758L;

	private Integer Id;

	private String Title;

	private String Type;
}
