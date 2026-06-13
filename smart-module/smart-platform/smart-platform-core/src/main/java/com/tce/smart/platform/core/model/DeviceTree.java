package com.tce.smart.platform.core.model;

import java.util.List;

import lombok.Data;

@Data
public class DeviceTree {

	private String id;

	private String label;

	private String disabled;

	private List<DeviceTree> children;
}
