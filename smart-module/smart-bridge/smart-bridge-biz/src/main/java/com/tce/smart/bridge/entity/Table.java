package com.tce.smart.bridge.entity;

import com.tce.smart.bridge.annotation.Key;
import lombok.Data;

@Data
public class Table {
	@Key
	private String key;

}
