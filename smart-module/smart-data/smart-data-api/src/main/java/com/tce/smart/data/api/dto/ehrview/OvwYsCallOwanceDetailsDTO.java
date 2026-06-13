package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OvwYsCallOwanceDetailsDTO implements Serializable {

	private static final long serialVersionUID = 1507155847724424476L;

	private Integer id;

	private Integer eid;

	private String badge;

	private Integer xtype;

	private Date begindate;

	private Date enddate;

	private Double amount;

	private Integer computationrule;

	private Integer convertrule;
}
