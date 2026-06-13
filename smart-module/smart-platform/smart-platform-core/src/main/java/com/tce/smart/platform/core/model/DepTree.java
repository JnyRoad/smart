package com.tce.smart.platform.core.model;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class DepTree extends BaseVO {

	private Integer value;

	private String label;

	private List<DepTree> children;
}
