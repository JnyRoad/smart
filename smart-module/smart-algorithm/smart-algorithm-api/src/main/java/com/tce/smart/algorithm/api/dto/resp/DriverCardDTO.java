package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wuxinjian
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DriverCardDTO extends BaseDTO {

	@Desc(type = "姓名")
	@ApiModelProperty("姓名")
	private String name;

	@Desc(type = "性别")
	@ApiModelProperty("性别")
	private String sex;

	@Desc(type = "证号")
	@ApiModelProperty("证号")
	private String idNum;

	@Desc(type = "出生日期")
	@ApiModelProperty("出生日期")
	private String birth;

	@Desc(type = "住址")
	@ApiModelProperty("住址")
	private String address;
	/**
	 * 车型
	 */
	@Desc(type = "准驾车型")
	@ApiModelProperty("准驾车型")
	private String carType;
	/**
	 * 头像照片
	 */
	@Desc(type = "头像")
	@ApiModelProperty("头像")
	private String headImg;
	/**
	 * 处理后照片
	 */
	@Desc(type = "处理后的图片")
	@ApiModelProperty("处理后的图片")
	private String handleImg;

	@Desc(type = "复印件判别")
	@ApiModelProperty("复印件判别")
	private String copy;
	/**
	 * 初始领证日期
	 */
	@Desc(type = "初始领证日期")
	@ApiModelProperty("初始领证日期")
	private String firstDate;
	/**
	 * 有效起始日期
	 */
	@Desc(type = "有效起始日期")
	@ApiModelProperty("有效起始日期")
	private String useDate;
	/**
	 * 有效期限
	 */
	@Desc(type = "有效期限")
	@ApiModelProperty("有效期限")
	private String expiryDate;
}
