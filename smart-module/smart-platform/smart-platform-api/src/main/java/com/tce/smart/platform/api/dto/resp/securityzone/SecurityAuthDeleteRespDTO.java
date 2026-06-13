package com.tce.smart.platform.api.dto.resp.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@Data
public class SecurityAuthDeleteRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("超过多少天后删除权限")
    private Integer deleteDay;

	@ApiModelProperty("是否计算假期")
    private Integer isHoliday;

	@ApiModelProperty("是否计算出差")
    private Integer isBusiness;

	@ApiModelProperty("是否计算请假")
    private Integer isLeave;

	@ApiModelProperty("是否计算调休")
    private Integer isCompensatory;

	@ApiModelProperty("是否启用白名单")
    private Integer isWhiteList;

	@ApiModelProperty("白名单列表")
	private List<SecurityWhiteRespDTO> whiteList;
}
