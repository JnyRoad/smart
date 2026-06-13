package com.tce.smart.app.dto;
import com.tce.smart.app.entity.AppModuleInfo;
import lombok.Data;

import java.util.List;

@Data
public class AppModuleDto {
	private AppModuleInfo  appParent;
	private List<AppModuleInfoDto> listChild;
}
