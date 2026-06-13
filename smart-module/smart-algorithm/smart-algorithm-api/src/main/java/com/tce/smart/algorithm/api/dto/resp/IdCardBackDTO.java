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
public class IdCardBackDTO extends BaseDTO {

	@Desc(type = "处理后的图片")
	@ApiModelProperty("处理后的图片")
	private String handleImg;

	@Desc(type = "复印件判别")
	@ApiModelProperty("复印件判别")
	private String copy;

	@Desc(type = "签发机关")
	@ApiModelProperty("签发机关")
	private String issueAuthority;

	@Desc(type = "有效期限")
	@ApiModelProperty("有效期限")
	private String expiryDate;

}
