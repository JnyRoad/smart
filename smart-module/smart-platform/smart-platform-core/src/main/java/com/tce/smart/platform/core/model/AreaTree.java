package com.tce.smart.platform.core.model;

import java.util.List;

import lombok.Data;

@Data
public class AreaTree {

	private Integer value;

	private String label;

	private List<AreaTree> children;
}
