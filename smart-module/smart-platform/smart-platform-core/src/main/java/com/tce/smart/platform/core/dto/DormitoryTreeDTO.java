package com.tce.smart.platform.core.dto;

import java.util.List;

import lombok.Data;

@Data
public class DormitoryTreeDTO {

	private Integer id;

	private String label;

	private List<DormitoryTreeDTO> children;
}
