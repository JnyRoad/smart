package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * @description: 查询宿舍详情DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchDormitoryRoomDetailReqDTO implements Serializable {
	private static final long serialVersionUID = -5589332175220795870L;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "房间类型")
	private Integer roomType;

	@ApiModelProperty(value = "入住状态 1-未满， 2-已满， 3-空房")
	private Integer inStatus;

	@ApiModelProperty(value = "房间性别类型 0-男，1-女，2-夫妻，3-其他")
	private Integer sex;

	@ApiModelProperty(value = "是否正常房间 1-正常，2-异常 默认正常")
	private Integer isNormal;

	@ApiModelProperty(value = "当前页")
	private long current;

	@ApiModelProperty(value = "每页大小")
	private long size;
}
