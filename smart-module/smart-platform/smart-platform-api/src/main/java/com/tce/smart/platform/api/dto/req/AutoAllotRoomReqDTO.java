package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 自动分配宿舍请求DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class AutoAllotRoomReqDTO implements Serializable {
	private static final long serialVersionUID = -3414907808079720881L;

	@ApiModelProperty(value = "工号")
	private String badge;

	@ApiModelProperty(value = "园区ID",required = true)
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID",required = true)
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "房间类型",required = true)
	private Integer roomType;

	@ApiModelProperty(value = "名称",required = true)
	private String name;

	@ApiModelProperty(value = "身份证号",required = true)
	private String certno;

	@ApiModelProperty(value = "房间性别类型 0-男，1-女",required = true)
	private Integer sex;

	@ApiModelProperty(value = "民族",required = true)
	private String nation;

	@ApiModelProperty(value = "出生日期",required = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

	@ApiModelProperty(value = "住址",required = true)
	private String address;

	@ApiModelProperty(value = "有效期开始时间",required = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date validDateStart;

	@ApiModelProperty(value = "有效期结束时间",required = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date validDateEnd;

	@ApiModelProperty(value = "签证机关",required = true)
	private String signOrg;

	@ApiModelProperty(value = "房间号")
	private Integer roomId;

	@ApiModelProperty(value = "床位号")
	private Integer bedId;
}
