package com.tce.smart.app.dto;
import com.tce.smart.app.entity.AppModuleInfo;
import lombok.Data;

import java.util.List;

@Data
public class AppServerDto {
	/**
	 * 准备添加的数据
	 */
	private List<AppModuleDto> insertData;
	/**
	 * 准备修改的数据
	 */
	private List<AppModuleInfoDto> updateData;
	/**
	 * 准备删除的数据
	 */
	private int[] id;
}
