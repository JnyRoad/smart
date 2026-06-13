package com.tce.smart.platform.core.model;

import lombok.Data;

import java.util.List;

@Data
public class TaskDownRecordPark {

	private String value;

	private String label;

	private List<TaskDownRecordPark> children;
}
