package com.tce.smart.platform.api.dto.resp.news;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@Data
public class NewsListRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@ApiModelProperty(value = "ID")
	private Long id;

	@ApiModelProperty(value = "消息标题")
	public String infoName;

	@ApiModelProperty(value = "绑定终端名")
	private String terminalName;

	@ApiModelProperty(value = "消息类型Code")
	private Integer type;

	@ApiModelProperty(value = "消息类型Desc")
	private String typeDesc;

	@ApiModelProperty(value = "发布状态code")
	private Integer status;

	@ApiModelProperty(value = "发布状态Desc")
	private String statusDesc;

	@ApiModelProperty(value = "创建人")
	private String creator;

	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createTime;
}
