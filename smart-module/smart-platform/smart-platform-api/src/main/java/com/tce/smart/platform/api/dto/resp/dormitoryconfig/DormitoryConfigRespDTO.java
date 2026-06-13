package com.tce.smart.platform.api.dto.resp.dormitoryconfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
@Data
public class DormitoryConfigRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @ApiModelProperty(value = "ID")
	@JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区id
   */
	@ApiModelProperty(value = "园区id")
    private Integer parkId;
    /**
   * 园区名
   */
	@ApiModelProperty(value = "园区名")
    private String parkName;
    /**
   * 关联BU
   */
	@ApiModelProperty(value = "关联BU")
    private List<String> relationBus;
	/**
	 * 后台数据权限人员
	 */
	@ApiModelProperty(value = "后台数据权限人员")
    private List<DormitoryPersonRespDTO> personList;

}
