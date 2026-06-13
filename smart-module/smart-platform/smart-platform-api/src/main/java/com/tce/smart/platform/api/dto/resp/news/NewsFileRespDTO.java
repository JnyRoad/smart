package com.tce.smart.platform.api.dto.resp.news;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.eventbus.AllowConcurrentEvents;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsFileRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@ApiModelProperty(value = "ID")
    private Long id;

	@ApiModelProperty(value = "文件名")
    private String fileName;

	@ApiModelProperty(value = "文件后缀")
	private String fileSuffix;

	@ApiModelProperty(value = "文件")
	private byte[] data;

	@ApiModelProperty(value = "文件大小")
	private Float fileSize;

	@ApiModelProperty(value = "文件url")
	private String fileUrl;

}
