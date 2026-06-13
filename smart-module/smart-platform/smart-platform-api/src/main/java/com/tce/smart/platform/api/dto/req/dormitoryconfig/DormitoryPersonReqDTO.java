package com.tce.smart.platform.api.dto.req.dormitoryconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:59
 */
@Data
public class DormitoryPersonReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * 用户账号
   */
	@ApiModelProperty(value = "用户账号")
    private String account;
    /**
   * 用户名
   */
	@ApiModelProperty(value = "用户名")
    private String name;
    /**
   * 关联宿舍楼
   */
	@ApiModelProperty(value = "关联宿舍楼")
    private List<Integer> dormitoryIds;
    /**
   * 园区ID
   */
	@ApiModelProperty(value = "园区ID")
    private Integer parkId;

}
