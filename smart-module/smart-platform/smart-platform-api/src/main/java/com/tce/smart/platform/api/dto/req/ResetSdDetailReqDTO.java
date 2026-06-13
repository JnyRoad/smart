package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 重置抄表数据
 *
 * @author wuling
 * @date 2021-08-31 15:09:27
 */
@Data
public class ResetSdDetailReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty("楼层Id")
	private Integer floorId;

	@ApiModelProperty("房间Id")
	private Integer roomId;

	@ApiModelProperty(value = "抄表月份",required = true)
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;


}
