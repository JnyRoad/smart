package com.tce.smart.platform.api.dto.req.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
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
public class NewsInfoImageReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "图片ID")
    private String imageId;

    @ApiModelProperty(value = "图片顺序")
    private Integer sort;

}
