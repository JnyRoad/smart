package com.tce.smart.platform.api.dto.resp.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 宿舍楼层统计
 * @date: 2020/9/29 8:48
 * @author: fushiping
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryStatisticsListRespDTO implements Serializable {
	private static final long serialVersionUID = 1470561687953010349L;


	@ApiModelProperty(value = "房间性别类型 0-男，1-女，2-夫妻，3-其他")
	private Integer roomSex;

	@ApiModelProperty(value = "房间性别类型Desc")
	private String roomSexDesc;

	@ApiModelProperty(value = "房间详细统计信息")
	List<DormitoryStatisticsRespDTO> sexList;

}
