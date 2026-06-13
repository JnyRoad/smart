package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:53
 */
@Data
public class WaterMeterAddDTO extends BaseDTO {

	@ApiModelProperty("名称")
	@NotBlank(message = "水表名称不能为空")
	private String name;

	@ApiModelProperty("序号")
	@NotNull(message = "水表序号不能为空")
	private Integer seq;

	@ApiModelProperty("水表通信端口号")
	@NotBlank(message = "水表通信端口号不能为空")
	private String port;

	@ApiModelProperty("水表大类：0、冷水表；1、热水表；2、直饮水水表；3、中水水表；4、大口径水表")
	@NotBlank(message = "水表大类不能为空")
	private String largeClass;

	@ApiModelProperty("集中器ID")
	@NotNull(message = "水表集中器ID不能为空")
	private Long concentratorId;

	@ApiModelProperty("房间ID")
	private Integer roomId;

	@ApiModelProperty("标签ID数组")
	private List<Long> tagIds;

	@ApiModelProperty("通信地址")
	private String address;

	@ApiModelProperty("区域类型：0、宿舍；1、厂区")
	@NotNull(message = "区域类型不能为空")
	private Integer placeType;

	@ApiModelProperty("厂区ID")
	private Integer areaId;
}
