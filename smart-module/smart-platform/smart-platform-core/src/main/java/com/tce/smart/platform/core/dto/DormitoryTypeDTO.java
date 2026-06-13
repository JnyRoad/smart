package com.tce.smart.platform.core.dto;

import lombok.Data;
import java.util.List;

/**
 * 添加或修改宿舍类型的参数
 * @author 齐佩
 *
 */
@Data
public class DormitoryTypeDTO {

	private Integer id;

	private String typeName;
	/**
	 * 每个类型房间中床位的默认个数
	 */
	private Integer bedTotal;

	private Integer parkId;

	private List<String> jches;

}
