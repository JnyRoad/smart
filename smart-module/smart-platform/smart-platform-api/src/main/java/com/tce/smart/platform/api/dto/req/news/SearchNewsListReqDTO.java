package com.tce.smart.platform.api.dto.req.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 17:59:54
 */
@Data
public class SearchNewsListReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "发布类型")
    private Integer type;

	@ApiModelProperty(value = "发布状态")
    private Integer status;

	@ApiModelProperty(value = "发布开始时间")
    private LocalDateTime startTime;

	@ApiModelProperty(value = "发布结束时间")
    private LocalDateTime endTime;

	@ApiModelProperty(value = "消息标题")
    private String infoName;

}
