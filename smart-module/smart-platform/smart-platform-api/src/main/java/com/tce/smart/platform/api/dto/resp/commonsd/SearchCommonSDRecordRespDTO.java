package com.tce.smart.platform.api.dto.resp.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 查询公摊水电表记录响应DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SearchCommonSDRecordRespDTO implements Serializable {
	private static final long serialVersionUID = 2885792010869190244L;

	@ApiModelProperty(value = "记录ID")
	private Long id;

	@ApiModelProperty(value = "水电表名称")
	private String sdName;

	@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
	private Integer categoryId;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼栋名称")
	private String dormitoryName;

	@ApiModelProperty(value = "房间名称列表")
	private String roomNameList;

	@ApiModelProperty(value = "房间ID列表")
	private List<Integer> roomIds;

	@ApiModelProperty(value = "楼层ID列表")
	private List<Integer> floorIds;
}
