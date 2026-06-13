package com.tce.smart.app.ao.wechat;

import java.util.List;

import lombok.Data;

@Data
public class EducationHisAo {

	private String applicationId;

	private List<ApplicationEducationAo> educationHis;
}
