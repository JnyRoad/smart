package com.tce.smart.platform.api.dto.req.news;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@Data
public class SaveNewsInfoReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	public Long id;

	@ApiModelProperty(value = "消息名")
	@NotBlank(message = "消息名不能为空")
	public String infoName;

	@ApiModelProperty(value = "新闻类型")
	@NotNull(message = "新闻类型不能为空")
	private Integer type;

	@ApiModelProperty(value = "文本/文件ID/URL")
	private String content;

	@ApiModelProperty(value = "文本样式")
	private String textStyle;

	@ApiModelProperty(value = "文字移动方式")
	private Integer textMoveType;

	@ApiModelProperty(value = "图片列表")
	private List<NewsInfoImageReqDTO> imageReqDTOS;

}
