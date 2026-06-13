package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName: LivenessRecordAddDTO
 * @Package com.tce.smart.algorithm.api.dto.req
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/18 19:47
 * @Version V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LivenessRecordAddDTO extends BaseDTO {

	/**
	 * 1成功,0失败
	 */
	@NotNull(message = "活体结果不能为空")
	@ApiModelProperty("活体结果:1成功,0失败")
	private Integer isSuccess;

	@NotEmpty(message = "活体检测提取图片不能为空")
	@ApiModelProperty("活体检测提取图片集合")
	private List<String> imageBase64List;
	/**
	 * 活体置信度:0-1小数
	 */
	@ApiModelProperty("活体置信度:0-1小数")
	private Double livevnessConfidence;
	/**
	 * 错误信息
	 */
	@ApiModelProperty("错误信息")
	private String errorMessage;
	/**
	 * 请求耗时(ms)
	 */
	@NotNull(message = "活体检测耗时不能为空")
	@ApiModelProperty("活体检测(ms)")
	private Integer consumeTime;
}
