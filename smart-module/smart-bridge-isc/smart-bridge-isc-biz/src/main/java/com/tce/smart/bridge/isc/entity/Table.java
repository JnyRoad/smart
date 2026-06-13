package com.tce.smart.bridge.isc.entity;

import com.tce.smart.bridge.isc.annotation.Key;
import lombok.Data;

@Data
public class Table {
	@Key
	private String key;

}
