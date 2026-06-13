package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryRepairsReqDTO
 * @date: 2020-07-20 14:13
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtDormitoryRepairsReqDTO implements Serializable {
	private static final long serialVersionUID = -7289022427725195102L;
	/**
	 * 园区Id
	 */
	@ApiModelProperty(value = "园区Id",required = true)
	private Integer parkId;

	/**
	 * 楼栋
	 */
	@ApiModelProperty(value = "楼栋",required = true)
	private String dormitoryName;

	/**
	 * 房间名称
	 */
	@ApiModelProperty(value = "房间名称",required = true)
	private String roomName;

	/**
	 * 员工Id
	 */
	@ApiModelProperty(value = "员工Id",required = true)
	private Long staffId;

	/**
	 * 员工姓名
	 */
	@JsonIgnore
	private String staffName;


	/**
	 * 开始时间
	 */
	@JsonIgnore
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date beginTime;

	/**
	 * 结束时间
	 */
	@JsonIgnore
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endTime;

	/**
	 * 范围类型
	 */
	@ApiModelProperty(value = "范围类型",required = true)
	private List<Integer> range;

	/**
	 * 维修类型
	 */
	@ApiModelProperty(value = "维修类型",required = true)
	private Integer repairType;

	/**
	 * 维修类型描述
	 */
	@JsonIgnore
	private String typeDesc;

	/**
	 * 状态
	 */
	@JsonIgnore
	private Integer status;

	/**
	 * 状态描述
	 */
	@JsonIgnore
	private String statusDesc;

	/**
	 * 故障描述
	 */
	@ApiModelProperty(value = "维修类型",required = false)
	private String faultDesc;

	/**
	 * 故障图片
	 */
	@ApiModelProperty(value = "维修类型",required = false)
	private List<String> faultImgs;

}
