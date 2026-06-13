package com.tce.smart.platform.api.dto.resp;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
@Data
public class WechatBandingPageRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;

	@ApiModelProperty(value = "openId")
    private String openId;

	@ApiModelProperty(value = "授权平台")
	private String from;

	@ApiModelProperty(value = "unionId")
    private String unionId;

	@ApiModelProperty(value = "工号")
    private String badge;

	@ApiModelProperty(value = "姓名")
    private String staffName;

	@ApiModelProperty(value = "园区id")
    private Integer parkId;

	@ApiModelProperty(value = "园区名")
    private String parkName;

	@ApiModelProperty(value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
