package com.tce.smart.app.vo;

import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;

@Data
public class AppCheckVo extends BaseVO {
	private List<AppModuleInfo>  moduleName;
	private List<AppParkSubject> parkName;
}
