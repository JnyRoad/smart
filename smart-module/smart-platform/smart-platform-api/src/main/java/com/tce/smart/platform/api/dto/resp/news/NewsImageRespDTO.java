package com.tce.smart.platform.api.dto.resp.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Data
public class NewsImageRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@ApiModelProperty(value = "ID")
    private Long id;

	@ApiModelProperty(value = "信息ID")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long infoId;

	@ApiModelProperty(value = "图片链接")
    private String imageUrl;

	@ApiModelProperty(value = "图片顺序")
    private Integer sort;

	@ApiModelProperty(value = "图片ID")
	private String imageId;

}
