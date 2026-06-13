package com.tce.smart.platform.core.dto;

import java.util.List;

import com.tce.smart.platform.core.vo.CheckFacePicVO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 人脸图片批量上传
 */
@Data
public class CheckFacePicDTO {

	@ApiModelProperty("图片上传信息")
	private List<CheckFacePicVO> facePicUpLoad;

	@ApiModelProperty("BUID")
	private String compId;

	@ApiModelProperty("园区id")
	private Integer parkId;

	@ApiModelProperty("任务名")
	private String taskName;

}
