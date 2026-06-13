package com.tce.smart.app.ao.wechat;

import java.util.List;

import lombok.Data;

@Data
public class WorkHisAo {

	private String applicationId;

	private List<ApplicationWorkAo> workHis;


}
