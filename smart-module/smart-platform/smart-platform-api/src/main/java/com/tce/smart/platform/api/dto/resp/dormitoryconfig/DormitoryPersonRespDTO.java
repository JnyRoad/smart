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
 * @date 2021-09-14 20:14:59
 */
@Data
public class DormitoryPersonRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * 配置表ID
   */
	@ApiModelProperty(value = "配置表ID")
	@JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long configId;
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
