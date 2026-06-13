package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: StaffFamilyDormitoryReqDTO
 * @date: 2020-07-07 11:49
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffFamilyDormitoryReqDTO implements Serializable {
	private static final long serialVersionUID = 4156789278406172013L;

	@ApiModelProperty(value = "标识Id")
	private Long id;

	@ApiModelProperty(value = "家属工号")
	private String badge;

	@ApiModelProperty(value = "员工工号",required = true)
	private String staffBadge;

	@ApiModelProperty(value = "姓名",required = true)
	private String name;

	@ApiModelProperty(value = "身份证",required = true)
	private String certno;

	@ApiModelProperty(value = "手机号",required = true)
	private String phone;

	@ApiModelProperty(value = "家属关系 1.夫妻 2.直系血亲 3.旁系血亲 4.近姻亲 5.其他",required = true)
	private Integer relation;
}
