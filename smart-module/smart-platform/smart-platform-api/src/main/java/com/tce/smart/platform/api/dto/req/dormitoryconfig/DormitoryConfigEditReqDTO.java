package com.tce.smart.platform.api.dto.req.dormitoryconfig;

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
public class DormitoryConfigEditReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @ApiModelProperty(value = "ID")
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
    private List<DormitoryPersonReqDTO> personList;

}
