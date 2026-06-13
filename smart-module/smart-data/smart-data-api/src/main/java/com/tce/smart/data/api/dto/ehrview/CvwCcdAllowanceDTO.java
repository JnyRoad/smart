package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;

/*
 * 补贴类型
 */
@Data
public class CvwCcdAllowanceDTO implements Serializable {

	private static final long serialVersionUID = 853496697011128047L;

    private Integer Id;

    private String Title;

    private Integer ComputationRule;

    private Integer ConvertRule;

    private Integer Pzid;


}
