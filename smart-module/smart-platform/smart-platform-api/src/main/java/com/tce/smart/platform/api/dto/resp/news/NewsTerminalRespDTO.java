package com.tce.smart.platform.api.dto.resp.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 17:59:47
 */
@Data
public class NewsTerminalRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "ID")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

	@ApiModelProperty(value = "终端名")
    private String name;

	@ApiModelProperty(value = "IP")
    private String ip;

	@ApiModelProperty(value = "备注")
    private String remark;

	@ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

	@ApiModelProperty(value = "创建人")
    private String creator;

	@ApiModelProperty(value = "消息内容Id")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long infoId;

	@ApiModelProperty(value = "消息标题")
	private String infoName;

	@ApiModelProperty(value = "消息类型")
	private String infoType;

	@ApiModelProperty(value = "发布时效code")
	private Integer timeType;

	@ApiModelProperty(value = "生效开始时间")
	private LocalDateTime startTime;

	@ApiModelProperty(value = "生效结束时间")
	private LocalDateTime endTime;

}
