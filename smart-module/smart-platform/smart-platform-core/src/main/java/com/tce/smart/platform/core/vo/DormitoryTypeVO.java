package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.entity.SmtDormitoryLevel;
import lombok.Data;

import java.util.List;

/**
 * 添加或修改宿舍类型的参数
 * @author 齐佩
 *
 */
@Data
public class DormitoryTypeVO {

	private Integer id;

	private String typeName;
	/**
	 * 每个类型房间中床位的默认个数
	 */
	private Integer bedTotal;

	private Integer parkId;

	private String parkName;

	private List<String> levelIds;

	private List<String> levelNames;


}
