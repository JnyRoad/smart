package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.model.DeviceTree;
import lombok.Data;

import java.util.List;

@Data
public class StaffDeviceAuthInfoVO {

	private String authName;

	private String remark;

	private String typeName;

	private List<DeviceTree> children;
}
