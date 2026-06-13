package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: IdCardFrontDTO
 * @Package com.tce.operator.jsiot.bean
 * @Description:
 * @Author wuxinjian
 * @Date 2018/11/29 10:28
 * @Version V1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IdCardFrontDTO extends BaseDTO {

	@Desc(type = "姓名")
	@ApiModelProperty("姓名")
	private String name;

	@Desc(type = "性别")
	@ApiModelProperty("性别")
	private String sex;

	@Desc(type = "公民身份号码")
	@ApiModelProperty("公民身份号码")
	private String idNum;

	@Desc(type = "民族")
	@ApiModelProperty("民族")
	private String nation;

	@Desc(type = "出生")
	@ApiModelProperty("出生")
	private String birth;

	@Desc(type = "住址")
	@ApiModelProperty("住址")
	private String address;

	@Desc(type = "头像")
	@ApiModelProperty("头像")
	private String headImg;

	@Desc(type = "处理后的图片")
	@ApiModelProperty("处理后的图片")
	private String handleImg;

	@Desc(type = "复印件判别")
	@ApiModelProperty("复印件判别")
	private String copy;

}
