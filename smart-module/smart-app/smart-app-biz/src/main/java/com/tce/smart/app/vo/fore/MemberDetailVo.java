package com.tce.smart.app.vo.fore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tce.smart.common.core.vo.BaseVO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客随行详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;


	/**
	 * 随行人员姓名
	 */
	@ApiModelProperty(value = "随行人员姓名",required = true)
	private String memberName;
	/**
	 * 随行人员图片信息
	 */
	@ApiModelProperty(value = "随行人员图片信息",required = true)
	@JsonIgnore
	private String memberPhoto;

}
