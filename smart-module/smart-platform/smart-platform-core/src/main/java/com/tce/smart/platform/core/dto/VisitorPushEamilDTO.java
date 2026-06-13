package com.tce.smart.platform.core.dto;

import java.util.List;

import com.tce.smart.platform.core.entity.SmtVisitorPushEamil;

import lombok.Data;

@Data
public class VisitorPushEamilDTO {


	private List<SmtVisitorPushEamil> emails;

	private Integer type;

	private Integer parkId;
}
